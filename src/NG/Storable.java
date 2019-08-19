package NG;

import NG.Tools.Logger;
import NG.Tools.Toolbox;
import org.joml.*;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * An Object of this type can be written to file using a {@link DataOutputStream} and read from that file using a {@link
 * DataInputStream}. This object must have a public constructor that accepts a {@link DataInputStream} object as only parameter.
 * Implementing classes may not be inner classes (until a workaround has been found)
 * @author Geert van Ieperen. Created on 28-9-2018.
 */
public interface Storable {
    /**
     * writes all the state data of this object to the given output. This object can be recovered by calling the
     * appropriate constructor that specifies to be the inverse of this method.
     * @param out any stream that reliably transfers data
     * @throws IOException if an I/O error occurs while writing data to the stream.
     * @implNote The data must be written in exactly the same order as the specified constructor reads.
     * @see #write(DataOutputStream, Storable)
     * @see #read(DataInputStream, Class)
     */
    void writeToDataStream(DataOutputStream out) throws IOException;

    /**
     * Writes the given object to the given data stream. The object must have a constructor that accepts exactly a
     * {@link DataInputStream} as only parameter. The object can be restored with a call to {@link #read(DataInputStream, Class)}
     * @param out    the data stream where to put the data to
     * @param object the object to store
     * @throws IOException if an I/O error occurs while writing data to the stream.
     */
    static void write(DataOutputStream out, Storable object) throws IOException {
        if (object instanceof Enum) {
            writeEnum(out, (Enum) object);
            return;
        }

        writeClass(out, object.getClass());
        object.writeToDataStream(out);
    }

    /**
     * reads an object from the input stream, constructs it using a constructor that takes a {@link DataInputStream} object as
     * only argument.
     * @param in       a stream where this object has been written to.
     * @param expected the class that is expected to be read from the stream.
     * @throws IOException        if something goes wrong with reading the data from the input stream.
     * @throws ClassCastException if the read class instance can't be cast to T
     * @implNote An object written to a stream using {@link #writeToDataStream(DataOutputStream)} should be {@link
     * Object#equals(Object) equal} to the object returned by this method.
     * @see #write(DataOutputStream, Storable)
     */
    static <T> T read(DataInputStream in, Class<T> expected) throws IOException, ClassNotFoundException {
        if (expected.isEnum()) {
            //noinspection unchecked
            return (T) readEnum(in, expected.asSubclass(Enum.class));
        }

        Class<? extends T> foundClass = null;

        // collect classes of arguments
        try {
            foundClass = readClass(in, expected);

            // find and call constructor
            Constructor<? extends T> constructor = foundClass.getDeclaredConstructor(DataInputStream.class);
            return constructor.newInstance(in);

        } catch (InstantiationException | IllegalAccessException ex) {
            throw new IOException("Could not access the constructor of type " + foundClass, ex);

        } catch (InvocationTargetException ex) {
            String exception = ex.getCause().getClass().getSimpleName();
            throw new IOException("Initializer caused an " + exception, ex.getCause());

        } catch (NoSuchMethodException ex) {
            throw new IOException(foundClass + " has no constructor that accepts as argument a DataInputStream class", ex);

        }
    }

    static void writeEnum(DataOutputStream out, Enum object) throws IOException {
        out.writeUTF(object.toString());
    }

    static <T extends Enum> T readEnum(DataInputStream in, Class<T> expected) throws IOException {
        String enumName = in.readUTF();
        T[] constants = expected.getEnumConstants();
        if (constants == null) throw new IllegalArgumentException(expected + " is not an Enum class");
        return Toolbox.findClosest(enumName, constants);
    }

    /**
     * writes a class object to file by writing package and name as a string.
     * @see #readClass(DataInputStream, Class)
     */
    static void writeClass(DataOutputStream out, Class<?> theClass) throws IOException {
        String name = theClass.getName();
        out.writeUTF(name);
    }

