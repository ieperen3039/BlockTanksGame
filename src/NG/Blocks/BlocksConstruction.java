package NG.Blocks;

import NG.CollisionDetection.BoundingBox;
import NG.CollisionDetection.Collision;
import NG.DataStructures.Generic.AABBi;
import NG.DataStructures.Vector3fx;
import NG.DataStructures.Vector3fxc;
import NG.Entities.Entity;
import NG.Entities.MovingEntity;
import NG.Entities.MutableState;
import NG.Entities.State;
import NG.Rendering.MatrixStack.SGL;
import NG.Tools.GridRayScanner;
import NG.Tools.Logger;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static NG.Blocks.Block.BLOCK_BASE;
import static NG.Blocks.Block.BLOCK_HEIGHT;

/**
 * @author Geert van Ieperen created on 14-8-2019.
 */
public class BlocksConstruction implements MovingEntity {
    private float totalMass = 0;
    private boolean isDisposed = false;
    private State state;
    private final BucketGrid3i<Block> blocks = new BucketGrid3i<>(-30, -20, -20, 30, 20, 20, 5);

    private AABBi bounds = new AABBi();

    public BlocksConstruction(BlockType type, Vector3fxc position, float gameTime) {
        this(position, gameTime, new Block(type, new Vector3i()));
    }

    public BlocksConstruction(Vector3fxc position, float gameTime, Block root) {
        this.state = new MutableState(gameTime, position);
        add(root);
    }

    public BlocksConstruction(BlockType type) {
        this(type, new Vector3fx(), 0);
    }

    public BlocksConstruction() {
        this.state = new MutableState(0, new Vector3fx());
    }

    public void add(BlockType type, Vector3ic position) {
        add(new Block(type, position));
    }

    public void add(Block block) {
        BlockType type = block.getType();
        AABBi hitBox = block.getHitBox();

        this.totalMass += type.mass;
        this.bounds.union(hitBox);

        blocks.add(block, hitBox);
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }

    @Override
    public float getMass() {
        return totalMass;
    }

    @Override
    public void draw(SGL gl, float renderTime) {
        gl.pushMatrix();
        {
            gl.translateRotate(state);
            for (Block block : blocks) {
                block.draw(gl, this);
            }
        }
        gl.popMatrix();
    }

    @Override
    public void update(float gameTime) {
        state.update(gameTime);
    }

    @Override
    public State getCurrentState() {
        return state;
    }

    @Override
    public BoundingBox getBoundingBox() {
        if (blocks.isEmpty()) return new BoundingBox();

        return new BoundingBox(
                bounds.xMin * BLOCK_BASE,
                bounds.yMin * BLOCK_BASE,
                bounds.zMin * BLOCK_HEIGHT,

                bounds.xMax * BLOCK_BASE,
                bounds.yMax * BLOCK_BASE,
                bounds.zMax * BLOCK_HEIGHT
        );
    }

    @Override
    public Collision getIntersection(Vector3fc origin, Vector3fc direction) {
        if (blocks.isEmpty()) return Collision.NONE;

        return new GridRayScanner(
                bounds.size(),
                new Vector3f(BLOCK_BASE, BLOCK_BASE, BLOCK_HEIGHT),
                new BlockIntersections()
        ).getIntersection(origin, direction, false);
    }

    @Override
    public List<Vector3f> getShapePoints(List<Vector3f> dest) {
        return null;
    }

    @Override
    public void collideWith(Entity other, Collision collision, float collisionTime) {

    }

    @Override
    public void dispose() {
        isDisposed = true;
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }

    public boolean canAttach(Block buildCursor) {
        if (blocks.isEmpty()) return true;

        AABBi box = buildCursor.getHitBox();

        List<Block> nearbyBlocks = blocks.get(
                box.xMin, box.yMin, box.zMin - 1,
                box.xMax, box.yMax, box.zMax + 1
        );

        // none may overlap
        for (Block block : nearbyBlocks) {
            if (block.intersects(buildCursor)) {
                Logger.WARN.print("Block overlaps with " + block);
                return false;
            }
        }
        // at least one must connect
        for (Block block : nearbyBlocks) {
            if (block.canConnect(buildCursor)) return true;
        }

        Logger.WARN.print("Block does not connect");
        return false;
    }

    private class BlockIntersections implements GridRayScanner.Intersectable {
        Set<Block> seen = new HashSet<>();
        private Vector3f blockLocalOrigin = new Vector3f();

        @Override
        public Collision getIntersection(Vector3fc origin, Vector3fc direction, int xCoord, int yCoord, int zCoord) {
            Block target = blocks.get(xCoord, yCoord, zCoord);
            if (seen.contains(target)) return Collision.NONE;
            seen.add(target);

            Vector3ic position = target.getPosition();
            blockLocalOrigin.set(position)
                    .mul(BLOCK_BASE, BLOCK_BASE, BLOCK_HEIGHT);
            blockLocalOrigin = origin.sub(blockLocalOrigin, blockLocalOrigin);

            return target.getIntersection(blockLocalOrigin, direction);
        }
    }
}
