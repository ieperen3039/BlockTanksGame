package NG.Blocks.Types;

import NG.Blocks.BlockSubGrid;
import NG.CollisionDetection.Collision;
import NG.DataStructures.Generic.AABBi;
import NG.DataStructures.Generic.Color4f;
import NG.Entities.Entity;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.MatrixStack;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Shapes.Shape;
import NG.Storable;
import NG.Tools.Directory;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 2-9-2019.
 */
public abstract class AbstractPiece {
    public static final boolean RENDER_STUDS = true;
    /* size of a 1x1x1 block, base in meters, scaled by 100 */
    public static final float BLOCK_BASE = 0.8f;
    public static final float BLOCK_HEIGHT = 0.32f;
    public static final Vector3f BLOCK_SIZE = new Vector3f(BLOCK_BASE, BLOCK_BASE, BLOCK_HEIGHT);

    private static final MeshFile STUD = MeshFile.loadFileRequired(Directory.meshes.getPath("stud.ply"));
    protected final Vector3i position;
    private AABBi hitbox = null;
    private static Mesh STUD_MESH = null;
    public Color4f color;
    protected byte rotation;

    AbstractPiece(Vector3ic position, int zRotation, Color4f color) {
        this.position = new Vector3i(position);
        this.rotation = (byte) zRotation;
        this.color = color;
    }

    /**
     * draw this block, assuming the gl object has been set to the origin of the parenting grid
     */
    public void draw(SGL gl, Entity entity, float renderTime) {
        if (color.alpha == 0) return;

        ShaderProgram shader = gl.getShader();
        if (shader instanceof MaterialShader) {
            ((MaterialShader) shader).setMaterial(Material.PLASTIC, color);
        }

        doLocal(gl, renderTime, () -> {
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
        });
    }

    public void doLocal(MatrixStack gl, float renderTime, Runnable action) {
        gl.pushMatrix();
        {
            gl.translate(
                    position.x * BLOCK_BASE,
                    position.y * BLOCK_BASE,
                    position.z * BLOCK_HEIGHT
            );
            gl.rotateQuarter(0, 0, rotation);

            action.run();
        }
        gl.popMatrix();
    }

    /**
     * Draw the actual element. Overriding classes can use this to add additional details
     * @param gl         the gl object, positioned and rotated as this block
     * @param entity     the entity this block is part of
     * @param renderTime the current
     */
    protected void drawPiece(SGL gl, Entity entity, float renderTime) {
        getType().draw(gl, entity);
    }

    /**
     * rotates this piece a quarter around the z-axis.
     * @param clockwise if true, rotate clockwise, else rotate counterclockwise
     */
    public void rotateZ(boolean clockwise) {
        rotation = (byte) Math.floorMod(rotation + (clockwise ? 1 : -1), 4);
        recalculateHitbox();
    }

    /**
     * @param grid the subgrid where this piece is placed in
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

    /**
     * a grid-based hitbox of this block. This box is one smaller than size in each dimension.
     * @return a box describing which gridpoints are covered by this block.
     */
    public AABBi getHitBox() {
        if (hitbox == null) recalculateHitbox();
        return hitbox;
    }

    /**
     * recalculate the hitbox and set the {@link #hitbox} field accordingly.
     * @see #getHitBox()
     */
    protected void recalculateHitbox() {
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
        hitbox = new AABBi(virtualPos, travel);
    }

    /**
     * @param other another piece
     * @return true iff this blocks intersects the given other block when these are placed in the SAME subgrid.
     */
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
     * checks whether this block can connect to the given other block using connection points. Does not check whether
     * these blocks intersect, which can be checked with {@link #intersects(AbstractPiece)}
     * @param other another piece
     * @return true iff these blocks have at least one connection point in common.
     */
    public boolean canConnect(AbstractPiece other) {
        List<Vector3ic> thisConnections = this.getType().getConnections();
        List<Vector3ic> otherConnections = other.getType().getConnections();

        for (Vector3ic connection : thisConnections) {
            Vector3i p = new Vector3i(connection);
            rotateQuarters(p, this.rotation);
            p.add(this.position);

            for (Vector3ic oc : otherConnections) {
                Vector3i q = new Vector3i(oc);
                rotateQuarters(q, other.rotation);
                q.add(other.position);

                if (p.equals(q)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setPosition(Vector3ic position) {
        this.position.set(position);
    }

    public void setPosition(int x, int y, int z) {
        this.position.set(x, y, z);
    }

    public void move(int x, int y, int z) {
        position.add(x, y, z);
        recalculateHitbox();
    }

    public Vector3ic getPosition() {
        return position;
    }

    public byte getRotationByte() {
        return rotation;
    }

    public abstract PieceType getType();

    public abstract AbstractPiece copy();

    public Shape getShape() {
        return getType().hitbox;
    }

    private static void rotateQuarters(Vector3i vector, byte quarters) {
        for (byte i = 0; i < quarters; i++) {
            //noinspection SuspiciousNameCombination
            vector.set(-vector.y, vector.x, vector.z);
        }
    }

    /**
     * writes this piece to the data output stream.
     * @param out     the stream to write to
     * @param typeMap a mapping from types to integers, such that types can be read from the stream with integers
     * @throws IOException if something goes wrong while writing
     */
    public final void writeToDataStream(DataOutputStream out, Map<PieceType, Integer> typeMap) throws IOException {
        out.writeInt(position.x);
        out.writeInt(position.y);
        out.writeInt(position.z);

        out.writeByte(rotation);
        Storable.writeColor(out, color);
        write(out, typeMap);
    }

    /**
     * writes data of implementing pieces to the stream. The complementary reading call must start with a call to {@link
     * #AbstractPiece(DataInputStream)}
     * @param out     the stream to write to
     * @param typeMap a mapping from types to integers
     */
    protected abstract void write(DataOutputStream out, Map<PieceType, Integer> typeMap) throws IOException;

    AbstractPiece(DataInputStream in) throws IOException {
        this(
                new Vector3i(
                        in.readInt(), in.readInt(), in.readInt()
                ),
                in.readByte(),
                Storable.readColor(in)
        );
    }
}
