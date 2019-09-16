package NG.DataStructures.Interpolation;

import NG.DataStructures.Generic.Pair;
import NG.DataStructures.Generic.TimedArrayQueue;

/**
 * a queue of elements, which can be interpolated based on fractions. The queue can be used as a bridge between a
 * data-generation thread and a visualisation thread
 * @author Geert van Ieperen created on 15-12-2017.
 */
public abstract class LinearInterpolator<T> extends TimedArrayQueue<T> {
    /**
     * @param initialElement this item will initially be placed in the queue twice.
     * @param initialTime    the time of starting
     */
    public LinearInterpolator(T initialElement, float initialTime) {
        super(initialElement, initialTime - 1);
        add(initialElement, initialTime);
    }

    /**
     * crates an interpolator with the first two values already given
     * @param firstElement  the item that occurs first
     * @param firstTime     the time of occurence
     * @param secondElement the item that occurs second
     * @param secondTime    the time of occurence of the second, where first < second
     */
    public LinearInterpolator(T firstElement, float firstTime, T secondElement, float secondTime) {
        super(firstElement, firstTime);
        assert firstTime < secondTime;
        add(secondElement, secondTime);
    }

    /**
     * @return the interpolated object defined by implementation. if there is only one element, the result is equal to
     * {@link #interpolate(Object, Object, float) interpolate(elt, elt, 0)}
     */
    public T getInterpolated(float timeStamp) {
        assert !isEmpty();
        if (size() == 1) {
            Pair<T, Float> elt = getFirst();
            return interpolate(elt.left, elt.left, 0);
        }

        ActiveNext elts = getActiveAndNext(timeStamp);

        float fraction = (timeStamp - elts.firstTime) / (elts.secondTime - elts.firstTime);
        if (Float.isNaN(fraction)) return elts.firstElement;

        return interpolate(elts.firstElement, elts.secondElement, fraction);
    }

    public T getDerivative(float timeStamp) {
        assert !isEmpty();
        if (size() == 1) {
            Pair<T, Float> elt = getFirst();
            return derivative(elt.left, elt.left, 0);
        }

        ActiveNext elts = getActiveAndNext(timeStamp);

        return derivative(elts.firstElement, elts.secondElement, elts.secondTime - elts.firstTime);
    }

    @Override
    public Pair<T, Float> poll() {
        if (size() == 1) return null;
        return super.poll();
    }

    /**
     * interpolate using linear interpolation
     * @return firstElt + (secondElt - firstElt) * fraction
     */
    protected abstract T interpolate(T firstElt, T secondElt, float fraction);

    /**
     * @return the derivative of the two values, when they are {@code timeDifference} seconds apart
     */
    protected abstract T derivative(T firstElt, T secondElt, float timeDifference);
}
