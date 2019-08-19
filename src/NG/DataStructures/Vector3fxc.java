package NG.DataStructures;

import org.joml.*;

/**
 * @author Geert van Ieperen created on 4-7-2019.
 */
public interface Vector3fxc {
    /**
     * @return this vector's x component in floating-point representation
     */
    float x();

    /**
     * @return this vector's y component in floating-point representation
     */
    float y();

    /**
     * @return this vector's z component in floating-point representation
     */
    float z();

    /**
     * @return a floating-point copy of this vector
     */
    Vector3f toVector3f();

    /**
     * stores the floating-point representation of this vector in the given destination vector.
     * @param dest will hold the result
     * @return
     */
    Vector3f toVector3f(Vector3f dest);

    /**
     * @return a double-precision floating-point representation of this vector
     */
    Vector3d toVector3d();

    /**
     * Add the other vector to this vector and store the result in dest
     * @param other another vector
     * @param dest  will hold the result
     * @return dest
     */
    Vector3fx add(Vector3fxc other, Vector3fx dest);

    /**
     * Add a floating-point vector to this vector and store the result in dest
     * @param other another vector
     * @param dest  will hold the result
     * @return dest
     */
    Vector3fx add(Vector3fc other, Vector3fx dest);

    /**
     * Subtract the other vector from this vector and store the result in dest
     * @param other another vector
     * @param dest  will hold the result
     * @return dest
     */
    Vector3fx sub(Vector3fxc other, Vector3fx dest);

    /**
     * Subtract a floating-point vector from this vector and store the result in dest
     * @param other another vector
     * @param dest  will hold the result
     * @return dest
     */
    Vector3fx sub(Vector3fc other, Vector3fx dest);

    /**
     * Multiply the other vector to this vector and store the result in dest
     * @param other another vector
     * @param dest  will hold the result
     * @return dest
     */
    Vector3fx mul(Vector3fxc other, Vector3fx dest);

    /**
     * Multiply a floating-point vector to this vector and store the result in dest
     * @param other another vector
     * @param dest  will hold the result
     * @return dest
     */
    Vector3fx mul(Vector3fc other, Vector3fx dest);

    /**
     * Divides each element of this vector with its element of the other vector, and store the result in dest
     * @param other another vector
     * @param dest  will hold the result
     * @return dest
     */
    Vector3fx div(Vector3fxc other, Vector3fx dest);

    /**
     * Divides each element of this vector with its element of the given floating-point vector, and store the result in
     * dest
     * @param other another vector
     * @param dest  will hold the result
     * @return dest
     */
    Vector3fx div(Vector3fc other, Vector3fx dest);

    /**
     * linearly interpolates this vector with other
     * @param other an other fixed-point vector
     * @param fraction the fraction of interpolation from this vector to the other vector.
     * @param dest will hold the result
     * @return dest
     */
    Vector3fx lerp(Vector3fxc other, float fraction, Vector3fx dest);

    /**
     * Multiply the given 4x4 matrix <code>mat</code> with <code>this</code> and store the result in <code>dest</code>,
     * as to apply a position transformation.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>1.0</code>.
     * @param mat  the matrix to multiply this vector by
     * @param dest will hold the result
     * @return dest
     */
    Vector3fx mulPosition(Matrix4fc mat, Vector3fx dest);

    /**
     * Multiply the given 4x4 matrix <code>mat</code> with <code>this</code> and store the result in <code>dest</code>,
     * as to apply a direction transformation.
     * <p>
     * This method assumes the <code>w</code> component of <code>this</code> to be <code>0.0</code>.
     * @param mat  the matrix to multiply this vector by
     * @param dest will hold the result
     * @return dest
     */
    Vector3fx mulDirection(Matrix4fc mat, Vector3fx dest);

    /**
     * Rotates this vector {@code angle} radians around the vector given by {@code (aX, aY, aZ)} and store the result in
     * dest.
     * @param angle the angle in radians
     * @param aX    the x component of the axis
     * @param aY    the y component of the axis
     * @param aZ    the z component of the axis
     * @param dest  will hold the result
     * @return dest
     */
    Vector3fx rotateAxis(float angle, float aX, float aY, float aZ, Vector3fx dest);

    /**
     * Rotates this vector with the given quaternion and store the result in dest
     * @param rotation the rotation to apply
     * @param dest     will hold the result
     * @return dest
     */
    Vector3fx rotate(Quaternionf rotation, Vector3fx dest);

    /**
     * @return the square length of this vector.
     * <p>
     * This method is faster than {@link #length()}
     */
    double lengthSquared();

    /**
     * @return the length of this vector
     * @see #lengthSquared()
     */
    float length();

    /**
     * Normalizes this vector such that {@link #length()} returns 1, and store the result in dest
     * @param dest will hold the result
     * @return dest
     */
    Vector3fx normalize(Vector3fx dest);

    /**
     * Calculates the squared distance between this vector and the given other vector.
     * <p>
     * This method is faster than {@link #distance(Vector3fxc)}
     * @param other another vector
     * @return the result of {@code this.sub(other).lengthSquared()}
     */
    float distanceSquared(Vector3fxc other);

    /**
     * Calculates the squared distance between this vector and the given floating-point vector.
     * <p>
     * This method is faster than {@link #distance(Vector3fc)}
     * @param other a floating-point vector
     * @return the result of {@code this.sub(other).lengthSquared()}
     */
    float distanceSquared(Vector3fc other);

    /**
     * Calculates the distance between this vector and the given other vector.
     * @param other another vector
     * @return the result of {@code this.sub(other).lengthSquared()}
     */
    float distance(Vector3fxc other);

    /**
     * Calculates the squared between this vector and the given floating-point vector.
     * @param other a floating-point vector
     * @return the result of {@code this.sub(other).lengthSquared()}
     */
    float distance(Vector3fc other);

    /**
     * computes the dot (inner) product of this vector and the given other vector
     * @param other another vector
     * @return the dot product of {@code this} and {@code other}
     */
    float dot(Vector3fxc other);

    /**
     * computes the dot (inner) product of this vector and the given floating-point vector
     * @param other another floating-point vector
     * @return the dot product of {@code this} and {@code other}
     */
    float dot(Vector3fc other);

    /**
     * @param v     another vector
     * @param delta an error bound
     * @return whether this vector is equal to {@code v}, allowing the given error for each component
     */
    boolean equals(Vector3fxc v, float delta);

    /**
     * @return the bitwise representation of the x component
     */
    int xBits();

    /**
     * @return the bitwise representation of the y component
     */
    int yBits();

    /**
     * @return the bitwise representation of the z component
     */
    int zBits();
}
