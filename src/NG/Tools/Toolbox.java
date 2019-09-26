package NG.Tools;

import NG.DataStructures.Generic.Color4f;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Shapes.GenericShapes;
import org.joml.AABBf;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.BufferUtils;

import javax.swing.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Random;
import java.util.function.Function;
import java.util.regex.Pattern;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Geert van Ieperen on 31-1-2017. a class with various tools
 */
public final class Toolbox {

    // universal random to be used everywhere
    public static final Random random = new Random();
    public static final double PHI = 1.6180339887498948;
    public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    public static final Pattern PERIOD_MATCHER = Pattern.compile("\\.");

    private static final float ROUNDINGERROR = 1E-6F;
    private static final float CURSOR_SIZE = 0.05f;

    // a set of possible titles for error messages
    private static final String[] ERROR_MESSAGES = new String[]{
            "I Blame Menno", "You're holding it wrong", "This title is at random",
            "You can't blame me for this", "Something Happened", "Oops!", "stuff's broke lol",
            "Look at what you have done", "Please ignore the following message", "Congratulations!"
    };

    /**
     * Draws the x-axis (red), y-axis (green), z-axis (blue), and origin (yellow).
     */
    public static void drawAxisFrame(SGL gl) {
        String source = Logger.getCallingMethod(1);
        if (!Logger.callerBlacklist.contains(source)) {
            Vector3f position = gl.getPosition(Vectors.O);
            Logger.DEBUG.printFrom(2, " - draws axis frame on " + Vectors.toString(position));
            Logger.callerBlacklist.add(source);
        }

        Material mat = Material.ROUGH;
        ShaderProgram shader = gl.getShader();
        MaterialShader matShader = (diffuse, specular, reflectance) -> {};

        if (shader instanceof MaterialShader) {
            matShader = (MaterialShader) shader;
        }

        gl.pushMatrix();
        {
            matShader.setMaterial(mat, Color4f.BLUE);
            gl.render(GenericShapes.ARROW, null);

            gl.rotate((float) Math.toRadians(90), 0f, 1f, 0f);
            matShader.setMaterial(mat, Color4f.RED);
            gl.render(GenericShapes.ARROW, null);
            gl.rotate((float) Math.toRadians(-90), 1f, 0f, 0f);
            matShader.setMaterial(mat, Color4f.GREEN);
            gl.render(GenericShapes.ARROW, null);

            matShader.setMaterial(Material.ROUGH, Color4f.WHITE);
        }
        gl.popMatrix();
    }

    public static void draw3DPointer(SGL gl) {
        Material mat = Material.ROUGH;
        ShaderProgram shader = gl.getShader();
        MaterialShader matShader = (diffuse, specular, reflectance) -> {};

        if (shader instanceof MaterialShader) {
            matShader = (MaterialShader) shader;
        }

        matShader.setMaterial(mat, Color4f.BLUE);
        gl.pushMatrix();
        {
            gl.scale(1, CURSOR_SIZE, CURSOR_SIZE);
            gl.render(GenericShapes.CUBE, null);
        }
        gl.popMatrix();

        matShader.setMaterial(mat, Color4f.RED);
        gl.pushMatrix();
        {
            gl.scale(CURSOR_SIZE, 1, CURSOR_SIZE);
            gl.render(GenericShapes.CUBE, null);
        }
        gl.popMatrix();

        matShader.setMaterial(mat, Color4f.GREEN);
        gl.pushMatrix();
        {
            gl.scale(CURSOR_SIZE, CURSOR_SIZE, 1);
            gl.render(GenericShapes.CUBE, null);
        }
        matShader.setMaterial(Material.ROUGH, Color4f.WHITE);
        gl.popMatrix();
    }

    public static void drawHitboxes(SGL gl, Collection<? extends AABBf> targets) {
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        for (AABBf h : targets) {
            gl.pushMatrix();
            {
                gl.translate((h.maxX + h.minX) / 2, (h.maxY + h.minY) / 2, (h.maxZ + h.minZ) / 2);
                gl.scale((h.maxX - h.minX) / 2, (h.maxY - h.minY) / 2, (h.maxZ - h.minZ) / 2);
                gl.render(GenericShapes.CUBE, null);
            }
            gl.popMatrix();

        }

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    }

