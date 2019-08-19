package NG.DataStructures;

import org.joml.Math;
import org.joml.*;

import java.util.Locale;

/**
 * a fixed-point vector that holds three signed fixed-point numbers in Q(20, 12).
 * @author Geert van Ieperen created on 24-6-2019.
 * @see org.joml.Vector3f
 */
public class Vector3fx implements Vector3fxc {
    // should be adjusted to find an optimal MAX_FIXPOINT and MIN_FIXPOINT
    private static final int FRACTIONAL_BITS = 12;

    private static final int FACTOR = 1 << FRACTIONAL_BITS;
    private static final float FACTOR_INV = 1f / FACTOR;

    /** the minimum value any component of this vector may assume, equal to the minimum difference between two numbers */
    public static final float MIN_FIXPOINT = FACTOR_INV;

    /** the maximum value any component of this vector may assume */
    public static final double MAX_FIXPOINT = (double) (1 << (Integer.SIZE - FRACTIONAL_BITS - 1)) - MIN_FIXPOINT;

    /**
     * The components of the vector, represented as integers with fixed-point shift
     */
    private int x, y, z;

    /**
     * Create a new {@code Vector3fx} of <code>(0, 0, 0)</code>.
     */
    public Vector3fx() {
        set(0, 0, 0);
    }

    /**
     * Create a new {@code Vector3fx} that is closest to the given vector.
     * @param source a floating-point vector
     */
    public Vector3fx(Vector3fc source) {
        set(source);
    }

    /**
     * Copies the given {@code Vector3fx}
     */
    public Vector3fx(Vector3fxc source) {
        this.x = source.xBits();
        this.y = source.yBits();
        this.z = source.zBits();
    }

    public Vector3fx(float x, float y, float z) {
        set(x, y, z);
    }

    public Vector3fx(Vector3dc origin) {
        this.x = doubleToFixed(origin.x());
        this.y = doubleToFixed(origin.y());
        this.z = doubleToFixed(origin.z());
    }

    @Override
    public float x() {
        return this.x * FACTOR_INV;
    }

    @Override
    public float y() {
        return this.y * FACTOR_INV;
    }

    @Override
    public float z() {
        return this.z * FACTOR_INV;
    }

    // conversion methods

    private float fixedToFloat(int fixedPoint) {
        return fixedPoint * FACTOR_INV;
    }

    private float fixedToFloat(long fixedPoint) {
        return (float) (fixedPoint * (double) FACTOR_INV);
    }

    private int floatToFixed(float floatingPoint) throws IllegalArgumentException {
        float value = floatingPoint * FACTOR;

        if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Value " + floatingPoint + " is larger than the maximum of " + MAX_FIXPOINT);
        }

