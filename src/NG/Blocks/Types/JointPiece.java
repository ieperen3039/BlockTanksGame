package NG.Blocks.Types;

import NG.Blocks.BlockSubGrid;
import NG.DataStructures.Generic.Color4f;
import org.joml.Vector3i;
import org.joml.Vector3ic;

/**
 * @author Geert van Ieperen created on 19-8-2019.
 */
public class JointPiece extends BlockPiece {
    private Vector3i axis;
    private PieceTypeJoint type;
    private BlockSubGrid subgrid;

    JointPiece(PieceTypeJoint jType, Vector3ic position, int rotation, Color4f color) {
        super(jType, position, rotation, color);
        this.type = jType;
        axis = new Vector3i(type.axis);

        for (byte i = 0; i < rotation; i++) {
            //noinspection SuspiciousNameCombination
            axis.set(-axis.y, axis.x, axis.z);
        }
    }

    @Override
    public BlockPiece copy() {
        return new JointPiece(type, position, rotation, color);
    }

    public Vector3ic getAxis() {
        return axis;
    }

    @Override
    public PieceTypeJoint getType() {
        return type;
    }

    public void setSubgrid(BlockSubGrid subgrid) {
        this.subgrid = subgrid;
    }

    public BlockSubGrid getSubgrid() {
        return subgrid;
    }
}
