package NG.CollisionDetection;

import NG.Rendering.MatrixStack.MatrixStack;
import NG.Tools.Vectors;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * A collision resulting from a ray and a plane.
 * @author Geert van Ieperen created on 7-11-2017.
 */
public class Collision implements Comparable<Collision> {
    public static final Collision NONE = new Collision(Float.POSITIVE_INFINITY, Vectors.O, Vectors.O);
    public static final Collision SCALAR_ONE = new Collision(1, Vectors.O, Vectors.O);

    private final float scalar;
    private final Vector3fc shapeLocalNormal;
    private final Vector3fc shapeLocalHitPos;

    private Vector3f hitPos;
    private Vector3f normal;

    public Collision(float scalar, Vector3fc normal, Vector3fc hitPos) {
        this.scalar = scalar;
        this.shapeLocalNormal = normal;
        this.shapeLocalHitPos = hitPos;
    }

    private Collision(Collision cause){
        scalar = cause.scalar;
        shapeLocalHitPos = null;
        shapeLocalNormal = null;
        hitPos = cause.hitPos;
        normal = new Vector3f(cause.normal).negate();
    }

    /**
     * convert the values of the collision to global values, using the provided transformation matrix
     * @param matrix     the matrix state as how this collision was created.
     */
    public void convertToGlobal(MatrixStack matrix) {
        if (!isCollision()) return;

        hitPos = matrix.getPosition(shapeLocalHitPos);
        normal = matrix.getDirection(shapeLocalNormal);
        normal.normalize();
    }

    /**
     * convert the values of the collision to global values, using the provided transformation matrix
     * @param matrix     the matrix state as how this collision was created.
     */
    public void convertToGlobal(Matrix4fc matrix) {
        if (!isCollision()) return;

        hitPos = new Vector3f(shapeLocalHitPos).mulPosition(matrix);
        normal = new Vector3f(shapeLocalNormal).mulDirection(matrix);
        normal.normalize();
    }

    public boolean isCollision() {
        return Vectors.isScalable(shapeLocalNormal);
    }

    /**
     * @param c another collision
     * @return a positive integer if this is later then c, a negative integer if this is earlier than c, or 0 if they
     *         are at the same moment
     */
    @Override
    public int compareTo(Collision c) {
        return (c == null) ? 1 : Float.compare(scalar, c.scalar);
    }

    public boolean isEarlierThan(Collision other) {
        return isCollision() && compareTo(other) < 0;
    }

    public Vector3fc getHitPos() {
        return hitPos;
    }

    public Vector3fc getNormal() {
        return normal;
    }

    public float getScalar() {
        return scalar;
    }

    public Collision getInverse() {
        return new Collision(this);
    }
}
