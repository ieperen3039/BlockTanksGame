package NG.Shapes.Primitives;

import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * TODO: allow quads in meshes and optimize CustomShape accordingly
 * @author Geert van Ieperen created on 29-10-2017.
 */
public class Quad extends Plane {

    /** ABRef, BCRef, CDRef, DARef are four reference vectors for collision detection */
    private Vector3fc ABRef, BCRef, CDRef, DARef;

    private Vector3f tempAlpha = new Vector3f();
    private Vector3f tempBeta = new Vector3f();

    /**
     * the vectors must be supplied in counterclockwise ordering
     */
    public Quad(Vector3fc A, Vector3fc B, Vector3fc C, Vector3fc D, Vector3fc normal) {
        super(new Vector3fc[]{A, B, C, D}, normal);

        ABRef = B.sub(A, tempAlpha).cross(D.sub(A, tempBeta), new Vector3f());
        BCRef = C.sub(B, tempAlpha).cross(A.sub(B, tempBeta), new Vector3f());
        CDRef = D.sub(C, tempAlpha).cross(B.sub(C, tempBeta), new Vector3f());
        DARef = A.sub(D, tempAlpha).cross(C.sub(D, tempBeta), new Vector3f());
    }

    public static Quad createQuad(Vector3fc A, Vector3fc B, Vector3fc C, Vector3fc D, Vector3fc direction) {
        Vector3fc currentNormal = Vectors.getNormalVector(A, B, C);

        if (currentNormal.dot(direction) >= 0) {
            return new Quad(A, B, C, D, direction);
        } else {
            return new Quad(D, C, B, A, direction);
        }
    }

    @Override
    protected boolean encapsulates(Vector3fc hitPos) {
        Vector3fc A = boundary[0];
        Vector3fc B = boundary[1];
        Vector3fc C = boundary[2];
        Vector3fc D = boundary[3];

        Vector3f cross = new Vector3f();
        B.sub(A, tempAlpha).cross(hitPos.sub(A, tempBeta), cross);

        if (ABRef.dot(cross) >= 0) {
            C.sub(B, tempAlpha).cross(hitPos.sub(B, tempBeta), cross);
            if (BCRef.dot(cross) >= 0) {
                D.sub(C, tempAlpha).cross(hitPos.sub(C, tempBeta), cross);
                if (CDRef.dot(cross) >= 0) {
                    A.sub(D, tempAlpha).cross(hitPos.sub(D, tempBeta), cross);
                    return DARef.dot(cross) >= 0;
                }
            }
        }
        return false;
    }
}
