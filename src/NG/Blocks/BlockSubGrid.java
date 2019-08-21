package NG.Blocks;

import NG.Blocks.Types.BlockPiece;
import NG.Blocks.Types.JointPiece;
import NG.Blocks.Types.PieceType;
import NG.CollisionDetection.Collision;
import NG.DataStructures.Generic.AABBi;
import NG.DataStructures.Generic.Color4f;
import NG.Entities.Entity;
import NG.Rendering.MatrixStack.SGL;
import NG.Tools.GridRayScanner;
import NG.Tools.Logger;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3ic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static NG.Blocks.Types.BlockPiece.BLOCK_BASE;
import static NG.Blocks.Types.BlockPiece.BLOCK_HEIGHT;

/**
 * @author Geert van Ieperen created on 20-8-2019.
 */
public class BlockSubGrid {
    public static final int BUCKET_SIZE = 5;
    protected final BucketGrid3i<BlockPiece> blocks = new BucketGrid3i<>(BUCKET_SIZE);
    protected AABBi bounds = new AABBi();
    protected float totalMass = 0;
    protected List<BlockSubGrid> subgrids = new ArrayList<>();

    public void add(PieceType type, Vector3ic position, Color4f color) {
        add(type.getInstance(position, color));
    }

    public void add(BlockPiece block) {
        PieceType type = block.getType();
        AABBi hitBox = block.getHitBox();

        this.totalMass += type.mass;
        this.bounds.union(hitBox);

       if (block instanceof JointPiece){
           JointPiece asJoint = (JointPiece) block;
           subgrids.add(asJoint.getSubgrid());
       }

        blocks.add(block, hitBox);
    }

    public float getMass() {
        return totalMass;
    }

    public boolean canAttach(BlockPiece element) {
        if (blocks.isEmpty()) return true;

        AABBi box = element.getHitBox();

        List<BlockPiece> nearbyBlocks = blocks.get(
                box.xMin, box.yMin, box.zMin - 1,
                box.xMax, box.yMax, box.zMax + 1
        );

        // none may overlap
        for (BlockPiece block : nearbyBlocks) {
            if (block.intersects(element)) {
                Logger.DEBUG.print("Block " + element + " overlaps with " + block);
                return false;
            }
        }
        // at least one must connect
        for (BlockPiece block : nearbyBlocks) {
            if (block.canConnect(element)) return true;
        }

        Logger.DEBUG.print("Block does not connect");
        return false;
    }

    public void draw(SGL gl, Entity entity) {
        for (BlockPiece block : blocks) {
            block.draw(gl, entity);
        }
    }

    protected class BlockIntersections implements GridRayScanner.Intersectable {
        Set<BlockPiece> seen = new HashSet<>();
        private Vector3f blockLocalOrigin = new Vector3f();

        @Override
        public Collision getIntersection(Vector3fc origin, Vector3fc direction, int xCoord, int yCoord, int zCoord) {
            BlockPiece target = blocks.get(xCoord, yCoord, zCoord);
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
