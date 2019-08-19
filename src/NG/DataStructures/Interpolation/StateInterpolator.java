package NG.DataStructures.Interpolation;

import NG.DataStructures.Vector3fx;
import NG.Entities.MutableState;
import NG.Entities.State;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * @author Geert van Ieperen created on 14-8-2019.
 */
public class StateInterpolator extends LinearInterpolator<State> {
    public StateInterpolator(int capacity, State initialElement, float initialTime) {
        super(capacity, initialElement, initialTime);
    }

    public StateInterpolator(int capacity, State firstElement, float firstTime, State secondElement, float secondTime) {
        super(capacity, firstElement, firstTime, secondElement, secondTime);
    }

    @Override
    protected State interpolate(State firstElt, State secondElt, float fraction) {
        return firstElt.interpolateFraction(secondElt, fraction);
    }

    @Override
    public State derivative(State firstElt, State secondElt, float timeDifference) {
        return new MutableState(
                secondElt.time() - firstElt.time(),
                new Vector3fx(secondElt.position()).sub(firstElt.position()),
                new Vector3f(secondElt.velocity()).sub(firstElt.velocity()),
                secondElt.orientation().difference(firstElt.orientation(), new Quaternionf())
        );
    }
}
