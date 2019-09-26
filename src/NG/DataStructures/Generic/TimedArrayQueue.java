package NG.DataStructures.Generic;

import java.util.AbstractQueue;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.BiConsumer;

/**
 * A queue of elements. This collection is synchronized.
 * @author Geert van Ieperen created on 12-2-2019.
 */
public class TimedArrayQueue<T> extends AbstractQueue<Pair<T, Float>> {
    // for timestamps: head < tail
    private ArrayDeque<Float> timeStamps;
    private ArrayDeque<T> elements;

    public TimedArrayQueue() {
        timeStamps = new ArrayDeque<>();
        elements = new ArrayDeque<>();
    }

    /**
     * @param initial
     * @param startTime the time at which this element is started.
     */
    public TimedArrayQueue(T initial, float startTime) {
        this();
        offer(initial, startTime);
    }

    @Override
    public boolean offer(Pair<T, Float> pair) {
        return offer(pair.left, pair.right);
    }

    /**
     * Inserts the specified element into this queue. This method is generally preferable to {@link #add}, which can
     * fail to insert an element only by throwing an exception. Items added to the queue with a timestamp less than any
     * previous addition will cause those previous values to be removed.
     * @param element   the element to add
     * @param timeStamp the start time of the element
     * @return {@code true} if the element was added to this queue, {@code false} if the given element does not follow
     * the previous last element.
     */
    public boolean offer(T element, float timeStamp) {
        synchronized (this) {
            // act as refinement
            while (!timeStamps.isEmpty() && timeStamps.peekLast() > timeStamp) {
                timeStamps.removeLast();
                elements.removeLast();
            }

            timeStamps.add(timeStamp);
            elements.add(element);

        }
        return true;
    }

    /**
     * @see #offer(T, float)
     */
    public boolean add(T element, float startTime) {
        if (offer(element, startTime)) {
            return true;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Pair<T, Float> poll() {
        synchronized (this) {
            if (isEmpty()) return null;
            return new Pair<>(elements.poll(), timeStamps.poll());
        }
    }

    @Override
    public Pair<T, Float> peek() {
        synchronized (this) {
            if (isEmpty()) return null;
            return new Pair<>(elements.peek(), timeStamps.peek());
        }
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * remove all actions from the head of this queue, which happen before the given time.
     * @param time the time until where to remove actions, exclusive.
     */
    public void removeUntil(float time) {
        if (isEmpty()) return;

        if (time > timeStamps.peekLast()) {
            clear();
            return;
        }

        // a currently executing element...
        synchronized (this) {
            Iterator<Float> times = timeStamps.iterator();
            Iterator<T> things = elements.iterator();

            while (times.hasNext() && times.next() < time) {
                times.remove();
                things.next();
                things.remove();
            }
        }
    }

    /**
     * inserts an element into the queue, leaving any future element unchanged
     * @param element   the element to do
     * @param startTime the moment of interrupt
     */
    public void insert(T element, float startTime) {
        synchronized (this) {
            if (timeStamps.isEmpty() || startTime > timeStamps.peekLast()) {
                add(element, startTime);
                return;
            }

            if (startTime < timeStamps.peekFirst()) {
                timeStamps.addFirst(startTime);
                elements.addFirst(element);
                return;
            }

            // a currently executing element...
            PairList<T, Float> buffer = new PairList<>();

            while (timeStamps.peekLast() < startTime) {
                buffer.add(elements.removeLast(), timeStamps.removeLast());
            }

            add(element, startTime);
            buffer.forEach((BiConsumer<T, Float>) this::add);
        }
    }

    /**
     * finds the element that is active at the given time, and returns this with its timestamp.
     * @param gameTime a moment in time
     * @return a pair with on left the element that is active at the given time, and on right the timestamp of this
     * element. If no element has started, return the first element.
     */
    public Pair<T, Float> get(float gameTime) {
        synchronized (this) {
            if (isEmpty()) {
                return null;
            }

            if (gameTime < timeStamps.peekFirst()) {
                return getFirst();

            } else if (gameTime > timeStamps.peekLast()) {
                return getLast();

            } else {
                // a currently executing element...
                Iterator<Float> times = timeStamps.iterator();
                Iterator<T> things = elements.iterator();
                T element;
                float currentTime;
                float nextTime = times.next();
                do {
                    element = things.next();
                    currentTime = nextTime;
                    nextTime = times.next();
                } while (nextTime < gameTime);

                return new Pair<>(element, currentTime);
            }
        }
    }

    /**
     * returns the element that is active later than the given time stamp
     * @param gameTime the time to query
     * @return the next element executing later than the give time stamp, or the last element if no such element exists.
     */
    public Pair<T, Float> getNext(float gameTime) {
        synchronized (this) {
            if (isEmpty()) {
                return null;
            }

            if (gameTime < timeStamps.peekFirst()) {
                return getFirst();

            } else if (gameTime > timeStamps.peekLast()) {
                return getLast();

            } else {
                // a currently executing element...
                Iterator<Float> times = timeStamps.iterator();
                Iterator<T> things = elements.iterator();
                T nextElt;
                float nextTime;

                do {
                    nextElt = things.next();
                    nextTime = times.next();
                } while (nextTime < gameTime);

                return new Pair<>(nextElt, nextTime);
            }
        }
    }

    protected ActiveNext getActiveAndNext(float gameTime) {
        synchronized (this) {
            assert timeStamps.size() > 1;
            // a currently executing element...
            Iterator<Float> times = timeStamps.iterator();
            Iterator<T> things = elements.iterator();
            T element;
            float currentTime;

            T nextElt = things.next();
            float nextTime = times.next();
            do {
                element = nextElt;
                nextElt = things.next();
                currentTime = nextTime;
                nextTime = times.next();
            } while (nextTime < gameTime && things.hasNext());

            return new ActiveNext(element, currentTime, nextElt, nextTime);
        }
    }

    protected class ActiveNext {
        public final T firstElement;
        public final float firstTime;
        public final T secondElement;
        public final float secondTime;

        private ActiveNext(T firstElement, float firstTime, T secondElement, float secondTime) {
            this.firstElement = firstElement;
            this.firstTime = firstTime;
            this.secondElement = secondElement;
            this.secondTime = secondTime;
        }
    }

    public Pair<T, Float> getFirst() {
        synchronized (this) {
            return new Pair<>(elements.getFirst(), timeStamps.getFirst());
        }
    }

    public Pair<T, Float> getLast() {
        synchronized (this) {
            return new Pair<>(elements.getLast(), timeStamps.getLast());
        }
    }

    @Override
    public Iterator<Pair<T, Float>> iterator() {
        Iterator<Float> times;
        Iterator<T> things;

        synchronized (this) {
            times = new ArrayList<>(timeStamps).iterator();
            things = new ArrayList<>(elements).iterator();
        }

        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return times.hasNext();
            }

            @Override
            public Pair<T, Float> next() {
                return new Pair<>(things.next(), times.next());
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("ActionQueue: ");

        synchronized (this) {
            Iterator<Float> times = timeStamps.iterator();
            Iterator<T> things = elements.iterator();

            while (times.hasNext()) {
                str.append("[");
                str.append(times.next());
                str.append(" : ");
                str.append(things.next());
                str.append("], ");
            }
        }

        str.delete(str.length() - 2, str.length());

        return str.toString();
    }
}
