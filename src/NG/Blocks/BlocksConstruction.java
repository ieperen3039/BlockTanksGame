package NG.Blocks;

import NG.Blocks.Types.AbstractPiece;
import NG.Blocks.Types.JointPiece;
import NG.Blocks.Types.PieceType;
import NG.CollisionDetection.BoundingBox;
import NG.CollisionDetection.Collision;
import NG.DataStructures.Generic.Color4f;
import NG.DataStructures.Vector3fx;
import NG.DataStructures.Vector3fxc;
import NG.Entities.*;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MatrixStack.ShadowMatrix;
import NG.Settings.Settings;
import NG.Storable;
import NG.Tools.BuoyancyComputation;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.*;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static NG.Blocks.BlocksConstruction._ClassVersion.INITIAL;
import static NG.Blocks.Types.AbstractPiece.BLOCK_VOLUME;
import static NG.Blocks.Types.AbstractPiece.BLOCK_WEIGHT;

/**
 * An entity made from block grids
 * @author Geert van Ieperen created on 14-8-2019.
 */
public class BlocksConstruction extends MovingEntity {
    private static final float WATER_RESIST_FACTOR = 20f;
    private static final float AIR_RESIST_FACTOR = 2f;

    private final List<BlockSubGrid> subgrids;
    private final List<ForceBlock> forceBlocks = new ArrayList<>();

    private final boolean doPerBlockBuoyancy = false;
    private final boolean doRotation = true;

    public BlocksConstruction(Vector3fxc position, Quaternionf rotation, float gameTime) {
        this(new FixedState(position, rotation, gameTime));
    }

    public BlocksConstruction(FixedState spawnState) {
        super(spawnState);
        subgrids = new ArrayList<>();
        subgrids.add(new BlockSubGrid());
    }

    @Override
    public void preUpdate(float gameTime, float deltaTime) {
        BuoyancyComputation buoy = new BuoyancyComputation();
        Vector3fxc thisPosition = state.position();
        Quaternionfc thisOrientation = state.orientation();
        BoundingBox hitbox = getHitbox(gameTime);
        Vector3fx COM = getCenterOfMass();
        float mass = getMass();
        float momentInertia = 0;
        Vector3f temp = new Vector3f();

        for (BlockSubGrid grid : subgrids) {
            if (doPerBlockBuoyancy) {
                for (AbstractPiece piece : grid) {
                    Vector3f structurePosition = piece.getStructurePosition(grid).rotate(thisOrientation);
                    Vector3fx pos = new Vector3fx(structurePosition).add(thisPosition);
                    Vector3fc dim = piece.getType().realSize;
                    float volume = dim.x() * dim.y() * dim.z();
                    buoy.addPointVolume(pos, volume);

                    momentInertia += piece.getType().mass * pos.sub(COM).lengthSquared();
                }

            } else {
                BoundingBox localHitBox = grid.getLocalHitBox();
                BoundingBox globalHitbox = new BoundingBox();
                globalHitbox.unionRotated(localHitBox, grid.getStructureRotation());
                globalHitbox.move(thisPosition);
                float volume = (grid.totalMass / BLOCK_WEIGHT) * BLOCK_VOLUME;
                buoy.addAABB(globalHitbox, volume);
            }
        }

        float upForce = buoy.getFloatForce();
        state.addForce(temp.set(0, 0, upForce), mass);

        float gravity = Settings.GRAVITY_CONSTANT * mass;
        state.addForce(temp.set(0, 0, -gravity), mass);
        Logger.WARN.print(thisPosition, gravity - upForce);

        if (doRotation) {
            if (momentInertia == 0){
                momentInertia = hitbox.size().div(4).lengthSquared() * mass;
            }
            Vector3f rotationForce = buoy.getRotationXYZ(COM, mass, upForce);
            state.addRotation(rotationForce, momentInertia); // every point m * r^2
        }

        for (ForceBlock block : forceBlocks) {
            Vector3f structurePosition = block.piece.getStructurePosition(block.grid);
            Vector3fx bPos = new Vector3fx(structurePosition).add(state.position());
            Vector3f force = block.fPiece.getForce(bPos);
            Vector3f comToPos = bPos.subToVector3f(COM);

            float distSq = Vectors.getDistanceSqPointLine(force, comToPos);

            // @see BuoyancyComputation#getRotationXYZ(Vector3fxc, float, float)
            float torque = (force.length()) / distSq;
            comToPos.normalize().cross(Vectors.Z).mul(torque / mass); // probably wrong, but close enough
        }

        // resistances, assumes motion in x-direction
        float inWaterFrac = buoy.getSubmergedFraction(hitbox);
        Vector3fc velocity = state.velocity();
        float vSq = velocity.lengthSquared();
        Vector3f resistForce = new Vector3f(velocity).mul(-vSq);
        float factor = WATER_RESIST_FACTOR * inWaterFrac + AIR_RESIST_FACTOR * (1 - inWaterFrac);
        resistForce.mul(factor * (hitbox.maxZ - hitbox.minZ) * (hitbox.maxX - hitbox.minX));
        state.addForce(resistForce, mass);

//        Logger.WARN.print(gravity, resistForce, rotationForce);
        super.preUpdate(gameTime, deltaTime);
    }