    public static void checkGLError() {
        int error;
        int i = 0;
        while ((error = glGetError()) != GL_NO_ERROR) {
            Logger.ERROR.printFrom(2, "glError " + asHex(error) + ": " + getMessage(error));
            if (++i == 10) throw new IllegalStateException("Context is probably not current for this thread");
        }
    }

    private static String getMessage(int error) {
        switch (error) {
            case GL_INVALID_ENUM:
                return "Invalid Enum";
            case GL_INVALID_VALUE:
                return "Invalid Value";
            case GL_INVALID_OPERATION:
                return "Invalid Operation";
            case GL_STACK_OVERFLOW:
                return "Stack Overflow";
            case GL_STACK_UNDERFLOW:
                return "Stack Underflow";
            case GL_OUT_OF_MEMORY:
                return "Out of Memory";
        }
        return "Unknown Error";
    }

    public static String asHex(int decimal) {
        return "0x" + Integer.toHexString(decimal).toUpperCase();
    }


    public static void checkALError() {
        checkALError("");
    }

    public static void checkALError(String args) {
        int error;
        int i = 0;
        while ((error = alGetError()) != AL_NO_ERROR) {
            Logger.WARN.printFrom(2, "alError " + asHex(error) + ": " + alGetString(error), args);
            if (++i == 10) {
                throw new IllegalStateException("Context is probably not current for this thread");
            }
        }
    }

    /**
     * call System.exit and tells who did it
     */
    public static void exitJava() {
        try {
            Logger.ERROR.newLine();
            Logger.DEBUG.printFrom(2, "Ending JVM");
            Thread.sleep(10);
            Thread.dumpStack();
            System.exit(-1);
        } catch (InterruptedException e) {
            System.exit(-1);
        }
    }

    public static boolean almostZero(float number) {
        return (((number + ROUNDINGERROR) >= 0.0f) && ((number - ROUNDINGERROR) <= 0.0f));
    }

    /**
     * performs an incremental insertion-sort on (preferably nearly-sorted) the given array. modifies items
     * @param items the array to sort
     * @param comparator the comparision function to use
     */
    public static <Type> void insertionSort(Type[] items, Comparator<Type> comparator) {
        // iterate incrementally over the array
        for (int head = 1; head < items.length; head++) {
            Type subject = items[head];

            // decrement for the right position
            int empty = head;

            while (empty > 0) {
                Type target = items[empty - 1];

                if (comparator.compare(target, subject) > 0) {
                    items[empty] = target;
                    empty--;
                } else {
                    break;
                }
            }
            items[empty] = subject;
        }
    }

    /** @return a rotation that maps the x-vector to the given direction, with up in direction of z */
    public static Quaternionf xTo(Vector3fc direction) {
        if (direction.y() == 0 && direction.z() == 0 && direction.x() < 0) {
            return new Quaternionf().rotateZ((float) Math.PI);
        }
        return new Quaternionf().rotateTo(Vectors.X, direction);
    }

    /** returns a uniformly distributed random value between val1 and val2 */
    public static float randomBetween(float val1, float val2) {
        return val1 + ((val2 - val1) * random.nextFloat());
    }

    /**
     * transforms a floating point value to an integer value, by drawing a random variable for the remainder.
     * @return an int i such that for float f, we have (f - 1 < i < f + 1) and the average return value is f.
     */
    public static int randomToInt(float value) {
        int floor = (int) value;
        if (floor == value) return floor;
        return random.nextFloat() > (value - floor) ? floor : floor + 1;
    }

