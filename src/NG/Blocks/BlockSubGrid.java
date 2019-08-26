package NG.Blocks;

import NG.Blocks.Types.BlockPiece;
import NG.Blocks.Types.JointPiece;
import NG.Blocks.Types.PieceType;
import NG.Blocks.Types.PieceTypeJoint;
import NG.CollisionDetection.Collision;
import NG.DataStructures.Generic.AABBi;
import NG.Entities.Entity;
import NG.Rendering.MatrixStack.SGL;
import NG.Tools.GridRayScanner;
import NG.Tools.Logger;
import org.joml.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Math;
import java.util.*;

import static NG.Blocks.Types.BlockPiece.BLOCK_BASE;
import static NG.Blocks.Types.BlockPiece.BLOCK_HEIGHT;

/**
 * @author Geert van Ieperen created on 20-8-2019.
 */
public class BlockSubGrid extends AbstractCollection<BlockPiece> {
    private static final int BUCKET_SIZE = 5;
    private static final float TAU = (float) Math.PI * 2;
    protected BucketGrid3i<BlockPiece> blocks = new BucketGrid3i<>(BUCKET_SIZE);
    protected AABBi bounds = new AABBi();
    protected float totalMass = 0;

    private Quaternionf rotation;
    private Float minAngle = null;
    private Float maxAngle = null;

    // these 4 are only valid when root != null
    private BlockSubGrid root;
    private Vector3fc rootJointOffset;
    private Vector3fc jointGridOffset;
    private Vector3ic axis;
    private Vector3f realAxis;

    public BlockSubGrid() {
        rotation = new Quaternionf();
    }

    public boolean add(BlockPiece block) {
        PieceType type = block.getType();
        AABBi hitBox = block.getHitBox();

        this.totalMass += type.mass;
        this.bounds.union(hitBox);

        blocks.add(block, hitBox);
        return true;
    }

    public void removeParent(){
        root = null;
    }

    /**
     * set this grid to be the child of the given root grid.
     * @param newParent the new parent of this grid
     * @param piece     the piece that connects the parent grid to this grid
     * @param maleSide  whether the joint piece is used the normal way or its inverse
     */
    public void setParent(BlockSubGrid newParent, JointPiece piece, boolean maleSide) {
        assert canParent(newParent, piece, maleSide);

        Vector3f piecePos = piece.getWorldPosition(newParent);
        PieceTypeJoint t = piece.getType();
        Vector3ic headPiecePos;

        if (root == null) {
            Vector3fc offGridPos = maleSide ? t.jointOffset : t.headOffset;
            Vector3fc onGridPos = maleSide ? t.headOffset : t.jointOffset;

            this.root = newParent;
            this.rootJointOffset = piecePos.add(offGridPos);
            this.jointGridOffset = onGridPos;
            this.axis = piece.getAxis();

            this.realAxis = new Vector3f(axis).rotate(newParent.getWorldRotation()).normalize();

            if (t.hasAngleLimit) {
                minAngle = maleSide ? t.minAngle : TAU - t.minAngle;
                maxAngle = maleSide ? t.maxAngle : TAU - t.maxAngle;
            }
            setRotationAngle(minAngle);
            headPiecePos = new Vector3i();

        } else {
            headPiecePos = piece.getPosition();
        }

        add(t.headPiece.getInstance(headPiecePos, piece.getRotationByte(), piece.color));
        piece.setSubgrid(this);
    }

    public void setRotationAngle(float angle) {
        assert root != null;
        assert minAngle <= angle && angle <= maxAngle :
                String.format("%1.04f != [%1.04f ... %1.04f]", angle, minAngle, maxAngle);

        rotation.rotationAxis(angle, realAxis);
    }

    public Quaternionf getWorldRotation() {
        if (root == null){
            return new Quaternionf(rotation);
        } else {
            return root.getWorldRotation().mul(rotation);
        }
    }

    public boolean canParent(BlockSubGrid parent, JointPiece joint, boolean maleSide) {
        if (parent == this) return false;
        if (root == null) return true;
        if (parent != root) return false;

        Vector3f jointPos = joint.getWorldPosition(parent);
        PieceTypeJoint t = joint.getType();

        if (joint.getAxis() != axis) return false;
        if (t.headOffset != jointGridOffset) return false;
        if (t.jointOffset != rootJointOffset) return false;

        Vector3fc offGridPos = maleSide ? t.jointOffset : t.headOffset;
        Vector3f newRootOffset = jointPos.add(offGridPos);

        float offset = Intersectionf.distancePointLine(
                newRootOffset.x(), newRootOffset.y(), newRootOffset.z(),
                rootJointOffset.x(), rootJointOffset.y(), rootJointOffset.z(),
                realAxis.x(), realAxis.y(), realAxis.z()
        );

        if (offset > 1e-4) return false;

        if (t.hasAngleLimit) {
            float tMin = maleSide ? t.minAngle : TAU - t.minAngle;
            float tMax = maleSide ? t.maxAngle : TAU - t.maxAngle;
            return tMin <= maxAngle && tMax >= minAngle;
        }

        return true;
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
        gl.pushMatrix();
        {
            gl.translate(getWorldPosition());
            gl.rotate(getWorldRotation());

            for (BlockPiece block : blocks) {
                block.draw(gl, entity);
            }
        }
        gl.popMatrix();
    }

    public void writeToDataStream(DataOutputStream out, HashMap<PieceType, Integer> typeMap) throws IOException {

        out.writeInt(blocks.size());
        for (BlockPiece s : blocks) {
            s.writeToDataStream(out, typeMap);
        }
    }

    public GridRayScanner getRayScanner() {
        return new GridRayScanner(
                bounds.size(),
                new Vector3f(BLOCK_BASE, BLOCK_BASE, BLOCK_HEIGHT),
                new BlockIntersections()
        );
    }

    @Override
    public Iterator<BlockPiece> iterator() {
        return blocks.iterator();
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof BlockPiece) {
            int firstSize = blocks.size();

            BlockPiece asBlock = (BlockPiece) o;
            blocks.remove(asBlock, asBlock.getHitBox());

            return firstSize == blocks.size();
        }
        return false;
    }

    @Override
    public void clear() {
        blocks = new BucketGrid3i<>(BUCKET_SIZE);
        totalMass = 0;
        bounds = new AABBi();
    }

    @Override
    public int size() {
        return blocks.size();
    }

    public Vector3f getWorldPosition() {
        if (root == null) return new Vector3f();
        Vector3f rootPos = root.getWorldPosition().add(rootJointOffset);
        Vector3f offset = new Vector3f(jointGridOffset).rotate(rotation);
        return rootPos.add(offset);
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
