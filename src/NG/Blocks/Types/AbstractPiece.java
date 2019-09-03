package NG.Blocks.Types;

import NG.Blocks.BlockSubGrid;
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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

/**
 * @author Geert van Ieperen created on 2-9-2019.
 */
public abstract class AbstractPiece {
    public static final boolean RENDER_STUDS = true;
    /* size of a 1x1x1 block, base in meters, scaled by 100 */
    public static final float BLOCK_BASE = 0.8f;
    public static final float BLOCK_HEIGHT = 0.32f;
    private static final MeshFile STUD = MeshFile.loadFileRequired(Directory.meshes.getPath("stud.ply"));
    protected final Vector3i position;
    protected final AbstractPiece[] connections;
    private AABBi hitbox = null;
    private static Mesh STUD_MESH = null;
    public Color4f color;
    protected byte rotation;

    AbstractPiece(Vector3ic position, int zRotation, Color4f color, int nrOfConnections) {
        this.position = new Vector3i(position);
        this.rotation = (byte) zRotation;
        this.connections = new AbstractPiece[nrOfConnections];
        this.color = color;
    }

    /**
     * draw this block
     */
    public void draw(SGL gl, Entity entity, float renderTime) {
        if (color.alpha == 0) return;

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
                for (Vector3ic conn : getType().getMaleConnections()) {
                    gl.pushMatrix();
                    gl.translate(conn.x() * BLOCK_BASE, conn.y() * BLOCK_BASE, conn.z() * BLOCK_HEIGHT);
                    gl.render(STUD_MESH, entity);
                    gl.popMatrix();
                }
                gl.translate(BLOCK_BASE / 2, BLOCK_BASE / 2, 0);
            }

            drawPiece(gl, entity, renderTime);
        }
        gl.popMatrix();
    }

    /**
     * Draw the actual element. Overriding classes can use this to add additional details
     * @param gl     the gl object, positioned and rotated as this block
     * @param entity the entity this block is part of
     * @param renderTime
     */
    protected void drawPiece(SGL gl, Entity entity, float renderTime) {
        getType().draw(gl, entity);
    }

    /**
     * @return all connected blocks.
     */
    public Collection<AbstractPiece> getConnected() {
        return Arrays.asList(connections);
    }

    /**
     * rotates this piece a quarter around the z-axis.
     * @param clockwise if true, rotate clockwise, else rotate counterclockwise
     */
    public void rotateZ(boolean clockwise) {
        rotation = (byte) Math.floorMod(rotation + (clockwise ? 1 : -1), 4);
    }

    public void setPosition(Vector3ic position) {
        this.position.set(position);
    }

    public Vector3ic getPosition() {
        return position;
    }

    /**
     * @param grid
     * @return real-world position as if it were placed in the given subgrid
     */
    public Vector3f getWorldPosition(BlockSubGrid grid) {
        Vector3f localOffset = new Vector3f(
                position.x * BLOCK_BASE,
                position.y * BLOCK_BASE,
                position.z * BLOCK_HEIGHT
        );
        Vector3f gPos = grid.getWorldPosition();
        return localOffset.rotate(grid.getWorldRotation()).add(gPos);
    }

    public AABBi getHitBox() {
        if (hitbox == null) hitbox = recalculateHitbox();
        return hitbox;
    }

    protected AABBi recalculateHitbox() {
        Vector3i travel = new Vector3i(getType().size).sub(1, 1, 1);
        Vector3i virtualPos = new Vector3i(position);

        if (travel.x < 0) {
            virtualPos.x += travel.x;
            travel.x = 0;
        }
        if (travel.y < 0) {
            virtualPos.y += travel.y;
            travel.y = 0;
        }
        if (travel.z < 0) {
            virtualPos.z += travel.z;
            travel.z = 0;
        }

        rotateQuarters(travel, rotation);
        return new AABBi(virtualPos, travel);
    }

    public boolean intersects(AbstractPiece other) {
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

        return getType().hitbox.getIntersection(origin, direction);
    }

    /**
     * connects the given block at its current position to this block. If the block is not adjacent, nothing happens.
     * The given block may not intersect the current block. A similar call to {@code other.connect(this)} must be made.
     * @param other some other block
     */
    public void connect(AbstractPiece other) {
        assert !intersects(other);

        forEachConnection(other, connInd -> connections[connInd] = other);
    }

    public boolean canConnect(AbstractPiece buildCursor) {
        boolean[] buffer = new boolean[]{false};
        forEachConnection(buildCursor, (i) -> buffer[0] = true);
        return buffer[0];
    }

    private void forEachConnection(AbstractPiece other, IntConsumer action) {
        List<Vector3ic> thisConnections = getType().getConnections();
        List<Vector3ic> otherConnections = other.getType().getConnections();

        for (int i = 0; i < thisConnections.size(); i++) {
            Vector3ic connection = thisConnections.get(i);

            Vector3i p = new Vector3i(connection);
            rotateQuarters(p, rotation);
            p.add(position);

            for (Vector3ic oc : otherConnections) {
                Vector3i q = new Vector3i(oc);
                rotateQuarters(q, rotation);
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

    public byte getRotationByte() {
        return rotation;
    }

    public abstract PieceType getType();

    public abstract AbstractPiece copy();

    public abstract void writeToDataStream(DataOutputStream out, Map<PieceType, Integer> typeMap) throws IOException;

    private static void rotateQuarters(Vector3i vector, byte quarters) {
        for (byte i = 0; i < quarters; i++) {
            //noinspection SuspiciousNameCombination
            vector.set(-vector.y, vector.x, vector.z);
        }
    }
}
