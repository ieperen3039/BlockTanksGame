package NG.DataStructures.Generic;

/**
 * A queue that allows a producer to queue timed objects, which are valid until the next time stamp
 * @author Geert van Ieperen created on 13-12-2017.
 */
public interface TimedQueue<T> {

    /**
     * add an element to be accessible in the interval [the timeStamp of the previous item, the given timestamp] Items
     * added to the queue with a timestamp less than any previous addition will cause those previous values to be removed
     * @param element   the element that will be returned upon calling {@link #get(float)}
     * @param timeStamp the timestamp in seconds from where this element becomes active
     */
    void add(T element, float timeStamp);

    /**
     * Receive the first element that is accessible, or null if no item is available on the period. All elements with a
     * timestamp before the returned item are no longer accessible. One should not try to call this method with a
     * timeStamp less than any previous call of this instance.
     * @param timeStamp the timestamp in seconds where the returned element is active
     * @return the first element with a timestamp after the given timestamp.
     */
    T get(float timeStamp);

    /**
     * @param timeStamp the timestamp in seconds from where the next element is active
     * @return the difference in timestamp of the given parameter and the next item. All elements with a timestamp
     * before the returned item are no longer accessible, even for {@link #get(float)} if this item does not
     * exist, it returns a negative value equal to minus the time since the last item
     */
    float timeUntilNext(float timeStamp);
}
