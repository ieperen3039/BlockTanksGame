package NG.Blocks.Types;

import NG.DataStructures.Generic.Color4f;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Shapes.Shape;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.List;

/**
 * @author Geert van Ieperen created on 20-8-2019.
 */
public class PieceTypeJoint extends PieceType {
    public final PieceType headPiece;
    public final Vector3ic axis;
    public final Vector3fc jointOffset; // this to joint
    public final Vector3fc headOffset; // joint to head
    public final boolean hasAngleLimit;
    public final float minAngle;
    public final float maxAngle;

    public PieceTypeJoint(
            String name,
            MeshFile rootMesh, Shape rootBox, Vector3ic rootSize, List<Vector3ic> rootConnections, int rootFStart,
            float mass, PieceType headPiece, char axis, Vector3fc jointOffset, Vector3fc headOffset,
            boolean hasAngleLimit, float minAngle, float maxAngle
    ) {
        super(name, rootMesh, rootBox, rootSize, mass, rootConnections, rootFStart);
        this.headPiece = headPiece;
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

    public PieceTypeJoint(
            String name, PieceType bottomPiece, PieceType topPiece, char axis, Vector3fc jointOffset,
            Vector3fc headOffset,
            boolean hasAngleLimit, float minAngle, float maxAngle
    ) {
        this(
                name, bottomPiece.meshFile, bottomPiece.hitbox, bottomPiece.size,
                bottomPiece.connections, bottomPiece.femaleStart, bottomPiece.mass, topPiece, axis,
                jointOffset, headOffset, hasAngleLimit, minAngle, maxAngle
        );
    }

    @Override
    public BlockPiece getInstance(Vector3ic position, int zRotation, Color4f color) {
        return new JointPiece(this, position, 0, color);
    }
}
