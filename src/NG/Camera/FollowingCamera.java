package NG.Camera;

import NG.Core.Game;
import NG.DataStructures.Tracked.ExponentialSmoothVector;
import NG.Entities.Entity;
import NG.Entities.State;
import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Implementation of a camera that follows a given entity, trying to stay behind this entity
 */
public class FollowingCamera implements Camera {
    /** camera settings */
    private static final Vector3fc RELATIVE_EYE = new Vector3f(-20, 0, 5);
    private static final Vector3fc RELATIVE_FOCUS = new Vector3f(0, 0, 5);
    private static final double EYE_PRESERVE = 1.0E-1;
    private static final double ORIENT_PRESERVE = 1.0E-1;
    private static final float TELEPORT_DISTANCE_SQ = 50f * 50f;

    /**
     * The position of the camera.
     */
    private final ExponentialSmoothVector eye;
    private final ExponentialSmoothVector vecToFocus;
    private final ExponentialSmoothVector up;
    private Entity target;
    private Game game;

    public FollowingCamera(Entity target, Vector3fc initialPosition, Vector3fc initialUp, Vector3fc vecToFocus) {
        this.eye = new ExponentialSmoothVector(initialPosition, EYE_PRESERVE);
        this.vecToFocus = new ExponentialSmoothVector(vecToFocus, ORIENT_PRESERVE);
        this.up = new ExponentialSmoothVector(initialUp, ORIENT_PRESERVE);
        this.target = target;
    }

    public FollowingCamera(Entity target, float gameTime){
        this.target = target;
        State state = target.getStateAt(gameTime);
        Vector3f tPos = state.position().toVector3f();
        Vector3f targetEye = tPos.add(RELATIVE_EYE).rotate(state.orientation());
        Vector3fc targetUp = Vectors.newZ().rotate(state.orientation());
        this.eye = new ExponentialSmoothVector(targetEye, EYE_PRESERVE);
        this.vecToFocus = new ExponentialSmoothVector(new Vector3f(tPos).add(RELATIVE_FOCUS), ORIENT_PRESERVE);
        this.up = new ExponentialSmoothVector(targetUp, ORIENT_PRESERVE);
    }

    @Override
    public void init(Game game) throws Exception {
        this.game = game;
    }

    @Override
    public void cleanup() {
        target = null;
    }

    @Override
    public void onScroll(float value) {
        // ignore
    }

    /**
     * @param deltaTime the animation time difference
     * @param renderTime
     */
    @Override
    public void updatePosition(float deltaTime, float renderTime) {
        if (game == null) return;
        State state = target.getStateAt(renderTime);

        Vector3f targetUp = Vectors.newZ().rotate(state.orientation());
        Vector3f targetPos = state.position().toVector3f();
        Vector3f targetEye = new Vector3f(RELATIVE_EYE)
                .rotate(state.orientation());

        targetEye.add(targetPos);

        Vector3f targetVecToFocus;
        // if the target is removed, look at the place where the target was
        if (target.isDisposed()) {
            targetVecToFocus = new Vector3f(targetPos).sub(targetEye);

        } else {
            targetVecToFocus = new Vector3f(RELATIVE_FOCUS)
                    .rotate(state.orientation())
                    .add(targetPos)
                    .sub(targetEye);
        }

        // teleport camera when too far away
        if (eye.current().distanceSquared(targetEye) > TELEPORT_DISTANCE_SQ) {
            eye.update(targetEye);
        } else {
            eye.updateFluent(targetEye, deltaTime);
        }

        vecToFocus.updateFluent(targetVecToFocus, deltaTime);
        up.updateFluent(targetUp, deltaTime);
    }

    @Override
    public Vector3fc vectorToFocus(){
        return vecToFocus.current();
    }

    @Override
    public Vector3fc getEye() {
        return eye.current();
    }

    @Override
    public Vector3fc getFocus() {
        return getEye().add(vectorToFocus(), new Vector3f());
    }

    @Override
    public Vector3fc getUpVector() {
        return up.current();
    }

    @Override
    public void set(Vector3fc focus, Vector3fc eye) {
        this.eye.update(eye);
        this.vecToFocus.update(new Vector3f(focus).sub(eye));
    }

    @Override
    public boolean isIsometric() {
        return false;
    }
}
