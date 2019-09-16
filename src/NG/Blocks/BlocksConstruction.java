package NG.Blocks;

import NG.Blocks.Types.*;
import NG.CollisionDetection.BoundingBox;
import NG.CollisionDetection.Collision;
import NG.DataStructures.Generic.Color4f;
import NG.DataStructures.Vector3fxc;
import NG.Entities.Entity;
import NG.Entities.FixedState;
import NG.Entities.MovingEntity;
import NG.Entities.State;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MatrixStack.ShadowMatrix;
import NG.Storable;
import NG.Tools.Logger;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3ic;

import java.io.ByteArrayOutputStream;
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
public class BlocksConstruction extends MovingEntity {
    private boolean isDisposed = false;
    private List<BlockSubGrid> subgrids = new ArrayList<>();

    public BlocksConstruction(Vector3fxc position, Quaternionf rotation, float gameTime) {
        this(new FixedState(position, rotation, gameTime));
    }

    public BlocksConstruction(FixedState spawnState) {
        super(spawnState);
        subgrids.add(new BlockSubGrid());
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

        cleanStatesUntil(renderTime);
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
        ShadowMatrix sm = new ShadowMatrix();
        int i = 0;

        for (BlockSubGrid grid : subgrids) {
            sm.pushMatrix();
            sm.translate(grid.getWorldPosition());
            sm.rotate(grid.getWorldRotation());

            for (AbstractPiece piece : grid) {
                // collect points and ensure dest capacity
                List<Vector3fc> points = piece.getShape().getPoints();
                int startInd = i;
                i += points.size();
                while (dest.size() < i){
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
            out.writeUTF(type.name);
        }

        // now write construction to out
        buffer.writeTo(out);
    }

    public BlocksConstruction(DataInputStream in) throws IOException, ClassNotFoundException {
        super(Storable.read(in, State.class));

        int nrOfTypes = in.readInt();
        PieceType[] typeMap = new PieceType[nrOfTypes];
        for (int i = 0; i < nrOfTypes; i++) {
            String pieceName = in.readUTF();
            // TODO maybe add manufacturer or include list of PieceTypeCollections
            typeMap[i] = PieceTypeCollection.cheatCache.get(pieceName);
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

            } else if (block instanceof WheelBasePiece) {
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
