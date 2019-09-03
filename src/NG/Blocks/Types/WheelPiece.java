package NG.Blocks.Types;

import NG.DataStructures.Generic.Color4f;
import NG.Entities.Entity;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import org.joml.Vector3ic;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 20-8-2019.
 */
public class WheelPiece extends AbstractPiece {
    private PieceTypeWheel type;
    private float rotSpeed;

    WheelPiece(PieceTypeWheel type, Vector3ic position, Color4f color) {
        super(position, 0, color, type.getConnections().size());
        this.type = type;
//        rotSpeed = 0;
        rotSpeed = 1f;
    }

    public void setRotSpeed(float rotSpeed) {
        this.rotSpeed = rotSpeed;
    }

    @Override
    public AbstractPiece copy() {
        return new WheelPiece(type, position, color);
    }

    @Override
    public PieceTypeWheel getType() {
        return type;
    }

    @Override
    public void writeToDataStream(DataOutputStream out, Map<PieceType, Integer> typeMap) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void draw(SGL gl, Entity entity, float renderTime) {
        ShaderProgram shader = gl.getShader();
        if (shader instanceof MaterialShader) {
            ((MaterialShader) shader).setMaterial(Material.PLASTIC, color);
        }

        gl.pushMatrix();
        {
            gl.rotate(rotSpeed * renderTime, 0, 0, 1);
            drawPiece(gl, entity, renderTime);
        }
        gl.popMatrix();
    }
}
