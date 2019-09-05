package NG.Blocks;

import NG.Blocks.Types.AbstractPiece;
import NG.Blocks.Types.JointPiece;
import NG.Blocks.Types.PieceType;
import NG.Blocks.Types.PieceTypeJoint;
import NG.CollisionDetection.Collision;
import NG.DataStructures.Generic.AABBi;
import NG.Entities.Entity;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import NG.Tools.GridRayScanner;
import NG.Tools.Logger;
import org.joml.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Math;
import java.util.*;

import static NG.Blocks.Types.AbstractPiece.BLOCK_BASE;
import static NG.Blocks.Types.AbstractPiece.BLOCK_HEIGHT;

/**
 * @author Geert van Ieperen created on 20-8-2019.
 */
public class BlockSubGrid extends AbstractCollection<AbstractPiece> {
    private static final int BUCKET_SIZE = 5;
    private static final float TAU = (float) Math.PI * 2;
    protected BucketGrid3i<AbstractPiece> blocks;
    protected AABBi bounds;
    protected float totalMass;

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
        clear();
    }

    public boolean add(AbstractPiece block) {
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

    public boolean canAttach(AbstractPiece element) {
        if (blocks.isEmpty()) return true;

        AABBi box = element.getHitBox();

        List<AbstractPiece> nearbyBlocks = blocks.get(
                box.xMin, box.yMin, box.zMin - 1,
                box.xMax, box.yMax, box.zMax + 1
        );

        // none may overlap
        for (AbstractPiece block : nearbyBlocks) {
            if (block.intersects(element)) {
                Logger.DEBUG.printf("%s overlaps with %s", element, block);
                return false;
            }
        }
        // at least one must connect
        for (AbstractPiece block : nearbyBlocks) {
            if (block.canConnect(element)) return true;
        }

        Logger.DEBUG.printf("%s does not connect", element);
        return false;
    }

    public void draw(SGL gl, Entity entity, float renderTime) {
        gl.pushMatrix();
        {
            gl.translate(getWorldPosition());
            gl.rotate(getWorldRotation());

            for (AbstractPiece block : blocks) {
                block.draw(gl, entity, renderTime);
            }
        }
        gl.popMatrix();
    }

    public void writeToDataStream(DataOutputStream out, HashMap<PieceType, Integer> typeMap) throws IOException {
        Storable.writeQuaternionf(out, rotation);

        out.writeInt(blocks.size());
        for (AbstractPiece s : blocks) {
            Storable.writeClass(out, s.getClass());
            s.writeToDataStream(out, typeMap);
        }
    }

    public BlockSubGrid(DataInputStream in, PieceType[] typeMap) throws IOException {
        rotation = Storable.readQuaternionf(in);
        clear();

        int nrOfBlocks = in.readInt();
        try {
            for (int i = 0; i < nrOfBlocks; i++) {
                // not the most beautiful, but robust enough
                AbstractPiece piece = Storable.readClass(in, AbstractPiece.class)
                        .getConstructor(DataInputStream.class, PieceType[].class)
                        .newInstance(in, typeMap);
                add(piece);
            }
        } catch (ReflectiveOperationException ex){
            throw new IOException(ex);
        }
    }

    public AABBf getHitBox() {
        AABBf result = new AABBf();
        Quaternionf rotation = getWorldRotation();

        Vector3f point = new Vector3f();
        result.union(point.set(bounds.xMin, bounds.yMin, bounds.zMin).rotate(rotation));
        result.union(point.set(bounds.xMax, bounds.yMin, bounds.zMin).rotate(rotation));
        result.union(point.set(bounds.xMin, bounds.yMax, bounds.zMin).rotate(rotation));
        result.union(point.set(bounds.xMin, bounds.yMin, bounds.zMax).rotate(rotation));
        result.union(point.set(bounds.xMax, bounds.yMax, bounds.zMin).rotate(rotation));
        result.union(point.set(bounds.xMax, bounds.yMin, bounds.zMax).rotate(rotation));
        result.union(point.set(bounds.xMin, bounds.yMax, bounds.zMax).rotate(rotation));
        result.union(point.set(bounds.xMax, bounds.yMax, bounds.zMax).rotate(rotation));

        return result;
    }

    public GridRayScanner getRayScanner() {
        return new GridRayScanner(
                bounds.getMinimum(), bounds.getMaximum(),
                v -> v, new BlockIntersections()
        );
    }

    @Override
    public Iterator<AbstractPiece> iterator() {
        return blocks.iterator();
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof AbstractPiece) {
            int firstSize = blocks.size();

            AbstractPiece asBlock = (AbstractPiece) o;
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
        Set<AbstractPiece> seen = new HashSet<>();
        private Vector3f blockLocalOrigin = new Vector3f();

        @Override
        public Collision getIntersection(Vector3fc origin, Vector3fc direction, int xCoord, int yCoord, int zCoord) {
            AbstractPiece target = blocks.get(xCoord, yCoord, zCoord);
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