    /**
     * reads a class object from the data stream
     * @param in       the data stream
     * @param expected a superclass of the expected read class. This is used to cast the return value to the desired
     *                 class.
     * @param <T>      The type of the expected class
     * @return a subclass of the expected class, as it was written to the stream.
     * @throws IOException            if the next data on the stream does not represent an UTF string
     * @throws IOException            if an exception occurs while reading the stream
     * @throws ClassNotFoundException if the found class can not be cast to T
     */
    static <T> Class<? extends T> readClass(DataInputStream in, Class<T> expected)
            throws IOException, ClassNotFoundException {
        String className = in.readUTF();
        Class<?> foundClass = Class.forName(className);

        if (expected.isAssignableFrom(foundClass)) {
            //noinspection unchecked
            return (Class<? extends T>) foundClass;

        } else {
            throw new ClassCastException("Found " + foundClass + " which does not implement or override " + expected);
        }

    }

    static void writeVector3f(DataOutputStream out, Vector3fc vec) throws IOException {
        out.writeFloat(vec.x());
        out.writeFloat(vec.y());
        out.writeFloat(vec.z());
    }

    static Vector3f readVector3f(DataInputStream in) throws IOException {
        return new Vector3f(
                in.readFloat(),
                in.readFloat(),
                in.readFloat()
        );
    }

    static void writeVector4f(DataOutputStream out, Vector4fc vec) throws IOException {
        out.writeFloat(vec.x());
        out.writeFloat(vec.y());
        out.writeFloat(vec.z());
        out.writeFloat(vec.w());
    }

    static Vector4f readVector4f(DataInputStream in) throws IOException {
        return new Vector4f(
                in.readFloat(),
                in.readFloat(),
                in.readFloat(),
                in.readFloat()
        );
    }

    static void writeQuaternionf(DataOutputStream out, Quaternionfc vec) throws IOException {
        out.writeFloat(vec.x());
        out.writeFloat(vec.y());
        out.writeFloat(vec.z());
        out.writeFloat(vec.w());
    }

    static Quaternionf readQuaternionf(DataInputStream in) throws IOException {
        return new Quaternionf(
                in.readFloat(),
                in.readFloat(),
                in.readFloat(),
                in.readFloat()
        );
    }

    static void writeCollection(DataOutputStream out, Collection<? extends Storable> box) throws IOException {
        out.writeInt(box.size());
        for (Storable s : box) {
            Storable.write(out, s);
        }
    }

