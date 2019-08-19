package NG.Camera;

import NG.Core.Game;
import NG.Core.GameTimer;
import NG.DataStructures.Tracked.ExponentialSmoothVector;
import NG.Entities.Entity;
import NG.Entities.State;
import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen
 * created on 25-12-2017.
 */
public class MountedCamera implements Camera {

    private final Entity target;
    private final ExponentialSmoothVector toFocus;
    private Vector3f relativeFocus;
    private Vector3f eye = new Vector3f();
    private Vector3f up = new Vector3f();

    public MountedCamera(Entity target, Vector3f relativeFocus) {
        this.target = target;
        this.toFocus = new ExponentialSmoothVector(Vectors.O, 0.002f);
        this.relativeFocus = relativeFocus;
    }

    @Override
    public void init(Game game) throws Exception {
        updatePosition(0, game.get(GameTimer.class).getRendertime());
    }

    @Override
    public Vector3fc vectorToFocus() {
        return toFocus.current();
    }

    @Override
    public void updatePosition(float deltaTime, float renderTime) {
        State state = target.getStateAt(renderTime);
        Vector3f focus = relativeFocus.rotate(state.orientation());
        toFocus.updateFluent(focus, deltaTime);
        state.position().toVector3f(eye);
        up = Vectors.Z.rotate(state.orientation(), up);
    }

    @Override
    public Vector3fc getEye() {
        return eye;
    }

    @Override
    public Vector3fc getFocus() {
        return new Vector3f(eye).add(vectorToFocus());
    }

    @Override
    public Vector3f getUpVector() {
        return up;
    }

    /**
     * sets the relative focus to {@code focus - eye}
     */
    @Override
    public void set(Vector3fc focus, Vector3fc eye) {
        focus.sub(eye, relativeFocus);
    }

    @Override
    public boolean isIsometric() {
        return false;
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void onScroll(float value) {

    }
}
