package NG.Blocks;

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
import NG.Tools.GridRayScanner;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;

import java.util.List;

import static NG.Blocks.Types.BlockPiece.BLOCK_BASE;
import static NG.Blocks.Types.BlockPiece.BLOCK_HEIGHT;

/**
 * A block grid as entity
 * @author Geert van Ieperen created on 14-8-2019.
 */
public class BlocksConstruction extends BlockSubGrid implements MovingEntity {
    private boolean isDisposed = false;
    private State state;

    public BlocksConstruction() {
        this(new Vector3fx(), 0);
    }

    public BlocksConstruction(Vector3fxc position, float gameTime) {
        this.state = new MutableState(gameTime, position);
    }

    public BlocksConstruction(PieceType initial) {
        this(initial, new Vector3fx(), 0);
    }

    public BlocksConstruction(PieceType initial, Vector3fx position, int gameTime) {
        this(position, gameTime);
        add(initial, new Vector3i(), Color4f.WHITE);
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }

    @Override
    public void draw(SGL gl, float renderTime) {
        gl.pushMatrix();
        {
            gl.translateRotate(state);
            draw(gl, this);
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

}
