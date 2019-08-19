package NG.CollisionDetection;

import NG.DataStructures.Generic.AABBi;
import NG.DataStructures.Generic.PairList;
import NG.Tools.Toolbox;

import java.util.*;
import java.util.function.Function;

/**
 * @author Geert van Ieperen created on 18-8-2019.
 */
public class BoxIntersection3i<T> {
    private static final int INSERTION_SORT_BOUND = 64;
    private final Function<T, AABBi> hitboxFunction;

    private HashMap<T, AABBi> cache;
    private T[] xLowerSorted;
    private T[] yLowerSorted;
    private T[] zLowerSorted;
    private List<T> newElements = new ArrayList<>();

    public BoxIntersection3i(T[] elements, Function<T, AABBi> hitboxFunction) {
        this.hitboxFunction = hitboxFunction;
        this.cache = new HashMap<>(elements.length);
        this.xLowerSorted = elements.clone();
        this.yLowerSorted = elements.clone();
        this.zLowerSorted = elements.clone();

        updateCache();

        sort(xLowerSorted, yLowerSorted, zLowerSorted, false);
    }

    public void sort(T[] xLowerSorted, T[] yLowerSorted, T[] zLowerSorted, boolean isAlmostSorted) {
        if (isAlmostSorted || xLowerSorted.length < INSERTION_SORT_BOUND) {
            Toolbox.insertionSort(xLowerSorted, Comparator.comparing(t -> cache.get(t).xMin));
            Toolbox.insertionSort(yLowerSorted, Comparator.comparing(t -> cache.get(t).yMin));
            Toolbox.insertionSort(zLowerSorted, Comparator.comparing(t -> cache.get(t).zMin));

        } else {
            Arrays.sort(xLowerSorted, Comparator.comparing(t -> cache.get(t).xMin));
            Arrays.sort(yLowerSorted, Comparator.comparing(t -> cache.get(t).yMin));
            Arrays.sort(zLowerSorted, Comparator.comparing(t -> cache.get(t).zMin));
        }
    }

    public void add(T block) {
        newElements.add(block);
    }

    public void addAll(List<T> elements){
        newElements.addAll(elements);
    }

    private void mergeNewElements(List<T> newElements) {
        int nrOfNewElts = newElements.size();
        if (nrOfNewElts == 0) return;

        T[] newXSort = Arrays.copyOf(array(), nrOfNewElts);
        T[] newYSort = Arrays.copyOf(array(), nrOfNewElts);
        T[] newZSort = Arrays.copyOf(array(), nrOfNewElts);

        for (int i = 0; i < nrOfNewElts; i++) {
            T elt = newElements.get(i);
            newXSort[i] = elt;
            newYSort[i] = elt;
            newZSort[i] = elt;
        }

        sort(newXSort, newYSort, newZSort, false);

        xLowerSorted = Toolbox.mergeArrays(xLowerSorted, newXSort, t -> cache.get(t).xMin);
        yLowerSorted = Toolbox.mergeArrays(yLowerSorted, newYSort, t -> cache.get(t).yMin);
        zLowerSorted = Toolbox.mergeArrays(zLowerSorted, newZSort, t -> cache.get(t).zMin);
    }

    /**
     * generate a list (possibly empty) of all pairs of objects that may have collided. This can include (parts of) the
     * ground, but not an object with itself. One pair does not occur the other way around.
     * @return a collection of pairs of objects that are close to each other
     */
    private PairList<T, T> getIntersectingPairs() {
        sort(xLowerSorted, yLowerSorted, zLowerSorted, true);
        mergeNewElements(newElements);
        newElements.clear();

        T[] entityArray = array();
        AdjacencyMatrix<T> adjacencies = new AdjacencyMatrix<>(entityArray, 3);
        adjacencies.checkOverlap(xLowerSorted, t -> cache.get(t).xMin, t -> cache.get(t).xMax, (a, b) -> true);
        adjacencies.checkOverlap(yLowerSorted, t -> cache.get(t).yMin, t -> cache.get(t).yMax, (a, b) -> true);
        adjacencies.checkOverlap(zLowerSorted, t -> cache.get(t).zMin, t -> cache.get(t).zMax, (a, b) -> true);

        int nrOfElts = adjacencies.nrOfFoundElements();

        PairList<T, T> allEntityPairs = new PairList<>(nrOfElts);
        adjacencies.forEach((i, j) -> allEntityPairs.add(
                entityArray[i], entityArray[j]
        ));

        return allEntityPairs;
    }

    private T[] array() {
        return xLowerSorted;
    }

    public void updateCache() {
        for (T elt : array()) {
            cache.put(elt, hitboxFunction.apply(elt));
        }
    }
}
