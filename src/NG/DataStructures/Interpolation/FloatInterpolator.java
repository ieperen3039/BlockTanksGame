package NG.DataStructures.Interpolation;

/**
 * @author Geert van Ieperen created on 15-12-2017.
 */
public class FloatInterpolator extends LinearInterpolator<Float> {

    public FloatInterpolator(int capacity, Float initialValue, float initialTime) {
        super(capacity, initialValue, initialTime);
    }

    @Override
    protected Float interpolate(Float firstElt, Float secondElt, float fraction) {
        float difference = secondElt - firstElt;

        return firstElt + (difference * fraction);
    }

    @Override
    public Float derivative(Float firstElt, Float secondElt, float timeDifference) {
        float dx = secondElt - firstElt;
        return dx / timeDifference;
    }
}
