package NG.Blocks.Types;

import NG.Blocks.BlockSubGrid;
import NG.DataStructures.Generic.Color4f;
import NG.Entities.Entity;
import NG.Rendering.MatrixStack.SGL;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

/**
 * @author Geert van Ieperen created on 19-8-2019.
 */
public class JointPiece extends BlockPiece {
    private BlockSubGrid subgrid;
    private float angle;
    private Vector3fc axis;
    private PieceTypeJoint type;

    JointPiece(PieceTypeJoint jType, Vector3ic position, int rotation, Color4f color) {
        super(jType, position, rotation, color);
        this.type = jType;
        subgrid = new BlockSubGrid();

        subgrid.add(type.headPiece, new Vector3i(), color);
        axis = new Vector3f(type.axis);
        angle = type.minAngle;
    }

    public BlockSubGrid getSubgrid() {
        return subgrid;
    }

    /**
     * @param angle angle that this joint makes in radians
     */
    public void setAngle(float angle) {
        angle = angle % (float) (2 * Math.PI); // cull on 2pi
        this.angle = Math.max(type.minAngle, Math.min(type.maxAngle, angle));
    }

    @Override
    protected void drawPiece(SGL gl, Entity entity) {
        super.drawPiece(gl, entity);

        gl.pushMatrix();
        {
            gl.translate(type.jointOffset);
            gl.rotate(axis, angle);
            gl.translate(type.headOffset);

            subgrid.draw(gl, entity);
        }
        gl.popMatrix();
    }

    @Override
    public BlockPiece copy() {
        return new JointPiece(type, position, rotation, color);
    }
}
