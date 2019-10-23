package NG.Blocks.Types;

import NG.Blocks.BlockSubGrid;
import NG.DataStructures.Generic.Color4f;
import NG.Entities.Entity;
import NG.Entities.ForceGeneratingBlock;
import NG.Rendering.MatrixStack.SGL;
import NG.Shapes.Shape;
import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3ic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 20-8-2019.
 */
public class PropellerPiece extends AbstractPiece implements ForceGeneratingBlock {
    private PieceTypePropeller type;
    private float rotSpeed;
    private float lastDrawTime = 0;
    private float propellerRotation = 0;

    PropellerPiece(PieceTypePropeller type, Vector3ic position, Color4f color, int rotation) {
        super(position, rotation, color);
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
        gl.render(type.getPropellerMesh(), entity);
        lastDrawTime = renderTime;
    }

    @Override
    public AbstractPiece copy() {
        return new PropellerPiece(type, position, color, rotation);
    }

    @Override
    public PieceTypeBlock getBaseType() {
        return type.axisPiece;
    }

    @Override
    public void write(DataOutputStream out, Map<PieceType, Integer> typeMap) throws IOException {
        Integer typeID = typeMap.computeIfAbsent(type, t -> typeMap.size());
        out.writeInt(typeID);
    }

    public PropellerPiece(DataInputStream in, PieceType[] typeMap) throws IOException {
        super(in);
        type = (PieceTypePropeller) typeMap[in.readInt()];
        rotSpeed = 1;
    }

    @Override
    public void update(float gameTime, float deltaTime, float activation) {
//        rotSpeed = Toolbox.interpolate(rotSpeed, type.maxRotSpeed * activation, 0.9f);
        rotSpeed = type.maxRotSpeed * activation;
    }

    @Override
    public float getForce() {
        return (rotSpeed / type.maxRotSpeed) * type.maxForce;
    }

    @Override
    public Vector3f getDirection(BlockSubGrid grid) {
        return new Vector3f(-1, 0, 0).rotate(getStructureRotation(grid));
    }

    @Override
    public Shape getShape() {
        return type.axisPiece.hitbox;
    }
}
