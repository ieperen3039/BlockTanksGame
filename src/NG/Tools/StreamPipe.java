package NG.Tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;

/**
 * A pipe that supplies an InputStream and an OutputStream. The InputStream blocks until data is available. The output
 * stream blocks until it has enough space to write. All data written to the OutputStream can be read from the
 * InputStream. This object assumes that the InputStream and the OutputStream are accessible to at most one thread each.
 * The OutputStream only flushes when strictly necessary
 * @author Geert van Ieperen. Created on 5-7-2018.
 */
public class StreamPipe {
    private LinkedInputStream source;
    private LinkedOutputStream sink;
    private Semaphore hasItems;
    private Semaphore hasSpace;

    private byte[] buffer;
    private int bufferHead = 0;
    private int bufferTail = 0;
    private int virtualHead = 0; // will overflow and wrap around
    private int virtualTail = 0;
    private boolean isClosed;

    public StreamPipe(int bufferSize) {
        if (bufferSize < 1) throw new IllegalArgumentException("buffer must have size at least 1, got " + bufferSize);

        buffer = new byte[bufferSize];
        hasItems = new Semaphore(0, false);
        hasSpace = new Semaphore(bufferSize, false);
        isClosed = false;

        source = new LinkedInputStream(bufferSize);
        sink = new LinkedOutputStream(bufferSize);
    }

    public InputStream getInputStream() {
        return source;
    }

    public OutputStream getOutputStream() {
        return sink;
    }


    private class LinkedInputStream extends InputStream {
        private final int bufferSize;

        public LinkedInputStream(int bufferSize) {
            this.bufferSize = bufferSize;
        }

        @Override
        public int read() throws IOException {
            if (isClosed && available() == 0) {
                return -1;
            }
            virtualTail++;

            try {
                hasItems.acquire();
            } catch (InterruptedException e) {
                virtualTail--;
                throw new IOException("Interrupted while waiting for input");
            }

            if (isClosed && available() == -1) {
                return -1;
            }

            int result = buffer[bufferTail] & 0xFF;
            bufferTail = (bufferTail + 1) % bufferSize;
            hasSpace.release();

            return result;
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            if (length <= 0 || offset < 0 || offset + length > bytes.length) {
                if (length == 0) {
                    return 0;
                }
                throw new ArrayIndexOutOfBoundsException();
            }

            if (isClosed && available() < length) {
                if (available() == 0) return -1;
                length = available();
            }

            virtualTail += length;
            int tail = bufferTail;
            try {
                if (length > bufferSize) {
                    readInChunks(bytes, offset, length, bufferSize / 2);

                } else {
                    readUnsafe(bytes, offset, length);
                }
            } catch (IOException ex) {
                virtualTail -= length;
                bufferTail = tail;
                throw ex;
            }

            if (isClosed && available() < 0) return -1;
            return length;
        }

        private void readUnsafe(byte[] bytes, int offset, int length) throws IOException {
            try {
                hasItems.acquire(length);
            } catch (InterruptedException e) {
                throw new IOException("Interrupted while waiting for input");
            }

            int firstHalf = bufferSize - bufferTail;
            if (bufferTail + length > bufferSize) { // wraps around
                System.arraycopy(buffer, bufferTail, bytes, offset, firstHalf);
                System.arraycopy(buffer, 0, bytes, offset + firstHalf, length - firstHalf);
            } else {
                System.arraycopy(buffer, bufferTail, bytes, offset, length);
            }

            bufferTail = (bufferTail + length) % bufferSize;
            hasSpace.release(length);
        }

        private void readInChunks(byte[] bytes, int offset, int targetLength, int chunkSize) throws IOException {
            int i = 0;
            do {
                readUnsafe(bytes, offset + i, chunkSize);
                i += chunkSize;
            } while (i + chunkSize < targetLength);

            readUnsafe(bytes, offset + i, targetLength % chunkSize);
        }

        @Override
        public int available() {
            return virtualHead - virtualTail; // overflow is dealt with
        }

        @Override
        public void close() {
            isClosed = true;
            hasSpace.release(bufferSize);
        }
    }

    private class LinkedOutputStream extends OutputStream {
        private final int bufferSize;
        private int unFlushed = 0;

        public LinkedOutputStream(int bufferSize) {
            this.bufferSize = bufferSize;
        }

        @Override
        public void write(int b) throws IOException {
            if (isClosed) return;
            virtualHead++;

            try {
                hasSpace.acquire();
                if (isClosed) throw new IOException("Tried writing on closed channel");
            } catch (InterruptedException e) {
                virtualHead--;
                throw new IOException("Interrupted while waiting for input");
            }

            buffer[bufferHead] = (byte) (b & 0xFF);
            bufferHead = (bufferHead + 1) % bufferSize;
            unFlushed++;
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            if (length <= 0 || offset < 0 || offset + length > bytes.length) {
                if (length == 0) {
                    return;
                }
                throw new ArrayIndexOutOfBoundsException();
            }
            if (isClosed) throw new IOException("Tried writing on closed channel");

            virtualHead += length;
            int preHead = bufferHead;

            try {
                if (length > bufferSize) {
                    writeInChunks(bytes, offset, length, bufferSize / 2);

                } else {
                    writeUnsafe(bytes, offset, length);
                }

            } catch (IOException ex) {
                bufferHead = preHead;
                virtualHead -= length;
                throw ex;
            }
        }

        private void writeUnsafe(byte[] bytes, int offset, int length) throws IOException {
            try {
                hasSpace.acquire(length);
                if (isClosed) throw new IOException("Tried writing on closed channel");
            } catch (InterruptedException e) {
                throw new IOException("Interrupted while waiting to write");
            }

            int firstHalf = bufferSize - bufferHead;
            if (bufferHead + length > bufferSize) { // wraps around
                System.arraycopy(bytes, offset, buffer, bufferHead, firstHalf);
                System.arraycopy(bytes, offset + firstHalf, buffer, 0, length - firstHalf);
            } else {
                System.arraycopy(bytes, offset, buffer, bufferHead, length);
            }

            bufferHead = (bufferHead + length) % bufferSize;
            unFlushed += length;

//            Logger.DEBUG.print(Arrays.toString(Arrays.copyOfRange(bytes, offset, offset + length)));
        }

        private void writeInChunks(byte[] bytes, int offset, int targetLength, int chunkSize) throws IOException {
            int i = 0;
            do {
                flush();
                writeUnsafe(bytes, offset + i, chunkSize);
                i += chunkSize;
            } while (i + chunkSize <= targetLength);
            flush();

            int remain = targetLength % chunkSize;
            if (remain > 0) writeUnsafe(bytes, offset + i, remain);
        }

        @Override
        public void close() {
            flush();
            isClosed = true;
            hasItems.release(bufferSize);
        }

        @Override
        public void flush() {
            if (unFlushed == 0) return;
            hasItems.release(unFlushed);
            unFlushed = 0;
        }
    }
}
