package NG.CollisionDetection;

import NG.Rendering.MatrixStack.MatrixStack;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * A collision resulting from a ray and a plane.
 * @author Geert van Ieperen created on 7-11-2017.
 */
public class Collision implements Comparable<Collision> {
    public static final Collision NONE = new Collision(Float.POSITIVE_INFINITY);
    public static final Collision SCALAR_ONE = new Collision(1);

    private final float collisionTime;
    private final Vector3fc shapeLocalNormal;
    private final Vector3fc shapeLocalHitPos;
    private final boolean isCollision;

    private Vector3f hitPos;
    private Vector3f normal;

    /**
     * a new collision, colliding a plane on the given local position and local normal. To get the global position and normal, use {@link #convertToGlobal(MatrixStack)}
     * @param collisionTime a value indicating when this collision happens.
     * @param normal the local normal of the collision
     * @param hitPos the local position of the collision
     */
    public Collision(float collisionTime, Vector3fc normal, Vector3fc hitPos) {
        this.collisionTime = collisionTime;
        this.shapeLocalNormal = normal;
        this.shapeLocalHitPos = hitPos;
        this.isCollision = true;
    }

    private Collision(Collision cause){
        isCollision = cause.isCollision;
        collisionTime = cause.collisionTime;
        shapeLocalHitPos = null;
        shapeLocalNormal = null;
        hitPos = cause.hitPos;
        normal = new Vector3f(cause.normal).negate();
    }

    private Collision(float collisionTime) {
        this.collisionTime = collisionTime;
        shapeLocalHitPos = null;
        shapeLocalNormal = null;
        isCollision = false;
    }

    /**
     * convert the values of the collision to global values, using the provided transformation matrix
     * @param matrix     the matrix state as how this collision was created.
     */
    public void convertToGlobal(MatrixStack matrix) {
        if (!isCollision) return;

        hitPos = matrix.getPosition(shapeLocalHitPos);
        normal = matrix.getDirection(shapeLocalNormal);
        normal.normalize();
    }

    /**
     * convert the values of the collision to global values, using the provided transformation matrix
     * @param matrix     the matrix state as how this collision was created.
     */
    public void convertToGlobal(Matrix4fc matrix) {
        if (!isCollision) return;

        hitPos = new Vector3f(shapeLocalHitPos).mulPosition(matrix);
        normal = new Vector3f(shapeLocalNormal).mulDirection(matrix);
        normal.normalize();
    }

    public boolean isCollision() {
        return isCollision;
    }

    /**
     * @param c another collision
     * @return a positive integer if this is later then c, a negative integer if this is earlier than c, or 0 if they
     *         are at the same moment
     */
    @Override
    public int compareTo(Collision c) {
        return (c == null) ? 1 : Float.compare(collisionTime, c.collisionTime);
    }

    public boolean isEarlierThan(Collision other) {
        return isCollision && compareTo(other) < 0;
    }

    public Vector3fc getHitPos() {
        return hitPos;
    }

    public Vector3fc getNormal() {
        return normal;
    }

    public float getCollisionTime() {
        return collisionTime;
    }

    public Collision getInverse() {
        return new Collision(this);
    }
}