    public static float instantPreserveFraction(float rotationPreserveFactor, float deltaTime) {
        return (float) (StrictMath.pow(rotationPreserveFactor, deltaTime));
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * merges a joining array into this array
     * @param host the sorted largest non-empty of the arrays to merge, entities in this array will be checked for
     *             relevance.
     * @param join the sorted other non-empty array to merge
     * @param map  maps a moving source to the value to be sorted upon
     * @return a sorted array of living entities from both host and join combined.
     */
    public static <Type, Key extends Comparable<Key>> Type[] mergeArrays(Type[] host, Type[] join, Function<Type, Key> map) {
        int hLength = host.length;
        int jLength = join.length;

        Type[] results = Arrays.copyOf(host, hLength + jLength);
        // current indices
        int hIndex = 0;
        int jIndex = 0;

        for (int i = 0; i < results.length; i++) {
            if (jIndex >= jLength) {
                results[i] = host[hIndex];
                hIndex++;

            } else if (hIndex >= hLength) {
                results[i] = join[jIndex];
                jIndex++;

            } else {
                Type hostItem = host[hIndex];
                Type joinItem = join[jIndex];

                // select the smallest
                if (map.apply(hostItem).compareTo(map.apply(joinItem)) < 0) {
                    results[i] = hostItem;
                    hIndex++;

                } else {
                    results[i] = joinItem;
                    jIndex++;
                }
            }
        }

        // loop automatically ends after at most (i = alpha.length + beta.length) iterations
        return results;
    }

    public static <Type, Key extends Comparable<Key>> int binarySearch(Type[] array, Function<Type, Key> map, Key value) {
        int low = 0;
        int high = array.length - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            Type e = array[mid];

            Key key = map.apply(e);
            if (key.compareTo(value) < 0) {
                low = mid + 1;
            } else if (key.compareTo(value) > 0) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return -(low + 1);  // key not found.
    }

    public static boolean isValidQuaternion(Quaternionf rotation) {
        return !(Float.isNaN(rotation.x) || Float.isNaN(rotation.y) || Float.isNaN(rotation.z) || Float.isNaN(rotation.w));
    }

    public static String[] toStringArray(Object[] values) {
        String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i].toString();
        }
        return result;
    }

    public static <T> T findClosest(String target, T[] options) {
        int max = 0;
        int lengthOfMax = Integer.MAX_VALUE;
        T best = null;

        for (T candidate : options) {
            String asString = candidate.toString();
            int wordLength = Math.abs(asString.length() - target.length());
            int dist = hammingDistance(target, asString);

            if (dist > max || (dist == max && wordLength < lengthOfMax)) {
                max = dist;
                lengthOfMax = wordLength;
                best = candidate;
            }
        }

        return best;
    }

    /**
     * computes the longest common substring of string a and b
     */
    // LCSLength(X[1..m], Y[1..n])
    //  C = array(0..m, 0..n)
    //  for i := 1..m
    //      for j := 1..n
    //          if X[i] = Y[j]
    //              C[i,j] := C[i-1,j-1] + 1
    //          else
    //              C[i,j] := max(C[i,j-1], C[i-1,j])
    //  return C[m,n]
    public static int hammingDistance(String a, String b) {
        int m = a.length();
        int n = b.length();
        int[][] cMat = new int[m + 1][n + 1]; // initialized at 0

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                char ca = a.charAt(i - 1);
                char cb = b.charAt(j - 1);
                if (ca == cb) {
                    cMat[i][j] = cMat[i - 1][j - 1] + 1;
                } else {
                    cMat[i][j] = Math.max(cMat[i][j - 1], cMat[i - 1][j]);
                }
            }
        }

        return cMat[m][n];
    }

    public static ByteBuffer toByteBuffer(Path path) throws IOException {
        ByteBuffer buffer;

        try (SeekableByteChannel fc = Files.newByteChannel(path)) {
            buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);
            while (fc.read(buffer) != -1) ;
        }

        buffer.flip();
        return buffer;
    }

    /**
     * @return the greatest common integer diviser of a and b.
     */
    public static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    public static float interpolate(float a, float b, float fraction) {
        return ((b - a) * fraction) + a;
    }

    public static void display(Exception e) {
        Logger.ERROR.print(e);
        int rng = random.nextInt(ERROR_MESSAGES.length);

        JOptionPane.showMessageDialog(null, e.getClass() + ":\n" + e.getMessage(), ERROR_MESSAGES[rng], JOptionPane.ERROR_MESSAGE);
    }
}
