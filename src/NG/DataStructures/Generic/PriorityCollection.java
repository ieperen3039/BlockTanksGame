package NG.DataStructures.Generic;

import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * a collection that only keeps the most relevant elements based on given values.
 * @author Geert van Ieperen created on 22-2-2019.
 */
public class PriorityCollection<T> extends AbstractCollection<T> {
    /** the elements that are remembered */
    private final T[] elements;
    /** the value associated with each element */
    private final float[] values;
    private List<T> eltList;
    private List<Float> valList;

    private int indMinimum = 0;
    private int indMaximum = 0;
    private int size;

    /**
     * create a priority collection of the given class, of the given size
     * @param newType
     * @param maximumSize
     */
    public PriorityCollection(Class<T> newType, int maximumSize) {
        //noinspection unchecked
        this.elements = (T[]) Array.newInstance(newType, maximumSize);
        this.values = new float[maximumSize];
    }

    /**
     * create a priority collection using the given array
     * @param elements an array of the desired size, possibly with elements present
     */
    public PriorityCollection(T[] elements) {
        if (elements == null) {
            throw new IllegalArgumentException("element must be non-null");
        } else if (elements.length == 0) {
            throw new IllegalArgumentException("array of elements must be larger than zero");
        }

        this.elements = elements;
        this.values = new float[elements.length];

        size = 0;
        for (int i = 0; i < elements.length; i++) {
            T elt = elements[i];

            if (elt != null) {
                elements[size++] = elt;
            }
        }
    }

    /**
     * creates a priority collection using the given elements and relevances.
     * @param elements   the elements
     * @param relevances for each element the relevance value
     */
    public PriorityCollection(T[] elements, float[] relevances) {
        if (elements == null || relevances == null) {
            throw new IllegalArgumentException("arguments must be non-null");
        }

        this.elements = elements;
        this.values = relevances;
        this.size = elements.length;

        if (elements.length == 0) {
            throw new IllegalArgumentException("array of elements must be larger than zero");

        } else if (elements.length != relevances.length) {
            throw new IllegalArgumentException("arrays must be of equal length");
        }

        float minimum = relevances[0];
        float maximum = relevances[0];
        for (int i = 1; i < elements.length; i++) {
            if (elements[i] == null) {
                throw new IllegalArgumentException("arguments must be non-null");
            }

            float relevance = relevances[i];
            if (relevance > maximum) {
                maximum = relevance;
                indMaximum = i;
            } else if (relevance < minimum) {
                minimum = relevance;
                indMinimum = i;
            }
        }
    }

    /**
     * adds an element with a given relevance value, and adds this to the collection if the relevance is higher than the
     * least element in the collection.
     * @param newElement the element to add
     * @param relevance  a positive relevance value of this element. When negative, this method returns false and the
     *                   collection is unchanged.
     * @param combiner   this function is used to calculate the new relevance of the element if this element is already
     *                   present in this collection.
     * @return true iff this collection changed as result to this call (as by the Collection specification)
     */
    public boolean add(T newElement, float relevance, BiFunction<Float, Float, Float> combiner) {
        if (relevance < values[indMinimum]) return false;

        // check for null or equal methods
        for (int i = 0; i < size; i++) {
            if (elements[i].equals(newElement)) {
                float newRelevance = combiner.apply(values[i], relevance);
                set(i, newElement, newRelevance);
                return true;
            }
        }

        if (size < elements.length) {
            elements[size] = newElement;
            values[size] = relevance;
            size++;
            return true;
        }

        // relevance >= values[indMinimum] && !elements.contain(newElement)
        set(indMinimum, newElement, relevance);
        return true;
    }

    /**
     * sets the element on the given index. Recalculates the minimum and maximum value of the collection.
     * @param index      the index to place the element
     * @param newElement the new element
     * @param relevance  the absolute value of the element.
     */
    private void set(int index, T newElement, float relevance) {
        float newMinimum = relevance; // new element may replace the current minimum or maximum
        float newMaximum = relevance;
        indMinimum = index;

        for (int i = 0; i < size; i++) {
            if (i == index) {
                // place new element here
                elements[i] = newElement;
                values[i] = relevance;

            } else if (values[i] < newMinimum) {
                newMinimum = values[i];
                indMinimum = i;

            } else if (values[i] > newMaximum) {
                newMaximum = values[i];
                indMaximum = i;
            }
        }
    }

    public T getMostRelevantElement() {
        return elements[indMaximum];
    }

    public float getHighestRelevance() {
        return values[indMaximum];
    }

    /**
     * @param stimulus any simulus
     * @return the relevance of the given stimulus, or 0 of this stimulus is not in this collection. A value of 0 may
     * mean that the stimulus is in the collection, but with 0 relevance.
     */
    public float relevanceOf(T stimulus) {
        for (int i = 0; i < size; i++) {
            if (elements[i].equals(stimulus)) {
                return values[i];
            }
        }

        return 0; // not in this collection => not relevant (and relevances must be positive)
    }

    public void reduceImportance(Function<Float, Float> method) {
        for (int i = 0; i < size; i++) {
            values[i] = method.apply(values[i]);
        }
    }

    public PairList<T, Float> asPairList() {
        if (valList == null) {
            valList = new AbstractList<>() {
                @Override
                public Float get(int index) {
                    return values[index];
                }

                @Override
                public int size() {
                    return size;
                }
            };
        }
        return new PairList<>(getElements(), valList);
    }

    @Override
    public Iterator<T> iterator() {
        return getElements().iterator();
    }

    private List<T> getElements() {
        if (eltList == null) {
            eltList = new AbstractList<>() {
                @Override
                public T get(int index) {
                    return elements[index];
                }

                @Override
                public int size() {
                    return size;
                }
            };
        }
        return eltList;
    }

    /**
     * adds the element with 0 relevance. This is likely to not succeed.
     * @param elt the new element
     * @return true iff this collection changed as result to this method
     */
    @Override
    public boolean add(T elt) {
        return add(elt, 0, Math::max);
    }

    /**
     * add all elements from the given collection to this collection, preserving the relevancy of the elements. This is
     * equal to calling {@link #add(Object, float, BiFunction)} for each element - value pair still in the other
     * collection.
     * @param other another priority collection
     * @return true iff this collection changed as result to this call.
     */
    public boolean addAll(PriorityCollection<? extends T> other) {
        float otherMaximum = other.getHighestRelevance();
        if (otherMaximum < values[indMinimum]) return false;

        boolean didChange = false;
        for (int i = 0; i < other.size; i++) {
            if (add(other.elements[i], other.values[i], Math::max)) {
                didChange = true;
            }
        }

        return didChange;
    }

    public int size() {
        return size;
    }
}
