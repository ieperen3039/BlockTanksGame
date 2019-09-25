package NG.Camera;

import NG.CollisionDetection.BoundingBox;
import NG.CollisionDetection.Collision;
import NG.DataStructures.Vector3fx;
import NG.Entities.Entity;
import NG.Entities.FixedState;
import NG.Entities.StaticEntity;
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
public abstract class DummyEntity extends StaticEntity {
    private static final FixedState NULL_STATE = new FixedState(new Vector3fx(), new Quaternionf());

    public DummyEntity() {
        super(NULL_STATE);
    }

    @Override
    public BoundingBox getHitbox(float time) {
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
    public List<Vector3f> getShapePoints(List<Vector3f> dest, float gameTime) {
        dest.clear();
        return dest;
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return false;
    }

    @Override
    public void writeToDataStream(DataOutputStream out) throws IOException {
        throw new UnsupportedOperationException();
    }
}
