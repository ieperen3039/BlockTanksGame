package NG.Blocks;

import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Shapes.Shape;
import org.joml.Vector3ic;

import java.util.List;

/**
 * @author Geert van Ieperen created on 16-8-2019.
 */
public class BlockType {
    public final String name;
    public final Shape hitbox;
    public final Vector3ic size;
    public final float mass;

    private Mesh mesh;
    private MeshFile meshFile;

    private List<Vector3ic> connections;
    private int femaleStart;

    public BlockType(
            String name, MeshFile file, Shape hitbox, Vector3ic size, float mass, List<Vector3ic> connections,
            int femaleStart
    ) {
        this.name = name;
        this.meshFile = file;
        this.hitbox = hitbox;
        this.size = size;
        this.mass = mass;
        this.connections = connections;
        this.femaleStart = femaleStart;
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

    public Mesh getMesh() {
        if (mesh == null) {
            mesh = meshFile.getMesh();
            meshFile = null;
        }
        return mesh;
    }
}
