package NG.Entities;

import NG.DataStructures.Vector3fxc;
import NG.Storable;
import org.joml.Quaternionf;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 26-7-2019.
 */
public interface State extends Storable {
    /**
     * @return a copy of this state
     */
    State copy();

    /**
     * @return the position on the time given by {@link #time()}
     */
    Vector3fxc position();

    /**
     * @return the velocity on the time given by {@link #time()}
     */
    Vector3fc velocity();

    /**
     * @return the rotation on the time given by {@link #time()}
     */
    Quaternionf orientation();

    /**
     * @return the time that this state refers to
     */
    float time();

    /**
     * Extrapolates this state for the given gameTime and updates this state accordingly. Modifies this state
     * @param gameTime the time of the updated state
     * @return this
     */
    State update(float gameTime);

    default State interpolateTime(State other, float gameTime) {
        float fraction = (time() - other.time()) / (time() - gameTime);
        return interpolateFraction(other, fraction);
    }

    State interpolateFraction(State other, float fraction);
}
