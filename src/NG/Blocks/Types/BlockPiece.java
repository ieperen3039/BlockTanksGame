package NG.Blocks.Types;

import NG.CollisionDetection.Collision;
import NG.DataStructures.Generic.AABBi;
import NG.DataStructures.Generic.Color4f;
import NG.Entities.Entity;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Tools.Directory;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * @author Geert van Ieperen created on 14-8-2019.
 */
public class BlockPiece {
    public static final boolean RENDER_STUDS = true;
    private static final MeshFile STUD = MeshFile.loadFileRequired(Directory.meshes.getPath("stud.ply"));
    private static Mesh STUD_MESH = null;

    /* size of a 1x1x1 block, base in meters, scaled by 100 */
    public static final float BLOCK_BASE = 0.8f;
    public static final float BLOCK_HEIGHT = 0.32f;
    protected final PieceType type;

    private final BlockPiece[] connections;
    protected final Vector3i position;
    protected byte rotation;
    public Color4f color;

    BlockPiece(PieceType type, Vector3ic position, int zRotation, Color4f color) {
        this.type = type;
        this.position = new Vector3i(position);
        this.rotation = (byte) zRotation;
        this.connections = new BlockPiece[type.getConnections().size()];
        this.color = color;
    }

    /**
     * draw this block
     */
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
            gl.rotateQuarter(0, 0, rotation);

            // first render studs to preserve color (regarding sub-grids)
            if (RENDER_STUDS) {
                if (STUD_MESH == null) STUD_MESH = STUD.getMesh();

                gl.translate(-BLOCK_BASE / 2, -BLOCK_BASE / 2, 0);
                for (Vector3ic conn : type.getMaleConnections()) {
                    gl.pushMatrix();
                    gl.translate(conn.x() * BLOCK_BASE, conn.y() * BLOCK_BASE, conn.z() * BLOCK_HEIGHT);
                    gl.render(STUD_MESH, entity);
                    gl.popMatrix();
                }
                gl.translate(BLOCK_BASE / 2, BLOCK_BASE / 2, 0);
            }

            drawPiece(gl, entity);
        }
        gl.popMatrix();
    }

    /**
     * Draw the actual element. Overriding classes can use this to add additional details
     * @param gl the gl object, positioned and rotated as this block
     * @param entity the entity this block is part of
     */
    protected void drawPiece(SGL gl, Entity entity) {
        type.draw(gl, entity);
    }

    /**
     * @return all connected blocks.
     */
    public Collection<BlockPiece> getConnected() {
        return Arrays.asList(connections);
    }

    public void rotateZ(boolean up) {
        rotation = (byte) Math.floorMod(rotation + (up ? 1 : -1), 4);
    }

    public void setPosition(Vector3ic position) {
        this.position.set(position);
    }

    public Vector3ic getPosition() {
        return position;
    }

    public AABBi getHitBox(){
        Vector3i travel = new Vector3i(type.size).sub(1, 1, 1);
        for (byte i = 0; i < rotation; i++) {
            //noinspection SuspiciousNameCombination
            travel.set(-travel.y, travel.x, travel.z);
        }

        return new AABBi(getPosition(), travel);
    }

    public boolean intersects(BlockPiece other) {
        return getHitBox().intersects(other.getHitBox());
    }

    public Collision getIntersection(Vector3fc origin, Vector3fc direction) {
        Vector3f lo = new Vector3f(origin);
        Vector3f ld = new Vector3f(direction);

        for (byte i = 0; i < rotation; i++) {
            //noinspection SuspiciousNameCombination
            lo.set(lo.y, -lo.x, lo.z);
            //noinspection SuspiciousNameCombination
            ld.set(ld.y, -ld.x, ld.z);
        }

        return type.hitbox.getIntersection(origin, direction);
    }

    /**
     * connects the given block at its current position to this block. If the block is not adjacent, nothing happens.
     * The given block may not intersect the current block. A similar call to {@code other.connect(this)} must be made.
     * @param other some other block
     */
    public void connect(BlockPiece other) {
        assert !intersects(other);

        forEachConnection(other, connInd -> connections[connInd] = other);
    }

    public PieceType getType() {
        return type;
    }

    public boolean canConnect(BlockPiece buildCursor) {
        boolean[] buffer = new boolean[]{false};
        forEachConnection(buildCursor, (i) -> buffer[0] = true);
        return buffer[0];
    }

    private void forEachConnection(BlockPiece other, IntConsumer action) {
        List<Vector3ic> thisConnections = type.getConnections();
        List<Vector3ic> otherConnections = other.type.getConnections();

        for (int i = 0; i < thisConnections.size(); i++) {
            Vector3ic connection = thisConnections.get(i);

            Vector3i p = new Vector3i(connection);
            for (byte j = 0; j < rotation; j++) {
                //noinspection SuspiciousNameCombination
                p.set(-p.y, p.x, p.z);
            }
            p.add(position);

            for (Vector3ic oc : otherConnections) {
                Vector3i q = new Vector3i(oc);
                for (byte j = 0; j < other.rotation; j++) {
                    //noinspection SuspiciousNameCombination
                    q.set(-q.y, q.x, q.z);
                }
                q.add(other.position);

                if (p.equals(q)) {
                    action.accept(i);
                    break; // only inner loop
                }
            }
        }
    }

    public void move(int x, int y, int z) {
        position.add(x, y, z);
    }

    @Override
    public String toString() {
        return type + " " + getHitBox();
    }

    public BlockPiece copy() {
        return new BlockPiece(type, position, rotation, color);
    }
}
