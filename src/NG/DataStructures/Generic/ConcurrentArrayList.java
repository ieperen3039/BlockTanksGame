package NG.DataStructures.Generic;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * An {@link ArrayList} wrapper that uses a {@link ReadWriteLock} for access. An alternative of {@link
 * java.util.Collections#synchronizedList(List)} of arrayLists
 * @author Geert van Ieperen created on 1-3-2018.
 */
@SuppressWarnings("NullableProblems")
public class ConcurrentArrayList<T> implements Collection<T> {
    private Lock readLock;
    private Lock writeLock;
    private ArrayList<T> list;

    /**
     * an array that is completely thread-safe, with separate locks for reading and writing
     */
    public ConcurrentArrayList() {
        ReadWriteLock master = new ReentrantReadWriteLock(false);
        readLock = master.readLock();
        writeLock = master.writeLock();
        list = new ArrayList<>();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        readLock.lock();
        try {
            return list.contains(o);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Iterator<T> iterator() {
        readLock.lock();
        try {
            return new Itr();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Object[] toArray() {
        readLock.lock();
        try {
            return list.toArray();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        readLock.lock();
        try {
            return list.toArray(a);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean add(T t) {
        writeLock.lock();
        try {
            return list.add(t);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean remove(Object o) {
        writeLock.lock();
        try {
            return list.remove(o);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        readLock.lock();
        try {
            return list.containsAll(c);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        writeLock.lock();
        try {
            return list.addAll(c);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        writeLock.lock();
        try {
            return list.removeAll(c);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        writeLock.lock();
        try {
            return list.retainAll(c);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Spliterator<T> spliterator() {
        return copy().spliterator();
    }

    public ArrayList<T> copy() {
        readLock.lock();
        try {
            return new ArrayList<>(list);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Stream<T> parallelStream() {
        return copy().parallelStream();
    }

    @Override
    public Stream<T> stream() {
        return copy().stream();
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            list.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        writeLock.lock();
        try {
            return list.removeIf(filter);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public String toString() {
        return list.toString();
    }

    /**
     * iterator for a copy of this list
     */
    private class Itr implements Iterator<T> {
        int size = list.size();
        Object[] elementData = list.toArray(); // creates a copy of the array.
        int cursor = 0;       // index of next element to return

        public boolean hasNext() {
            return cursor < size;
        }

        @SuppressWarnings("unchecked")
        public T next() {
            return (T) elementData[cursor++];
        }
    }
}