    /**
     * reads a collection from the stream, and stores it in an {@link ArrayList}
     * @param in       the data input stream
     * @param expected the class of the elements
     * @param <T>      the actual type of the elements
     * @return an {@link ArrayList} with the values read from the input stream
     * @throws IOException            if an exception occurs while reading the stream
     * @throws ClassNotFoundException if any element of the map has an unknown or not-loaded class
     * @throws ClassCastException     if any of the elements can not be cast to T
     * @see #writeCollection(DataOutputStream, Collection)
     */
    static <T> List<T> readCollection(DataInputStream in, Class<T> expected)
            throws IOException, ClassNotFoundException {
        int size = in.readInt();
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            T entity = Storable.read(in, expected);
            list.add(entity);
        }
        return list;
    }

    static void writeMatrix4f(DataOutputStream out, Matrix4fc mat) throws IOException {
        byte[] bytes = new byte[Float.BYTES * 16];

        float[] array = new float[16];
        ByteBuffer.wrap(bytes).asFloatBuffer().put(mat.get(array));

        out.write(bytes);
    }

    static Matrix4f readMatrix4f(DataInputStream in) throws IOException {
        byte[] bytes = new byte[Float.BYTES * 16];
        in.readFully(bytes);

        float[] array = new float[16];
        ByteBuffer.wrap(bytes).asFloatBuffer().get(array);

        Matrix4f mat = new Matrix4f();
        mat.set(array).determineProperties();

        return mat;
    }

    static <K extends Storable, V extends Storable> void writeMap(DataOutputStream out, Map<K, V> map)
            throws IOException {
        out.writeInt(map.size());
        Set<K> keySet = map.keySet();

        for (K key : keySet) {
            Storable.write(out, key);
            Storable.write(out, map.get(key));
        }
    }

    /**
     * reads a map from the stream and stores it in a {@link HashMap}. If the keyClass is an {@link Enum}, an {@link
     * EnumMap} is returned instead.
     * @param in         the data input stream
     * @param keyClass   the class of the keys of the map
     * @param valueClass the class of the values in the map
     * @param <K>        the type of the keys
     * @param <V>        the type of the values
     * @return a {@link HashMap} or {@link EnumMap} with the values read from the stream
     * @throws IOException            if an exception occurs while reading the stream
     * @throws ClassNotFoundException if any element of the map has an unknown or not-loaded class
     * @throws ClassCastException     if any of the keys can not be cast to K, or any of the values can not be cast to
     *                                V
     * @see #writeMap(DataOutputStream, Map)
     */
    static <K, V> Map<K, V> readMap(DataInputStream in, Class<K> keyClass, Class<V> valueClass)
            throws IOException, ClassNotFoundException {

        int size = in.readInt();
        Map<K, V> map = new HashMap<>(size);


        for (int i = 0; i < size; i++) {
            K key = Storable.read(in, keyClass);
            V value = Storable.read(in, valueClass);
            map.put(key, value);
        }

        if (keyClass.isEnum()) {
            //noinspection unchecked
            Map<? extends Enum, V> enumMap = (Map<? extends Enum, V>) map;
            //noinspection unchecked
            return new EnumMap<>(enumMap);

        } else {
            return map;
        }
    }

    /**
     * Writes this object to a file, which can be recovered with either {@link #readFromFile(File, Class)} or {@link
     * #read(DataInputStream, Class)}.
     * @param file the file to write to, existing or not.
     */
    default void writeToFile(File file) {
        try (OutputStream fileStream = new FileOutputStream(file)) {
            DataOutputStream out = new DataOutputStream(fileStream);
            Logger.DEBUG.print("Writing " + file, this);
            write(out, this);

        } catch (IOException ex) {
            Logger.ERROR.print(ex);
        }
    }

    /**
     * reads an object from file, returning null if any exception occurs.
     * @param file     the file to read
     * @param expected the class that is expected to be in the file
     * @param <T>      the type of the returned object
     * @return the object from the file, cast to T
     * @throws IOException if anything goes wrong while reading the file
     * @see #readFromFileRequired(File, Class)
     */
    static <T> T readFromFile(File file, Class<T> expected) throws IOException, ClassNotFoundException {
        try (InputStream fileStream = new FileInputStream(file)) {
            DataInputStream in = new DataInputStream(fileStream);
            return read(in, expected);
        }
    }

    /** the version of {@link #readFromFile(File, Class)} that throws an Runtime exception when an error occurs */
    static <T> T readFromFileRequired(File file, Class<T> expected) {
        try {
            return readFromFile(file, expected);

        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException("Could not load required file", ex);
        }
    }

    /**
     * packs the given element in a storage box and writes it to the stream. This ensures that reading the stream is
     * protected against crashes, at the cost of temporary overhead while writing.
     * @param out     the output stream
     * @param element the element to write to the stream
     * @throws IOException signals that anything went wrong. The output stream is untouched when this happens.
     */
    static void writeSafe(DataOutputStream out, Storable element) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(256);
        DataOutputStream wrapOut = new DataOutputStream(buffer);

        write(wrapOut, element);
        wrapOut.close();

        out.writeInt(buffer.size());
        buffer.writeTo(out);
    }

    /**
     * reads and returns the contents of a storage box from the output stream. Upon failure, return null and the stream
     * is skipped to the byte right after this box.
     * @param in       the input stream
     * @param expected the expected class of the contents
     * @return the contents of this storage box
     * @throws IOException only if the mandatory operations of this box fail. No guarantee can be made about the state
     *                     of the input stream when this is thrown.
     */
    static <T> T readSafe(DataInputStream in, Class<T> expected) throws IOException {
        int bits = in.readInt();
        in.mark(bits);

        try {
            return read(in, expected);

        } catch (IOException | ClassNotFoundException e) {
            in.reset();
            long skip = in.skip(bits);
            assert skip == bits;
            return null;
        }
    }
}
