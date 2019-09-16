package NG.Blocks.Types;

import NG.DataStructures.Generic.Color4f;
import NG.Rendering.MatrixStack.MatrixStack;
import org.joml.Vector3ic;

import java.io.DataInputStream;
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
        super(position, 0, color);
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
    public void write(DataOutputStream out, Map<PieceType, Integer> typeMap) throws IOException {
        Integer typeID = typeMap.computeIfAbsent(type, t -> typeMap.size());
        out.writeInt(typeID);
    }

    public WheelPiece(DataInputStream in, PieceType[] typeMap) throws IOException {
        super(in);
        type = (PieceTypeWheel) typeMap[in.readInt()];
        rotSpeed = 0;
    }

    @Override
    public void doLocal(MatrixStack gl, float renderTime, Runnable action) {
        gl.pushMatrix();
        {
            gl.translate(
                    position.x * BLOCK_BASE,
                    position.y * BLOCK_BASE,
                    position.z * BLOCK_HEIGHT
            );
            gl.rotate(rotSpeed * renderTime, 0, 0, 1);

            action.run();
        }
        gl.popMatrix();
    }
}