        return round(value);
    }

    private int doubleToFixed(double floatingPoint) throws IllegalArgumentException {
        double value = floatingPoint * FACTOR;

        if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Value " + floatingPoint + " is larger than the maximum of " + MAX_FIXPOINT);
        }

        return round(value);
    }

    private static int round(double value) {
        return value < 0 ? (int) (value - 0.5) : (int) (value + 0.5);
    }

    private static int round(float value) {
        return value < 0 ? (int) (value - 0.5f) : (int) (value + 0.5f);
    }

    @Override
    public Vector3f toVector3f() {
        return new Vector3f(fixedToFloat(x), fixedToFloat(y), fixedToFloat(z));
    }

    @Override
    public Vector3f toVector3f(Vector3f dest) {
        return dest.set(fixedToFloat(x), fixedToFloat(y), fixedToFloat(z));
    }

    @Override
    public Vector3d toVector3d() {
        return new Vector3d((double) x / FACTOR, (double) y / FACTOR, (double) z / FACTOR);
    }

    /** multiplies two fixed-points */
    private static int mulfx(int a, int b) {
        long ans = (long) a * b;
        long shift = ans >> FRACTIONAL_BITS;
        return (int) shift;
    }

    /**
     * multiply x with the given floating-point factor, and return the fixed-point representation
     * @return the fixed-point result of {@code x() * factor}
     */
    private int mulX(float factor) {
        return round(x * factor);
    }

    /**
     * multiply y with the given floating-point factor, and return the fixed-point representation
     * @return the fixed-point result of {@code y() * factor}
     */
    private int mulY(float factor) {
        return round(y * factor);
    }

    /**
     * multiply z with the given floating-point factor, and return the fixed-point representation
     * @return the fixed-point result of {@code z() * factor}
     */
    private int mulZ(float factor) {
        return round(z * factor);
    }

    /** divide fixed-points a by b */
    private static int divfx(int a, int b) {
        return (int) (((long) a << FRACTIONAL_BITS) / b);
    }

    /**
     * divide x by the given floating-point factor, and return the fixed-point representation
     * @return the fixed-point result of {@code x() / factor}
     */
    private int divX(float factor) {
        return round(x / factor);
    }

    /**
     * divide y by the given floating-point factor, and return the fixed-point representation
     * @return the fixed-point result of {@code y() / factor}
     */
    private int divY(float factor) {
        return round(y / factor);
    }

    /**
     * divide z by the given floating-point factor, and return the fixed-point representation
     * @return the fixed-point result of {@code z() / factor}
     */
    private int divZ(float factor) {
        return round(z / factor);
    }

    // memory methods

    /**
     * Set the x, y and z components to match the supplied vector.
     * @param v contains the values of x, y and z to set
     * @return this
     */
    public Vector3fx set(Vector3fc v) {
        set(v.x(), v.y(), v.z());
        return this;
    }

    /**
     * Set this vector to be a copy of the given vector.
     * @param v contains the values of x, y and z to set
     * @return this
     */
    public Vector3fx set(Vector3fxc v) {
        this.x = v.xBits();
        this.y = v.yBits();
        this.z = v.zBits();
        return this;
    }

    /**
     * Set the x, y and z components as given
     */
    private void set(float x, float y, float z) {
        this.x = floatToFixed(x);
        this.y = floatToFixed(y);
        this.z = floatToFixed(z);
    }

    /**
     * Set the x, y and z components to the supplied integer values.
     * @param xi the x component
     * @param yi the y component
     * @param zi the z component
     * @return this
     */
    public Vector3fx set(int xi, int yi, int zi) {
        this.x = xi * FACTOR;
        this.y = yi * FACTOR;
        this.z = zi * FACTOR;
        return this;
    }

    // arithmetic

    /**
     * Add the given vector to this vector
     * @param other another vector
     * @return this
     */
    public Vector3fx add(Vector3fx other) {
        return add(other, this);
    }

    @Override
    public Vector3fx add(Vector3fxc other, Vector3fx dest) {
        dest.x = this.x + other.xBits();
        dest.y = this.y + other.yBits();
        dest.z = this.z + other.zBits();
        return dest;
    }

    /**
     * Add the given floating point values to their respective components
     * @return this
     */
    public Vector3fx add(float x, float y, float z) {
        this.x += floatToFixed(x);
        this.y += floatToFixed(y);
        this.z += floatToFixed(z);
        return this;
    }

    /**
     * Add integer values to their respective components
     * @return this
     */
    public Vector3fx add(int x, int y, int z) {
        this.x += x * FACTOR;
        this.y += y * FACTOR;
        this.z += z * FACTOR;
        return this;
    }

    /**
     * Add a floating-point vector to this vector
     * @param other another vector
     * @return this
     */
    public Vector3fx add(Vector3fc other) {
        return add(other, this);
    }

    @Override
    public Vector3fx add(Vector3fc other, Vector3fx dest) {
        dest.x = this.x + floatToFixed(other.x());
        dest.y = this.y + floatToFixed(other.y());
        dest.z = this.z + floatToFixed(other.z());
        return dest;
    }

    /**
     * Subtract the given vector from this vector
     * @param other another vector
     * @return this
     */
    public Vector3fx sub(Vector3fxc other) {
        return sub(other, this);
    }

    @Override
    public Vector3fx sub(Vector3fxc other, Vector3fx dest) {
        dest.x = this.x - other.xBits();
        dest.y = this.y - other.yBits();
        dest.z = this.z - other.zBits();
        return dest;
    }

    /**
     * Subtract the given floating point values from their respective components
     * @return this
     */
    public Vector3fx sub(float x, float y, float z) {
        this.x -= floatToFixed(x);
        this.y -= floatToFixed(y);
        this.z -= floatToFixed(z);
        return this;
    }

    /**
     * Subtract integer values from their respective components
     * @return this
     */
    public Vector3fx sub(int x, int y, int z) {
        this.x -= x * FACTOR;
        this.y -= y * FACTOR;
        this.z -= z * FACTOR;
        return this;
    }

    /**
     * Subtract a floating-point vector from this vector
     * @param other another vector
     * @return this
     */
    public Vector3fx sub(Vector3fc other) {
        return sub(other, this);
    }

    @Override
    public Vector3fx sub(Vector3fc other, Vector3fx dest) {
        dest.x = this.x - floatToFixed(other.x());
        dest.y = this.y - floatToFixed(other.y());
        dest.z = this.z - floatToFixed(other.z());
        return dest;
    }

    /**
     * Multiply this vector with the given floating-point vector
     * @param other another vector
     * @return this
     */
    public Vector3fx mul(Vector3fx other) {
        return mul(other, this);
    }

    @Override
    public Vector3fx mul(Vector3fxc other, Vector3fx dest) {
        dest.x = mulfx(this.x, other.xBits());
        dest.y = mulfx(this.y, other.yBits());
        dest.z = mulfx(this.z, other.zBits());
        return dest;
    }

    /**
     * Multiply the components of this vector with the respective floating-point values
     * @return this
     */
    public Vector3fx mul(float x, float y, float z) {
        this.x = mulX(x);
        this.y = mulY(y);
        this.z = mulZ(z);
        return this;
    }

    /**
     * Multiplies this vector with the given vector
     * @param other another vector
     * @return this
     */
    public Vector3fx mul(Vector3fc other) {
        return mul(other, this);
    }

    @Override
    public Vector3fx mul(Vector3fc other, Vector3fx dest) {
        dest.x = mulX(other.x());
        dest.y = mulY(other.y());
        dest.z = mulZ(other.z());
        return dest;
    }

    /**
     * Divides each component of this vector by the component of the given vector
     * @param other another vector
     * @return this
     */
    public Vector3fx div(Vector3fx other) {
        return div(other, this);
    }

    @Override
    public Vector3fx div(Vector3fxc other, Vector3fx dest) {
        dest.x = divfx(this.x, other.xBits());
        dest.y = divfx(this.y, other.yBits());
        dest.z = divfx(this.z, other.zBits());
        return dest;
    }

    /**
     * Divide the components of this vector by the respective floating-point values
     * @return this
     */
    public Vector3fx div(float x, float y, float z) {
        this.x = divX(x);
        this.y = divY(y);
        this.z = divZ(z);
        return this;
    }

    /**
     * Divides each component of this vector by the component of the given floating-point vector
     * @param other another vector
     * @return this
     */
    public Vector3fx div(Vector3fc other) {
        return div(other, this);
    }

    @Override
    public Vector3fx div(Vector3fc other, Vector3fx dest) {
        dest.x = divX(other.x());
        dest.y = divY(other.y());
        dest.z = divZ(other.z());
        return dest;
    }

    /**
     * linearly interpolates this vector to the other vector
     * @param other an other fixed-point vector
     * @param fraction the fraction of interpolation from this vector to the other vector.
     * @return this
     */
    public Vector3fx lerp(Vector3fxc other, float fraction) {
        return lerp(other, fraction, this);
    }

    @Override
    public Vector3fx lerp(Vector3fxc other, float fraction, Vector3fx dest) {
        dest.x += (int) ((other.xBits() - x) * fraction);
        dest.y += (int) ((other.yBits() - y) * fraction);
        dest.z += (int) ((other.zBits() - z) * fraction);
        return dest;
    }

    // transformations

    /**
     * Multiply <code>this</code> with the given 4x4 matrix <code>mat</code>
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>1.0</code>.
     * @param mat the matrix to multiply this vector by
     * @return this
     */
    public Vector3fx mulPosition(Matrix4fc mat) {
        return mulPosition(mat, this);
    }

    @Override
    public Vector3fx mulPosition(Matrix4fc mat, Vector3fx dest) {
        int rx = mulX(mat.m00()) + mulY(mat.m10()) + mulZ(mat.m20()) + floatToFixed(mat.m30());
        int ry = mulX(mat.m01()) + mulY(mat.m11()) + mulZ(mat.m21()) + floatToFixed(mat.m31());
        int rz = mulX(mat.m02()) + mulY(mat.m12()) + mulZ(mat.m22()) + floatToFixed(mat.m32());
        dest.x = rx;
        dest.y = ry;
        dest.z = rz;
        return dest;
    }

    /**
     * Multiply <code>this</code> with the given 4x4 matrix <code>mat</code> as to apply a position transformation. as
     * to apply a direction transformation.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>0.0</code>.
     * @param mat the matrix to multiply this vector by
     * @return this
     */
    public Vector3fx mulDirection(Matrix4fc mat) {
        return mulDirection(mat, this);
    }

    @Override
    public Vector3fx mulDirection(Matrix4fc mat, Vector3fx dest) {
        int rx = mulX(mat.m00()) + mulY(mat.m10()) + mulZ(mat.m20());
        int ry = mulX(mat.m01()) + mulY(mat.m11()) + mulZ(mat.m21());
        int rz = mulX(mat.m02()) + mulY(mat.m12()) + mulZ(mat.m22());
        dest.x = rx;
        dest.y = ry;
        dest.z = rz;
        return dest;
    }

    /**
     * Rotates this vector {@code angle} radians around the vector given by {@code (aX, aY, aZ)}.
     * @param angle the angle in radians
     * @param aX    the x component of the axis
     * @param aY    the y component of the axis
     * @param aZ    the z component of the axis
     * @return this
     */
    public Vector3fx rotateAxis(float angle, float aX, float aY, float aZ) {
        return rotateAxis(angle, aX, aY, aZ, this);
    }

    @Override
    public Vector3fx rotateAxis(float angle, float aX, float aY, float aZ, Vector3fx dest) {
        float hangle = angle * 0.5f;
        float sinAngle = (float) Math.sin(hangle);
        float qx = aX * sinAngle, qy = aY * sinAngle, qz = aZ * sinAngle;
        float qw = (float) Math.cosFromSin(sinAngle, hangle);
        float w2 = qw * qw, x2 = qx * qx, y2 = qy * qy, z2 = qz * qz, zw = qz * qw;
        float xy = qx * qy, xz = qx * qz, yw = qy * qw, yz = qy * qz, xw = qx * qw;
        int nx = mulX(w2 + x2 - z2 - y2) + mulY(-zw + xy - zw + xy) + mulZ(yw + xz + xz + yw);
        int ny = mulX(xy + zw + zw + xy) + mulY(y2 - z2 + w2 - x2) + mulZ(yz + yz - xw - xw);
        int nz = mulX(xz - yw + xz - yw) + mulY(yz + yz + xw + xw) + mulZ(z2 - y2 - x2 + w2);
        dest.x = nx;
        dest.y = ny;
        dest.z = nz;
        return dest;
    }

    /**
     * Rotates this vector with the given quaternion
     * @param rotation the rotation to apply
     * @return this
     */
    public Vector3fx rotate(Quaternionf rotation) {
        return rotate(rotation, this);
    }

    @Override
    public Vector3fx rotate(Quaternionf rotation, Vector3fx dest) {
        float w2 = rotation.w * rotation.w;
        float x2 = rotation.x * rotation.x;
        float y2 = rotation.y * rotation.y;
        float z2 = rotation.z * rotation.z;
        float zw = rotation.z * rotation.w, zwd = zw + zw;
        float xy = rotation.x * rotation.y, xyd = xy + xy;
        float xz = rotation.x * rotation.z, xzd = xz + xz;
        float yw = rotation.y * rotation.w, ywd = yw + yw;
        float yz = rotation.y * rotation.z, yzd = yz + yz;
        float xw = rotation.x * rotation.w, xwd = xw + xw;
        float m00 = w2 + x2 - z2 - y2;
        float m01 = xyd + zwd;
        float m02 = xzd - ywd;
        float m10 = xyd - zwd;
        float m11 = y2 - z2 + w2 - x2;
        float m12 = yzd + xwd;
        float m20 = ywd + xzd;
        float m21 = yzd - xwd;
        float m22 = z2 - y2 - x2 + w2;

        dest.x = mulX(m00) + mulY(m10) + mulZ(m20);
        dest.y = mulX(m01) + mulY(m11) + mulZ(m21);
        dest.z = mulX(m02) + mulY(m12) + mulZ(m22);

        return dest;
    }

    // other

    @Override
    public double lengthSquared() {
        long mul = (long) x * x + (long) y * y + (long) z * z;
        if (mul >= 0) { // overflow is at most 2 bits
            return (double) ((mul >> FRACTIONAL_BITS) * FACTOR_INV);
        }

        // overflow-safe version
        mul = 0;
        mul += ((long) x * x) >> FRACTIONAL_BITS;
        mul += ((long) y * y) >> FRACTIONAL_BITS;
        mul += ((long) z * z) >> FRACTIONAL_BITS;
        return ((double) mul * FACTOR_INV);
    }

    @Override
    public float length() {
        double sqrt = StrictMath.sqrt(lengthSquared());
        return (float) sqrt;
    }

    /**
     * normalize this vector
     * @return this
     */
    public Vector3fx normalize() {
        return normalize(this);
    }

    @Override
    public Vector3fx normalize(Vector3fx dest) {
        float invLength = 1.0f / length();
        dest.x = mulX(invLength);
        dest.y = mulY(invLength);
        dest.z = mulZ(invLength);
        return dest;
    }

    @Override
    public float distanceSquared(Vector3fxc other) {
        int dx = this.x - other.xBits();
        int dy = this.y - other.yBits();
        int dz = this.z - other.zBits();
        long mul = (long) dx * dx + (long) dy * dy + (long) dz * dz;
        return fixedToFloat(mul >> FRACTIONAL_BITS);
    }

    @Override
    public float distanceSquared(Vector3fc other) {
        // do calculation in floating-point
        float dx = this.x() - other.x();
        float dy = this.y() - other.y();
        float dz = this.z() - other.z();
        return dx * dx + dy * dy + dz * dz;
    }

    @Override
    public float distance(Vector3fxc other) {
        return (float) Math.sqrt(distanceSquared(other));
    }

    @Override
    public float distance(Vector3fc other) {
        return (float) Math.sqrt(distanceSquared(other));
    }

    @Override
    public float dot(Vector3fxc other) {
        return fixedToFloat(
                (((long) this.x * other.xBits()) +
                        ((long) this.y * other.yBits()) +
                        ((long) this.z * other.zBits()))
                        >> FRACTIONAL_BITS);
    }

    @Override
    public float dot(Vector3fc other) {
        // do calculation in floating-point
        return x() * other.x() + y() * other.y() + z() * other.z();
    }

    // general methods

    public String toString() {
        return String.format(Locale.US, "(%1.03f, %1.03f, %1.03f)", x(), y(), z());
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + z;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Vector3fx other = (Vector3fx) obj;
        if (x != other.x) return false;
        if (y != other.y) return false;
        if (z != other.z) return false;
        return true;
    }

    @Override
    public boolean equals(Vector3fxc v, float delta) {
        if (this == v) return true;
        if (v == null) return false;

        int dx = x - v.xBits();
        if (-delta > dx || dx > delta) return false;
        int dy = y - v.yBits();
        if (-delta > dy || dy > delta) return false;
        int dz = z - v.zBits();
        if (-delta > dz || dz > delta) return false;
        return true;
    }

    @Override
    public int xBits() {
        return x;
    }

    @Override
    public int yBits() {
        return y;
    }

    @Override
    public int zBits() {
        return z;
    }
}
