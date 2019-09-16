package NG.DataStructures.Interpolation;

import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 5-2-2019.
 */
public class VectorInterpolator extends LinearInterpolator<Vector3fc> {

    public VectorInterpolator(Vector3fc initialVector, float initialTime) {
        super(initialVector, initialTime);
    }

    /**
     * creates an interpolator with two values already set. Make sure that firstTime < secondTime
     */
    public VectorInterpolator(
            Vector3fc firstElement, float firstTime, Vector3fc secondElement, float secondTime
    ) {
        super(firstElement, firstTime, secondElement, secondTime);
    }

    protected Vector3fc interpolate(Vector3fc firstElt, Vector3fc secondElt, float fraction) {
        return new Vector3f(firstElt).lerp(secondElt, fraction);
    }

    @Override
    public Vector3fc derivative(Vector3fc firstElt, Vector3fc secondElt, float timeDifference) {
        Vector3f dx = new Vector3f(secondElt).sub(firstElt);
        return dx.mul(1 / timeDifference);
    }
}