    @Override
    public float getMass() {
        float sum = 0f;
        for (BlockSubGrid subgrid : subgrids) {
            sum += subgrid.getMass();
        }
        return sum;
    }

    @Override
    public Vector3fx getCenterOfMass() {
        Vector3f globalCOMOffset = getLocalCenterOfMass().rotate(state.orientation());
        return new Vector3fx(state.position()).add(globalCOMOffset);
    }

    private Vector3f getLocalCenterOfMass() {
        Vector3f COM = new Vector3f();
        float totalMass = 0;

        for (BlockSubGrid grid : subgrids) {
            float mass = grid.getMass();
            Vector3fc weighted = new Vector3f(grid.getCenterOfMass()).mul(mass);
            COM.add(weighted);
            totalMass += mass;
        }

        return COM.div(totalMass);
    }

    @Override
    public void draw(SGL gl, float renderTime) {
        gl.pushMatrix();
        {
            gl.translateRotate(state);
            for (BlockSubGrid subgrid : subgrids) {
                subgrid.draw(gl, this, renderTime);
            }
            gl.translate(getLocalCenterOfMass());
            Toolbox.draw3DPointer(gl);
        }
        gl.popMatrix();

        disposeStatesUntil(renderTime);
    }

    @Override
    public BoundingBox getHitbox(float time) {
        BoundingBox box = new BoundingBox();
        Quaternionfc orientation = getStateAt(time).orientation();

        for (BlockSubGrid subgrid : subgrids) {
            box.unionRotated(subgrid.getLocalHitBox(), orientation);
        }

        box.move(getStateAt(time).position());

        return box;
    }

    @Override
    public Collision getIntersection(Vector3fc origin, Vector3fc direction) {
        Collision intersection = Collision.NONE;

        for (BlockSubGrid subgrid : subgrids) {
            Quaternionf rotationInv = subgrid.getStructureRotation().invert();
            Vector3fc localOrg = new Vector3f(origin).rotate(rotationInv);
            Vector3fc localDir = new Vector3f(direction).rotate(rotationInv);

            Collision next = subgrid.getRayScanner()
                    .getIntersection(localOrg, localDir, false);

            if (next.isEarlierThan(intersection)) {
                intersection = next;
            }
        }

        return intersection;
    }

    public GridModificator getSubgridModificator() {
        assert !subgrids.isEmpty();
        return new GridModificator();
    }

    @Override
    public List<Vector3f> getShapePoints(List<Vector3f> dest, float gameTime) {
        ShadowMatrix sm = new ShadowMatrix();
        int i = 0;

        for (BlockSubGrid grid : subgrids) {
            sm.pushMatrix();
            sm.translate(grid.getStructurePosition());
            sm.rotate(grid.getStructureRotation());

            for (AbstractPiece piece : grid) {
                // collect points and ensure dest capacity
                List<Vector3fc> points = piece.getShape().getPoints();
                int startInd = i;
                i += points.size();
                while (dest.size() < i) {
                    dest.add(new Vector3f());
                }

                piece.doLocal(sm, 0, () -> {
                    int curInd = startInd;
                    for (Vector3fc point : points) {
                        // map position of point to world-space and store it in the appropriate position in dest
                        sm.getPosition(point, dest.get(curInd++));
                    }
                });
            }
            sm.popMatrix();
        }

        if (dest.size() > i) {
            dest.subList(i, dest.size()).clear();
        }

        return dest;
    }

