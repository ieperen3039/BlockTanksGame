package NG.Blocks.Types;

import NG.DataStructures.Generic.Color4f;
import NG.DataStructures.Vector3fxc;
import NG.Entities.Entity;
import NG.Entities.ForceGenerating;
import NG.Rendering.MatrixStack.MatrixStack;
import NG.Rendering.MatrixStack.SGL;
import NG.Tools.BuoyancyComputation;
import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3ic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Geert van Ieperen created on 20-8-2019.
 */
public class PropellerPiece extends AbstractPiece implements ForceGenerating {
    private PieceTypePropeller type;
    private float rotSpeed;
    private float lastDrawTime = 0;
    private float propellerRotation = 0;
    private Supplier<Float> activationSupplier;

    PropellerPiece(PieceTypePropeller type, Vector3ic position, Color4f color) {
        super(position, 0, color);
        this.type = type;
        this.rotSpeed = 1f;
    }

    @Override
    protected void drawPiece(SGL gl, Entity entity, float renderTime) {
        super.drawPiece(gl, entity, renderTime);

        float dt = renderTime - lastDrawTime;
        propellerRotation += dt * rotSpeed;
        gl.translate(type.propellerOffset);
        gl.rotateQuarter(0, 1, 0);
        gl.rotate(propellerRotation, Vectors.Z);
        lastDrawTime = renderTime;
    }

    public void setRotSpeed(float rotSpeed) {
        this.rotSpeed = rotSpeed;
    }

    public void setActivationSupplier(Supplier<Float> activationSupplier) {
        this.activationSupplier = activationSupplier;
    }

    @Override
    public AbstractPiece copy() {
        return new PropellerPiece(type, position, color);
    }

    @Override
    public PieceTypePropeller getType() {
        return type;
    }

    @Override
    public void write(DataOutputStream out, Map<PieceType, Integer> typeMap) throws IOException {
        Integer typeID = typeMap.computeIfAbsent(type, t -> typeMap.size());
        out.writeInt(typeID);
    }

    public PropellerPiece(DataInputStream in, PieceType[] typeMap) throws IOException {
        super(in);
        type = (PieceTypePropeller) typeMap[in.readInt()];
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

    @Override
    public Vector3f getForce(Vector3fxc position) {
        boolean inWater = position.z() < BuoyancyComputation.FLUID_LEVEL;
        if (!inWater) return Vectors.newZero();

        float fraction = (rotSpeed / type.maxRotSpeed) * activationSupplier.get();
        Vector3f force = new Vector3f(fraction * type.maxForce, 0, 0);
        rotateQuarters(force, rotation);

        return force;
    }
}
