package NG.Blocks.Types;

import NG.DataStructures.Generic.Color4f;
import NG.Entities.Entity;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Shapes.Shape;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3ic;

import java.util.List;

import static NG.Blocks.Types.AbstractPiece.BLOCK_SIZE;

/**
 * @author Geert van Ieperen created on 16-8-2019.
 */
public class PieceTypeBlock extends PieceType {
    public final Vector3fc realSize; // in m
    public final Shape hitbox;

    private MeshFile meshFile;
    private Mesh mesh;

    protected List<Vector3ic> connections;
    protected int femaleStart;

    public PieceTypeBlock(
            String name, String category, MeshFile file, Shape hitbox, Vector3ic size, float mass,
            List<Vector3ic> connections, int femaleStart
    ) {
        super(name, category, mass, size);
        this.meshFile = file;
        this.hitbox = hitbox;
        this.realSize = new Vector3f(size).mul(BLOCK_SIZE);
        this.connections = connections;
        this.femaleStart = femaleStart;
    }

    public void draw(SGL gl, Entity entity) {
        if (mesh == null) {
            mesh = meshFile.getMesh();
        }
        gl.render(mesh, entity);
    }

    @Override
    public PieceTypeBlock getRootType() {
        return this;
    }

    @Override
    public AbstractPiece getInstance(Vector3ic position, int zRotation, Color4f color) {
        return new BlockPiece(this, position, zRotation, color);
    }

    public List<Vector3ic> getConnections() {
        return connections;
    }

    public List<Vector3ic> getMaleConnections() {
        return connections.subList(0, femaleStart);
    }

    public List<Vector3ic> getFemaleConnections() {
        return connections.subList(femaleStart, connections.size());
    }

    @Override
    public String toString() {
        return name;
    }

}
