package NG.Blocks;

import NG.Blocks.Types.*;
import NG.CollisionDetection.BoundingBox;
import NG.CollisionDetection.Collision;
import NG.DataStructures.Generic.Color4f;
import NG.DataStructures.Vector3fx;
import NG.DataStructures.Vector3fxc;
import NG.Entities.Entity;
import NG.Entities.MovingEntity;
import NG.Entities.MutableState;
import NG.Entities.State;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import NG.Tools.Logger;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3ic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * An entity made from block grids
 * @author Geert van Ieperen created on 14-8-2019.
 */
public class BlocksConstruction implements MovingEntity {
    private boolean isDisposed = false;
    private State state;
    private List<BlockSubGrid> subgrids = new ArrayList<>();

    public BlocksConstruction() {
        this(new Vector3fx(), 0);
    }

    public BlocksConstruction(Vector3fxc position, float gameTime) {
        this.state = new MutableState(gameTime, position);
        subgrids.add(new BlockSubGrid());
    }

    @Override
    public void setState(State state) {
        this.state = state;
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
    public void draw(SGL gl, float renderTime) {
        gl.pushMatrix();
        {
            gl.translateRotate(state);
            for (BlockSubGrid subgrid : subgrids) {
                subgrid.draw(gl, this, renderTime);
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
    public BoundingBox getHitbox() {
        BoundingBox box = new BoundingBox();

        for (BlockSubGrid subgrid : subgrids) {
            box.union(subgrid.getHitBox());
        }

        return box;
    }

    @Override
    public Collision getIntersection(Vector3fc origin, Vector3fc direction) {
        Collision intersection = Collision.NONE;

        for (BlockSubGrid subgrid : subgrids) {
            Quaternionf rotationInv = subgrid.getWorldRotation().invert();
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

    @Override
    public void writeToDataStream(DataOutputStream out) throws IOException {
        HashMap<PieceType, Integer> types = new HashMap<>();

        for (BlockSubGrid subgrid : subgrids) {
            for (AbstractPiece piece : subgrid) {
                PieceType type = piece.getType();
                types.computeIfAbsent(type, t -> types.size());
            }
        }

        PieceType[] sorted = new PieceType[types.size()];
        types.forEach((v, i) -> sorted[i] = v);

        out.writeInt(sorted.length);
        for (PieceType type : sorted) {
            out.writeUTF(type.name);
        }

        Storable.write(out, state);
        out.writeInt(subgrids.size());
        for (BlockSubGrid s : subgrids) {
            s.writeToDataStream(out, types);
        }
    }

    public BlocksConstruction(DataInputStream in) throws IOException, ClassNotFoundException {
        int nrOfTypes = in.readInt();
        PieceType[] typeMap = new PieceType[nrOfTypes];
        for (int i = 0; i < nrOfTypes; i++) {
            String pieceName = in.readUTF();
            // TODO maybe add manufacturer or include list of PieceTypeCollections
            typeMap[i] = PieceTypeCollection.cheatCache.get(pieceName);
        }

        state = Storable.read(in, State.class);
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

            } else if (block instanceof WheelBasePiece){
                WheelBasePiece wheelBaseBlock = (WheelBasePiece) block;
                List<WheelPiece> wheels = wheelBaseBlock.getWheels();
                Logger.DEBUG.printf("Adding %s wheels", wheels.size());

                PieceTypeWheel wheel = (PieceTypeWheel) PieceTypeCollection.cheatCache.get("Wheel small");

                for (int i = 0; i < wheels.size(); i++) {
                    wheels.add(i, wheel.getInstance(Color4f.WHITE));
                }
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
}