    @Override
    public void collideWith(Entity other, Collision collision, float collisionTime) {

    }

    @Override
    public void writeToDataStream(DataOutputStream out) throws IOException {
        Storable.write(out, state);
        Storable.writeEnum(out, INITIAL);

        HashMap<PieceType, Integer> types = new HashMap<>();

        // write the construction to buffer, while collecting type information
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream bufferOut = new DataOutputStream(buffer);
        bufferOut.writeInt(subgrids.size());
        for (BlockSubGrid s : subgrids) {
            s.writeToDataStream(bufferOut, types);
        }

        // gather types and connect id values
        PieceType[] sorted = new PieceType[types.size()];
        types.forEach((v, i) -> sorted[i] = v);

        out.writeInt(sorted.length);
        for (PieceType type : sorted) {
            out.writeUTF(type.category);
            out.writeUTF(type.name);
        }

        // now write construction to out
        buffer.writeTo(out);
    }

    public BlocksConstruction(DataInputStream in) throws IOException, ClassNotFoundException {
        super(Storable.read(in, State.class));
        _ClassVersion version = Storable.readEnum(in, _ClassVersion.class);

        int nrOfTypes = in.readInt();
        PieceType[] typeMap = new PieceType[nrOfTypes];

        switch (version) {
            case INITIAL:
                for (int i = 0; i < nrOfTypes; i++) {
                    String manufacturer = in.readUTF();
                    String pieceName = in.readUTF();

                    PieceTypeCollection manf = PieceTypeCollection.allCollections.get(manufacturer);
                    if (manf == null) {
                        Logger.ERROR.print("Could not find manufacturer " + manufacturer);
                        break;
                    }

                    PieceType piece = manf.getByName(pieceName);
                    if (piece == null) {
                        Logger.ERROR.print("Could not find block type " + pieceName + " of manufacturer " + manufacturer);
                        break;
                    }

                    typeMap[i] = piece;
                }
                break;

            default:
                throw new IOException("Entity version " + version);
        }

        int nrOfGrids = in.readInt();
        subgrids = new ArrayList<>(nrOfGrids);
        for (int i = 0; i < nrOfGrids; i++) {
            subgrids.add(new BlockSubGrid(in, typeMap));
        }
    }

    public class GridModificator {
        BlockSubGrid target;
        int index = 0;

        private GridModificator() {
            validateCache();
        }

        public void add(PieceType type, Vector3ic position, Color4f color) {
            add(type.getInstance(position, 0, color));
        }

        public void add(AbstractPiece block) {
            target.add(block);

            if (block instanceof JointPiece) {
                JointPiece jointBlock = (JointPiece) block;
                // try to connect with any of the existing subgrids
                boolean found = false;
                for (BlockSubGrid subgrid : subgrids) {
                    if (subgrid.canParent(target, jointBlock, true)) {
                        subgrid.setParent(target, jointBlock, true);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    BlockSubGrid newGrid = new BlockSubGrid();
                    newGrid.setParent(target, jointBlock, true);
                    subgrids.add(newGrid);
                }
            }
            if (block instanceof ForceGenerating){
                forceBlocks.add(new ForceBlock(block, target));
            }
        }

        public void next() {
            index++;
            validateCache();
        }

        private void validateCache() {
            index = Math.floorMod(index, subgrids.size());
            target = subgrids.get(index);
        }

        public void previous() {
            index--;
            validateCache();
        }

        public boolean canAttach(AbstractPiece element) {
            // TODO check overlap with other subgrids
            return target.canAttach(element);
        }

        public BlockSubGrid getGrid() {
            return target;
        }
    }

    enum _ClassVersion {
        INITIAL,
    }

    private static class ForceBlock {
        public final AbstractPiece piece;
        public final BlockSubGrid grid;
        public final ForceGenerating fPiece;

        private ForceBlock(AbstractPiece piece, BlockSubGrid grid) {
            this.piece = piece;
            this.grid = grid;
            this.fPiece = (ForceGenerating) piece;
        }
    }
}
