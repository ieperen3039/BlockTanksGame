package NG.DataStructures.Interpolation;

import org.joml.Quaternionf;

/**
 * @author Geert van Ieperen created on 22-12-2017.
 */
public class QuaternionInterpolator extends LinearInterpolator<Quaternionf> {

    private static final float DOT_THRESHOLD = 0.125f;

    /**
     * @param initialItem
     * @param initialTime
     */
    public QuaternionInterpolator(Quaternionf initialItem, float initialTime) {
        super(initialItem, initialTime);
    }

    @Override
    protected Quaternionf interpolate(Quaternionf firstElt, Quaternionf secondElt, float fraction) {
        final Quaternionf result = new Quaternionf();

        if (firstElt.dot(secondElt) > DOT_THRESHOLD) {
            firstElt.nlerpIterative(secondElt, fraction, DOT_THRESHOLD, result);
        } else {
            firstElt.nlerp(secondElt, fraction, result);
        }
        return result;
    }

    @Override
    public Quaternionf derivative(Quaternionf firstElt, Quaternionf secondElt, float timeDifference) {
        Quaternionf dx = secondElt.difference(firstElt, new Quaternionf());
        return dx.scale(1 / timeDifference);
    }
}
