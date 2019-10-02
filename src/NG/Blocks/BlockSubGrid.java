package NG.Blocks;

import NG.Blocks.Types.AbstractPiece;
import NG.Blocks.Types.JointPiece;
import NG.Blocks.Types.PieceType;
import NG.Blocks.Types.PieceTypeJoint;
import NG.CollisionDetection.BoundingBox;
import NG.CollisionDetection.Collision;
import NG.DataStructures.Generic.AABBi;
import NG.Entities.Entity;
import NG.Rendering.MatrixStack.SGL;
import NG.Tools.GridRayScanner;
import NG.Tools.Logger;
import org.joml.*;

import java.lang.Math;
import java.util.*;

import static NG.Blocks.Types.AbstractPiece.*;

/**
 * @author Geert van Ieperen created on 20-8-2019.
 */
public class BlockSubGrid extends AbstractCollection<AbstractPiece> {
    private static final int BUCKET_SIZE = 5;
    private static final float TAU = (float) Math.PI * 2;
    protected BucketGrid3i<AbstractPiece> blocks;
    protected AABBi bounds;
    protected float totalMass = 0;
    private Vector3f centerOfMass = new Vector3f();

    // these 6 are only valid when root != null
    private BlockSubGrid root;
    private Vector3fc rootJointOffset;
    private Vector3ic axis;
    private Vector3f realAxis;
    private Float minAngle = null;
    private Float maxAngle = null;

    // position
    private Vector3f jointGridOffset;
    // rotation
    private Quaternionf rotation;

    public BlockSubGrid() {
        rotation = new Quaternionf();
        jointGridOffset = new Vector3f();
        clear();
    }

    public BlockSubGrid(Quaternionf orientation, Vector3fc position) {
        rotation = new Quaternionf(orientation);
        jointGridOffset = new Vector3f(position);
        clear();
    }

    public boolean add(AbstractPiece block) {
        PieceType type = block.getType();
        AABBi hitBox = block.getHitBox();

        this.bounds.union(hitBox);

        Vector3f blockCOM = new Vector3f(hitBox.getMinimum())
                .lerp(new Vector3f(hitBox.getMaximum()), 0.5f)
                .mul(BLOCK_SIZE);


        // weighted average of center of mass
        centerOfMass.mul(totalMass)
                .add(blockCOM.mul(type.mass));
        this.totalMass += type.mass;
        centerOfMass.div(totalMass);

        blocks.add(block, hitBox);

        return true;
    }

    /**
     * removes attachment properties, and set this subgrid to the given center.
     * @param newPosition the new position of the origin of this subgrid.
     */
    public void setPosition(Vector3fc newPosition) {
        root = null;
        jointGridOffset.set(newPosition);
    }

    /**
     * set this grid to be the child of the given root grid.
     * @param newParent the new parent of this grid
     * @param piece     the piece that connects the parent grid to this grid
     * @param maleSide  whether the joint piece is used the normal way or its inverse
     */
    public void setParent(BlockSubGrid newParent, JointPiece piece, boolean maleSide) {
        assert canParent(newParent, piece, maleSide);

        Vector3f piecePos = piece.getStructurePosition(newParent);
        PieceTypeJoint t = piece.getType();
        Vector3ic headPiecePos;

        if (root == null) {
            Vector3fc offGridPos = maleSide ? t.jointOffset : t.headOffset;
            Vector3fc onGridPos = maleSide ? t.headOffset : t.jointOffset;

            this.root = newParent;
            this.rootJointOffset = piecePos.add(offGridPos);
            this.jointGridOffset.set(onGridPos);
            this.axis = piece.getAxis();

            this.realAxis = new Vector3f(axis).rotate(newParent.getStructureRotation()).normalize();

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

    /**
     * @return construction-space rotation
     */
    public Quaternionf getStructureRotation() {
        if (root == null) {
            return new Quaternionf(rotation);
        } else {
            return root.getStructureRotation().mul(rotation);
        }
    }

    public boolean canParent(BlockSubGrid parent, JointPiece joint, boolean maleSide) {
        if (parent == this) return false;
        if (root == null) return true;
        if (parent != root) return false;

        Vector3f jointPos = joint.getStructurePosition(parent);
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

    public Vector3fc getCenterOfMass() {
        return centerOfMass;
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
            gl.translate(getStructurePosition());
            gl.rotate(getStructureRotation());

            for (AbstractPiece block : blocks) {
                block.draw(gl, entity, renderTime);
            }
        }
        gl.popMatrix();
    }

    /**
     * @return structure-space hitbox
     */
    public BoundingBox getStructureHitbox() {
        BoundingBox result = new BoundingBox();
        Quaternionf rotation = getStructureRotation();

        Vector3f buffer = new Vector3f();
        addBound(result, rotation, buffer, bounds.xMin - 0.5f, bounds.yMin - 0.5f, bounds.zMin);
        addBound(result, rotation, buffer, bounds.xMax + 0.5f, bounds.yMin - 0.5f, bounds.zMin);
        addBound(result, rotation, buffer, bounds.xMin - 0.5f, bounds.yMax + 0.5f, bounds.zMin);
        addBound(result, rotation, buffer, bounds.xMin - 0.5f, bounds.yMin - 0.5f, bounds.zMax + 1f);
        addBound(result, rotation, buffer, bounds.xMax + 0.5f, bounds.yMax + 0.5f, bounds.zMin);
        addBound(result, rotation, buffer, bounds.xMax + 0.5f, bounds.yMin - 0.5f, bounds.zMax + 1f);
        addBound(result, rotation, buffer, bounds.xMin - 0.5f, bounds.yMax + 0.5f, bounds.zMax + 1f);
        addBound(result, rotation, buffer, bounds.xMax + 0.5f, bounds.yMax + 0.5f, bounds.zMax + 1f);
        result.move(getStructurePosition());

        return result;
    }

    public boolean isRoot() {
        return root == null;
    }

    private static void addBound(BoundingBox dest, Quaternionf rotation, Vector3f buffer, float x, float y, float z) {
        Vector3f point3D = buffer.set(x, y, z)
                .mul(BLOCK_SIZE)
                .rotate(rotation);

        dest.union(point3D);
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

    /**
     * @return contruction-space position
     */
    public Vector3f getStructurePosition() {
        if (root == null) return new Vector3f(jointGridOffset);
        Vector3f rootPos = root.getStructurePosition().add(rootJointOffset);
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
