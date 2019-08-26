package NG.Blocks;

import NG.Blocks.Types.BlockPiece;
import NG.Blocks.Types.JointPiece;
import NG.Blocks.Types.PieceType;
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
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3ic;

import java.util.ArrayList;
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
                subgrid.draw(gl, this);
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
        if (subgrids.isEmpty()) return new BoundingBox();

        return new BoundingBox( // TODO calculate hitbox including rotation and subgrids
                50, 50, 50, 50, 50, 50
//                bounds.xMin * BLOCK_BASE,
//                bounds.yMin * BLOCK_BASE,
//                bounds.zMin * BLOCK_HEIGHT,
//
//                bounds.xMax * BLOCK_BASE,
//                bounds.yMax * BLOCK_BASE,
//                bounds.zMax * BLOCK_HEIGHT
        );
    }

    @Override
    public Collision getIntersection(Vector3fc origin, Vector3fc direction) {
        Collision intersection = Collision.NONE;

        for (BlockSubGrid subgrid : subgrids) {
            Collision next = subgrid.getRayScanner()
                    .getIntersection(origin, direction, false);

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

    public class GridModificator {
        BlockSubGrid target;
        int index = 0;

        private GridModificator() {
            validateCache();
        }

        ;

        public void add(PieceType type, Vector3ic position, Color4f color) {
            add(type.getInstance(position, 0, color));
        }

        public void add(BlockPiece block) {
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

        public boolean canAttach(BlockPiece element) {
            // TODO check overlap with other subgrids
            return target.canAttach(element);
        }

        public BlockSubGrid getGrid() {
            return target;
        }
    }

    @Override
    public void dispose() {
        isDisposed = true;
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }

}
