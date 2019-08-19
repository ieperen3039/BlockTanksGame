package NG.DataStructures.Interpolation;

import NG.DataStructures.Generic.BlockingTimedArrayQueue;

/**
 * @author Geert van Ieperen created on 15-12-2017.
 */
public abstract class LinearInterpolator<T> extends BlockingTimedArrayQueue<T> {
    private double activeTime;
    protected T activeElement;

    /**
     * @param capacity       the expected maximum number of entries
     * @param initialElement this item will initially be placed in the queue twice.
     * @param initialTime    the time of starting
     */
    public LinearInterpolator(int capacity, T initialElement, float initialTime) {
        super(capacity);
        add(initialElement, initialTime - 1);
        activeTime = initialTime;
        activeElement = initialElement;
    }

    /**
     * crates an interpolator with the first two values already given
     * @param capacity      the expected maximum number of entries
     * @param firstElement  the item that occurs first
     * @param firstTime     the time of occurence
     * @param secondElement the item that occurs second
     * @param secondTime    the time of occurence of the second, where first < second
     */
    public LinearInterpolator(int capacity, T firstElement, float firstTime, T secondElement, float secondTime) {
        super(capacity);
        add(secondElement, secondTime);
        activeTime = firstTime;
        activeElement = firstElement;
    }

    /**
     * @return the interpolated object defined by implementation
     */
    public T getInterpolated(float timeStamp) {
        removeUntil(timeStamp);

        double firstTime = activeTime;
        T firstElt = activeElement;
        double secondTime = nextTimeStamp();
        T secondElt = nextElement();

        if (firstElt == null) {
            firstElt = secondElt;
        }

        float fraction = (float) ((timeStamp - firstTime) / (secondTime - firstTime));
        if (Float.isNaN(fraction)) return firstElt;

        return interpolate(firstElt, secondElt, fraction);
    }

    public T getDerivative(float timeStamp) {
        removeUntil(timeStamp);

        double firstTime = activeTime;
        T firstElt = activeElement;
        double secondTime = nextTimeStamp();
        T secondElt = nextElement();

        if (firstElt == null) {
            firstElt = secondElt;
        }

        return derivative(firstElt, secondElt, (float) (secondTime - firstTime));
    }
    /**
     * interpolate using linear interpolation
     * @return firstElt + (secondElt - firstElt) * fraction
     */
    protected abstract T interpolate(T firstElt, T secondElt, float fraction);

    @Override
    protected void progress() {
        activeTime = nextTimeStamp();
        activeElement = nextElement();
        super.progress();
    }

    @Override
    public String toString() {
        final String backend = super.toString();
        return backend.replaceFirst("\n", "\n" + String.format("%1.04f", activeTime) + " > " + activeElement + "\n");
    }

    /**
     * @return the derivative of the two values, when they are {@code timeDifference} seconds apart
     */
    protected abstract T derivative(T firstElt, T secondElt, float timeDifference);
}
