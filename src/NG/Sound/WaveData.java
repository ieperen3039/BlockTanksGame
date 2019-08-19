package NG.Sound;
//package org.lwjgl.util;
/*
 * Copyright (c) 2002 Lightweight Java Game Library Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *	 notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *	 notice, this list of conditions and the following disclaimer in the
 *	 documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'Light Weight Java Game Library' nor the names of
 *	 its contributors may be used to endorse or promote products derived
 *	 from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
// Note 6-feb-18: this file has been changed by a third party

import org.lwjgl.openal.AL10;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * $Id: WaveData.java,v 1.15 2004/02/26 21:51:58 matzon Exp $
 * <p>
 * Utitlity class for loading wavefiles.
 *
 * @author Brian Matzon <brian@matzon.dk>
 * @version $Revision: 1.15 $
 */
public class WaveData {
    /**
     * actual wave data
     */
    public final ByteBuffer data;

    /**
     * format type of data
     */
    public final int format;

    /**
     * sample rate of data
     */
    public final int samplerate;

    /**
     * Creates a new WaveData
     *
     * @param data       actual wavedata
     * @param format     format of wave data
     * @param samplerate sample rate of data
     */
    private WaveData(ByteBuffer data, int format, int samplerate) {
        this.data = data;
        this.format = format;
        this.samplerate = samplerate;
    }

    /**
     * Disposes the wavedata
     */
    public void dispose() {
        data.clear();
    }

    /**
     * Creates a WaveData container from the specified filename
     *
     * @param sample
     * @return WaveData containing data, or null if a failure occured
     */
    public static WaveData create(File sample) throws IOException, UnsupportedAudioFileException {
        try (final AudioInputStream ais = AudioSystem.getAudioInputStream(sample)) {
            return create(ais);
        }
    }

    /**
     *
     * @param sourceBytes a bytestream of audio
     * @param audioFormat
     * @return
     * @throws UnsupportedAudioFileException
     * @throws IllegalArgumentException
     * @throws Exception
     */
    public static byte[] getAudioDataBytes(byte[] sourceBytes, AudioFormat audioFormat) throws UnsupportedAudioFileException, IllegalArgumentException, Exception {
        if (sourceBytes == null || sourceBytes.length == 0 || audioFormat == null) {
            throw new IllegalArgumentException("Illegal Argument passed to this method");
        }

        try (final ByteArrayInputStream bais = new ByteArrayInputStream(sourceBytes);
             final AudioInputStream sourceAIS = AudioSystem.getAudioInputStream(bais)
        ) {

            AudioFormat sourceFormat = sourceAIS.getFormat();
            AudioFormat convertFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sourceFormat.getSampleRate(),
                    16,
                    sourceFormat.getChannels(),
                    sourceFormat.getChannels() * 2,
                    sourceFormat.getSampleRate(),
                    false
            );

            try (final AudioInputStream convert1AIS = AudioSystem.getAudioInputStream(convertFormat, sourceAIS);
                 final AudioInputStream convert2AIS = AudioSystem.getAudioInputStream(audioFormat, convert1AIS);
                 final ByteArrayOutputStream baos = new ByteArrayOutputStream()
            ) {

                byte[] buffer = new byte[8192];
                while (true) {
                    int readCount = convert2AIS.read(buffer, 0, buffer.length);
                    if (readCount == -1) {
                        break;
                    }
                    baos.write(buffer, 0, readCount);
                }
                return baos.toByteArray();
            }
        }
    }

    /**
     * Creates a WaveData container from the specified bytes
     *
     * @param buffer array of bytes containing the complete wave file
     * @return WaveData containing data, or null if a failure occured
     */
    public static WaveData create(byte[] buffer) throws IOException, UnsupportedAudioFileException {
        final AudioInputStream ain = AudioSystem.getAudioInputStream(
                new BufferedInputStream(new ByteArrayInputStream(buffer)));
        return create(ain);
    }

    /**
     * Creates a WaveData container from the specified stream
     *
     * @param ais AudioInputStream to read from
     * @return WaveData containing data, or null if a failure occured
     */
    public static WaveData create(AudioInputStream ais) throws IOException, UnsupportedAudioFileException {
        //get format of data
        AudioFormat audioformat = ais.getFormat();

        // get channels
        int channels = 0;
        if (audioformat.getChannels() == 1) {
            if (audioformat.getSampleSizeInBits() == 8) {
                channels = AL10.AL_FORMAT_MONO8;
            } else if (audioformat.getSampleSizeInBits() == 16) {
                channels = AL10.AL_FORMAT_MONO16;
            } else {
                throw new IOException("WaveData.create(): Illegal sample size");
            }
        } else if (audioformat.getChannels() == 2) {
            if (audioformat.getSampleSizeInBits() == 8) {
                channels = AL10.AL_FORMAT_STEREO8;
            } else if (audioformat.getSampleSizeInBits() == 16) {
                channels = AL10.AL_FORMAT_STEREO16;
            } else {
                throw new IOException("WaveData.create(): Illegal sample size");
            }
        } else {
            throw new UnsupportedAudioFileException("WaveData.create(): Only mono or stereo is supported");
        }

        //read data into buffer
        byte[] buf =
                new byte[(audioformat.getChannels()
                        * (int) ais.getFrameLength()
                        * audioformat.getSampleSizeInBits())
                        / 8];
        int read = 0, total = 0;
        while ((read = ais.read(buf, total, buf.length - total)) != -1
                && total < buf.length) {
            total += read;
        }


        //insert data into bytebuffer
        ByteBuffer buffer = convertAudioBytes(buf, audioformat.getSampleSizeInBits() == 16);

        //create our result
        WaveData wavedata =
                new WaveData(buffer, channels, (int) audioformat.getSampleRate());

        //close stream
        ais.close();

        return wavedata;
    }

    private static ByteBuffer convertAudioBytes(byte[] audio_bytes, boolean two_bytes_data) {
        ByteBuffer dest = ByteBuffer.allocateDirect(audio_bytes.length);
        dest.order(ByteOrder.nativeOrder());
        ByteBuffer src = ByteBuffer.wrap(audio_bytes);
        src.order(ByteOrder.LITTLE_ENDIAN);
        if (two_bytes_data) {
            ShortBuffer dest_short = dest.asShortBuffer();
            ShortBuffer src_short = src.asShortBuffer();
            while (src_short.hasRemaining())
                dest_short.put(src_short.get());
        } else {
            while (src.hasRemaining())
                dest.put(src.get());
        }
        dest.rewind();
        return dest;
    }
}
