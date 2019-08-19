package NG.CollisionDetection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * tracks how often pairs of integers are added, and allows querying whether a given pair has been added at least a
 * given number of times.
 */
public class AdjacencyMatrix<Type> {
    private Function<Type, Integer> idMap;
    private Map<Integer, Map<Integer, Integer>> relations;
    private Map<Integer, Set<Integer>> found;
    private int depth;

    /**
     * @param nrOfElements maximum value that can be added
     * @param depth        how many times a number must be added to trigger {@link #has(int, int)}. For 3-coordinate
     * @param idMap a mapping for the elements to a unique integer value i the range of [0 ... nrOfElements)
     */
    public AdjacencyMatrix(int nrOfElements, int depth, Function<Type, Integer> idMap) {
        this.relations = new HashMap<>(nrOfElements);
        this.idMap = idMap;
        this.found = new HashMap<>();
        this.depth = depth;
    }

    /**
     * creates a hashmap-based backing for calculating item indices
     * @param depth
     */
    public AdjacencyMatrix(Type[] elements, int depth) {
        this(elements.length, depth, getIndexMap(elements));
    }

    /**
     * creates a mapping from the elements of the array to their respective indices.
     * @param elements an array of elements
     * @return a mapping of an elements to its index in the array. Changes in the array are not reflected.
     */
    public static <T> Function<T, Integer> getIndexMap(T[] elements){
        HashMap<T, Integer> idMap = new HashMap<>();
        for (int i = 0; i < elements.length; i++) {
            idMap.put(elements[i], i);
        }
        return idMap::get;
    }

    public void add(int i, int j) {
        if (j > i) {
            int t = i;
            i = j;
            j = t;
        }

        Map<Integer, Integer> firstSide = relations.computeIfAbsent(i, HashMap::new);
        int newValue = firstSide.getOrDefault(j, 0) + 1;
        if (newValue == depth) {
            found.computeIfAbsent(i, HashSet::new).add(j);
        }
        firstSide.put(j, newValue);
    }

    public boolean has(int i, int j) {
        return found.containsKey(i) && found.get(i).contains(j);
    }

    public void forEach(BiConsumer<Integer, Integer> action) {
        for (Integer i : found.keySet()) {
            for (Integer j : found.get(i)) {
                action.accept(i, j);
            }
        }
    }

    public int nrOfFoundElements() {
        int count = 0;
        for (Set<Integer> integers : found.values()) {
            count += integers.size();
        }
        return count;
    }

    /**
     * iterating over the sorted array, increase the value of all pairs that have coinciding intervals
     * @param sortedArray an array sorted in increasing order on the lower mapping
     * @param lower       a function that maps to the lower value of the interval of the entity
     * @param upper       a function that maps an entity to its upper interval
     */
    protected <Key extends Comparable<Key>> void checkOverlap(
            Type[] sortedArray, Function<Type, Key> lower, Function<Type, Key> upper, BiPredicate<Type, Type> checker
    ) {
        // INVARIANT:
        // all items i where i.lower < source.lower, are already added to the matrix

        int nOfItems = sortedArray.length;
        for (int i = 0; i < (nOfItems - 1); i++) {
            Type subject = sortedArray[i];

            // increases the checks count of every source with index less than i, with position less than the given minimum
            int j = i + 1;
            Type target = sortedArray[j++];

            // while the lowerbound of target is less than the upperbound of our subject
            while (lower.apply(target).compareTo(upper.apply(subject)) <= 0) {
                if (checker.test(subject, target) && checker.test(target, subject)) {
                    add(idMap.apply(target), idMap.apply(subject));
                }

                if (j == nOfItems) break;
                target = sortedArray[j++];
            }
        }
    }
}
