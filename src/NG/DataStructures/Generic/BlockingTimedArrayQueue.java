package NG.DataStructures.Generic;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link TimedQueue} that uses ArrayDeque for implementation. Includes synchronized adding and deletion. Items added
 * to the queue with a timestamp less than the previous addition will cause the previous value to be removed
 * @author Geert van Ieperen created on 13-12-2017.
 */
public class BlockingTimedArrayQueue<T> implements TimedQueue<T>, Serializable {

    /** prevents race-conditions upon adding and removing */
    private Lock changeGuard;

    /** timestamps in seconds. Private, as semaphore must be handled */
    private Deque<Float> timeStamps;
    private Deque<T> elements;

    /**
     * @param capacity the initial expected maximum number of entries
     */
    public BlockingTimedArrayQueue(int capacity) {
        timeStamps = new ArrayDeque<>(capacity);
        elements = new ArrayDeque<>(capacity);
        changeGuard = new ReentrantLock();
    }

    @Override
    public void add(T element, float timeStamp) {
        changeGuard.lock();

        // act as refinement
        while (!timeStamps.isEmpty() && timeStamps.peekLast() > timeStamp) {
            timeStamps.removeLast();
            elements.removeLast();
        }

        timeStamps.add(timeStamp);
        elements.add(element);
        changeGuard.unlock();
    }

    @Override
    public T get(float timeStamp) {
        removeUntil(timeStamp);
        // if (activeTimeStamp < timeStamp), there is no element available
        return (nextTimeStamp() < timeStamp) ? null : nextElement();
    }

    @Override
    public float timeUntilNext(float timeStamp) {
        return (nextTimeStamp() - timeStamp);
    }

    /**
     * Removes all elements with time value less than the given time stamp
     * <p>
     * Upon returning, {@link #nextTimeStamp()} > timeStamp or there exist no item with a timestamp higher than the given timestamp.
     * @param timeStamp the time until where the state of the queue should be updated.
     */
    public void removeUntil(float timeStamp) {
        if (nextTimeStamp() > timeStamp) return;

        changeGuard.lock();

        while ((timeStamps.size() > 1) && (timeStamp > nextTimeStamp())) {
            progress();
        }
        changeGuard.unlock();
    }

    /**
     * unsafe progression of the queue
     */
    protected void progress() {
        timeStamps.remove();
        elements.remove();
    }

    /** returns the next queued timestamp in seconds or null if there is none */
    public Float nextTimeStamp() {
        return timeStamps.peek();
    }

    /** returns the next queued element or null if there is none */
    public T nextElement() {
        return elements.peek();
    }

    @Override
    public String toString() {
        Iterator<Float> times = timeStamps.iterator();
        Iterator elts = elements.iterator();

        StringBuilder s = new StringBuilder();
        s.append("TimedArray:");
        while (times.hasNext()) {
            s.append("\n");
            s.append(String.format("%1.04f", times.next()));
            s.append(" > ");
            s.append(elts.next());
        }

        return s.toString();
    }
}
