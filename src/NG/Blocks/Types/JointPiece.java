package NG.Blocks.Types;

import NG.Blocks.BlockSubGrid;
import NG.DataStructures.Generic.Color4f;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 19-8-2019.
 */
public class JointPiece extends AbstractPiece {
    private Vector3i axis;
    private PieceTypeJoint type;
    private BlockSubGrid subgrid;

    JointPiece(PieceTypeJoint type, Vector3ic position, int rotation, Color4f color) {
        super(position, rotation, color);
        this.type = type;
        axis = new Vector3i(type.axis);

        for (byte i = 0; i < rotation; i++) {
            //noinspection SuspiciousNameCombination
            axis.set(-axis.y, axis.x, axis.z);
        }
    }

    @Override
    public void rotateZ(boolean clockwise) {
        super.rotateZ(clockwise);
        if (clockwise) {
            //noinspection SuspiciousNameCombination
            axis.set(-axis.y, axis.x, axis.z);
        } else {
            //noinspection SuspiciousNameCombination
            axis.set(axis.y, -axis.x, axis.z);
        }
    }

    @Override
    public PieceTypeJoint getType() {
        return type;
    }

    public AbstractPiece copy() {
        return new JointPiece(type, position, rotation, color);
    }

    @Override
    public void write(DataOutputStream out, Map<PieceType, Integer> typeMap) throws IOException {
        out.writeInt(typeMap.get(type));
    }

    public JointPiece(DataInputStream in, PieceType[] typeMap) throws IOException {
        super(in);
        type = (PieceTypeJoint) typeMap[in.readInt()];
    }

    public Vector3ic getAxis() {
        return axis;
    }

    public void setSubgrid(BlockSubGrid subgrid) {
        this.subgrid = subgrid;
    }

    public BlockSubGrid getSubgrid() {
        return subgrid;
    }
}
