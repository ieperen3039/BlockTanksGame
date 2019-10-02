package NG.Camera;

import NG.DataStructures.Tracked.ExponentialSmoothVector;
import NG.Entities.MovingEntity;
import org.joml.Vector3fc;

/**
 * a point centered camera that follows a given entity
 * @author Geert van Ieperen created on 1-10-2019.
 */
public class PointFollowingCamera extends PointCenteredCamera {

    private final MovingEntity entity;
    private final ExponentialSmoothVector smoothFocus;

    public PointFollowingCamera(Vector3fc eye, MovingEntity entity, Vector3fc entityPos) {
        super(eye, entityPos);
        this.entity = entity;
        smoothFocus = new ExponentialSmoothVector(getFocus(), 0.01f);
    }

    @Override
    public void updatePosition(float deltaTime, float renderTime) {
        Vector3fc newPos = entity.getStateAt(renderTime).position().toVector3f();
        smoothFocus.updateFluent(newPos, deltaTime);
        focus.set(smoothFocus.current());
        super.updatePosition(deltaTime, renderTime);
    }
}
