package NG.Camera;

import NG.CollisionDetection.BoundingBox;
import NG.CollisionDetection.Collision;
import NG.DataStructures.Vector3fx;
import NG.Entities.Entity;
import NG.Entities.FixedState;
import NG.Entities.State;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * an entity that can't be collided with. Only the drawing method has to be implemented
 * @author Geert van Ieperen created on 4-9-2019.
 */
public abstract class DummyEntity implements Entity {
    private static final FixedState NULL_STATE = new FixedState(new Vector3fx(), new Quaternionf());
    private boolean isDisposed = false;

    public void update(float gameTime) {
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
    public BoundingBox getHitbox() {
        return new BoundingBox(
                Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY,
                Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY
        );
    }

    @Override
    public Collision getIntersection(Vector3fc origin, Vector3fc direction) {
        return Collision.NONE;
    }

    @Override
    public List<Vector3f> getShapePoints(List<Vector3f> dest) {
        dest.clear();
        return dest;
    }

    @Override
    public State getCurrentState() {
        return NULL_STATE;
    }

    @Override
    public void collideWith(Entity other, Collision collision, float collisionTime) {
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return false;
    }

    @Override
    public void writeToDataStream(DataOutputStream out) throws IOException {
    }
}
