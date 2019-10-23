package NG.Blocks.Types;

import NG.DataStructures.Generic.Color4f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

/**
 * @author Geert van Ieperen created on 20-8-2019.
 */
public class PieceTypeJoint extends PieceType {
    public final PieceTypeBlock bottomPiece;
    public final PieceType headPiece;
    public final Vector3ic axis;
    public final Vector3fc jointOffset; // this to joint
    public final Vector3fc headOffset; // joint to head
    public final boolean hasAngleLimit;
    public final float minAngle;
    public final float maxAngle;

    public PieceTypeJoint(
            String name, String category, PieceTypeBlock bottomPiece, PieceType topPiece, char axis, Vector3fc jointOffset,
            Vector3fc headOffset, boolean hasAngleLimit, float minAngle, float maxAngle
    ) {
        super(name, category, bottomPiece.mass + topPiece.mass, bottomPiece.dimensions);
        this.bottomPiece = bottomPiece;
        this.headPiece = topPiece;
        this.axis = new Vector3i(
                (axis == 'x') ? 1 : 0,
                (axis == 'y') ? 1 : 0,
                (axis == 'z') ? 1 : 0
        );
        this.jointOffset = jointOffset;
        this.headOffset = headOffset;
        this.hasAngleLimit = hasAngleLimit;
        this.minAngle = minAngle;
        this.maxAngle = maxAngle;
    }

    @Override
    public PieceTypeBlock getRootType() {
        return bottomPiece;
    }

    @Override
    public AbstractPiece getInstance(Vector3ic position, int zRotation, Color4f color) {
        return new JointPiece(this, position, 0, color);
    }
}
