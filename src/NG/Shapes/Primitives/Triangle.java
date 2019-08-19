package NG.Shapes.Primitives;

import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 29-10-2017.
 */
public class Triangle extends Plane {

    /** ABRef, BCRef, CARef are three reference vectors for collision detection */
    private Vector3fc ABRef, BCRef, CARef;

    /** reserved space for collision detection */
    private Vector3f tempAlpha = new Vector3f();
    private Vector3f tempBeta = new Vector3f();
    private Vector3f cross = new Vector3f();

    /**
     * the vectors must be supplied in counterclockwise ordering
     */
    public Triangle(Vector3fc A, Vector3fc B, Vector3fc C, Vector3fc normal) {
        super(new Vector3fc[]{A, B, C}, normal);

        ABRef = B.sub(A, tempAlpha).cross(C.sub(A, tempBeta), new Vector3f());
        BCRef = C.sub(B, tempAlpha).cross(A.sub(B, tempBeta), new Vector3f());
        CARef = A.sub(C, tempAlpha).cross(B.sub(C, tempBeta), new Vector3f());
    }

    public static Triangle createTriangle(Vector3fc A, Vector3fc B, Vector3fc C, Vector3fc direction) {
        final Vector3f normal = Vectors.getNormalVector(A, B, C);

        if (normal.dot(direction) >= 0) {
            return new Triangle(A, B, C, normal);
        } else {
            normal.negate();
            return new Triangle(C, B, A, normal);
        }
    }

    /**
     * computes in optimized fashion whether the given point lies inside the triangle
     * @param hitPos a point on this plane
     * @return true if the point is within the boundaries
     */
    @Override
    protected boolean encapsulates(Vector3fc hitPos) {
        Vector3fc A = boundary[0];
        Vector3fc B = boundary[1];
        Vector3fc C = boundary[2];

        B.sub(A, tempAlpha).cross(hitPos.sub(A, tempBeta), cross);

        if (ABRef.dot(cross) >= 0) {
            C.sub(B, tempAlpha).cross(hitPos.sub(B, tempBeta), cross);
            if (BCRef.dot(cross) >= 0) {
                A.sub(C, tempAlpha).cross(hitPos.sub(C, tempBeta), cross);
                return CARef.dot(cross) >= 0;
            }
        }
        return false;
    }
}
