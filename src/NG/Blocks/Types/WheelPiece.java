package NG.Blocks.Types;

import NG.DataStructures.Generic.Color4f;
import NG.Entities.Entity;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import org.joml.Vector3ic;

/**
 * @author Geert van Ieperen created on 20-8-2019.
 */
public class WheelPiece extends BlockPiece {
    private PieceTypeWheel type;
    private float angle;
    private float rotationSpeed = 1;

    WheelPiece(PieceTypeWheel type, Vector3ic position, Color4f color) {
        super(type, position, 0, color);
        this.type = type;
    }

    public void update(float deltaTime){
        angle += deltaTime * rotationSpeed;
    }

    @Override
    public BlockPiece copy() {
        return new WheelPiece(type, position, color);
    }

    @Override
    public void draw(SGL gl, Entity entity) {
        ShaderProgram shader = gl.getShader();
        if (shader instanceof MaterialShader) {
            ((MaterialShader) shader).setMaterial(Material.PLASTIC, color);
        }

        gl.pushMatrix();
        {
            gl.translate(
                    position.x * BLOCK_BASE,
                    position.y * BLOCK_BASE,
                    position.z * BLOCK_HEIGHT
            );
            gl.rotate(angle, 0, 0, 1);

            drawPiece(gl, entity);
        }
        gl.popMatrix();
    }
}
