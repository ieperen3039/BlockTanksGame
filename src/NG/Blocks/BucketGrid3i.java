package NG.Blocks;

import NG.DataStructures.Generic.AABBi;
import NG.DataStructures.Generic.Pair;
import org.joml.Vector3ic;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author Geert van Ieperen created on 18-8-2019.
 */
public class BucketGrid3i<T> implements Iterable<T> {
    private final InfiniteGrid<Element> grid;
    private int bucketSize;
    private int size = 0;

    /**
     * @param bucketSize a large bucket size makes queries longer, a small bucket size makes edits longer
     */
    public BucketGrid3i(int bucketSize) {
        this.grid = new InfiniteGrid<>();
        this.bucketSize = bucketSize;
    }

    /**
     * creates a new bucketgrid with the same elements as this grid, but with the new given bucket size
     * @param newBucketSize a large bucket size allows efficient editing, a small bucket size allows efficient querying
     * @return a copy of this bucketgrid, but having the new given bucket size
     */
    public BucketGrid3i<T> rebucket(int newBucketSize) { // TODO more efficient implementation
        BucketGrid3i<T> newGrid = new BucketGrid3i<>(newBucketSize);
        for (ListGrid<Element> list : grid) {
            for (Element elt : list) {
                newGrid.add(elt.right, elt.left);
            }
        }
        return newGrid;
    }

    private List<Element> bucketOf(int x, int y, int z) {
        return grid.get(x / bucketSize, y / bucketSize, z / bucketSize);
    }

    private List<List<Element>> bucketsOf(AABBi hitBox) {
        List<List<Element>> result = new ArrayList<>(/*product of differences*/);
        int xMin = hitBox.xMin / bucketSize;
        int yMin = hitBox.yMin / bucketSize;
        int zMin = hitBox.zMin / bucketSize;
        int xMax = hitBox.xMax / bucketSize;
        int yMax = hitBox.yMax / bucketSize;
        int zMax = hitBox.zMax / bucketSize;
        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    result.add(grid.get(x, y, z));
                }
            }
        }

        return result;
    }

    public void add(T element, AABBi hitBox) {
        Element e = new Element(hitBox, element);
        int xMin = hitBox.xMin / bucketSize;
        int yMin = hitBox.yMin / bucketSize;
        int zMin = hitBox.zMin / bucketSize;
        int xMax = hitBox.xMax / bucketSize;
        int yMax = hitBox.yMax / bucketSize;
        int zMax = hitBox.zMax / bucketSize;

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    grid.add(x, y, z, e);
                }
            }
        }

        size++;
    }

    public List<T> get(AABBi range) {
        List<T> results = new ArrayList<>();
        List<List<Element>> buckets = bucketsOf(range);

        for (List<Element> bucket : buckets) {
            for (Element elt : bucket) {
                if (elt.left.intersects(range)) {
                    results.add(elt.right);
                }
            }
        }
        return results;
    }

    public T get(Vector3ic position) {
        return get(position.x(), position.y(), position.z());
    }

    public T get(int x, int y, int z) {
        for (Element elt : bucketOf(x, y, z)) {
            if (elt.left.intersects(x, y, z)) {
                return elt.right;
            }
        }
        return null;
    }

    public void remove(T element, AABBi hitBox) {
        for (List<Element> list : bucketsOf(hitBox)) {
            list.removeIf(elt -> elt.right.equals(element));
        }

        size--;
    }

    public Collection<T> values() {
        HashSet<T> set = new HashSet<>();
        for (ListGrid<Element> listGrid : grid) {
            for (Element elt : listGrid) {
                set.add(elt.right);
            }
        }


        HashSet<T> check = new HashSet<>();
        for (ListGrid<Element> listGrid : grid) {
            listGrid.forEach(e -> check.add(e.right));
        }

        assert set.containsAll(check) && check.containsAll(set) : String.format("%s \n%s", set, check);

        return set;
    }

    @Override
    public Iterator<T> iterator() {
        return values().iterator();
    }

    public int size() {
        return size;
    }

    public List<T> get(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
        return get(new AABBi(xMin, yMin, zMin, xMax, yMax, zMax));
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private class Element extends Pair<AABBi, T> {
        private Element(AABBi key, T value) {
            super(key, value);
        }
    }

    ;

    private static class InfiniteGrid<ELT> implements Iterable<ListGrid<ELT>> {
        private ListGrid<ELT>[] grid;

        public InfiniteGrid() {
            grid = new ListGrid[8];
            for (int i = 0; i < grid.length; i++) {
                grid[i] = new ListGrid<>(0, 0, 0);
            }
        }

        public List<ELT> get(int x, int y, int z) {
            int i = 0;
            if (x < 0) {
                x = -x - 1;
                i += 4;
            }
            if (y < 0) {
                y = -y - 1;
                i += 2;
            }
            if (z < 0) {
                z = -z - 1;
                i += 1;
            }
            return grid[i].get(x, y, z);
        }

        public void add(int x, int y, int z, ELT element) {
            int i = 0;
            if (x < 0) {
                x = -x - 1;
                i += 4;
            }
            if (y < 0) {
                y = -y - 1;
                i += 2;
            }
            if (z < 0) {
                z = -z - 1;
                i += 1;
            }
            grid[i].add(x, y, z, element);
        }

        @Override
        public Iterator<ListGrid<ELT>> iterator() {
            return Arrays.asList(grid).iterator();
        }
    }

    private static class ListGrid<ELT> implements Iterable<ELT> {
        private ArrayList<ArrayList<ArrayList<List<ELT>>>> grid;
        private int xSize = 0;
        private int ySize = 0;
        private int zSize = 0;

        public ListGrid(int xSize, int ySize, int zSize) {
            grid = new ArrayList<>(xSize);
            ensureSize(xSize, ySize, zSize);
        }

        private void ensureSize(int xMin, int yMin, int zMin) {
            if (xMin < xSize && yMin < ySize && zMin < zSize) {
                return;
            }

            grid.ensureCapacity(xMin + 1);

            for (int x = 0; x <= xMin; x++) {
                ArrayList<ArrayList<List<ELT>>> yzRow = x < grid.size() ? grid.get(x) : new ArrayList<>(yMin);
                yzRow.ensureCapacity(yMin + 1);

                for (int y = 0; y <= yMin; y++) {
                    ArrayList<List<ELT>> zRow = y < yzRow.size() ? yzRow.get(y) : new ArrayList<>(zMin);
                    zRow.ensureCapacity(zMin + 1);

                    for (int z = zRow.size(); z <= zMin; z++) {
                        zRow.add(new ArrayList<>());
                    }
                    yzRow.add(zRow);
                }
                grid.add(yzRow);
            }

            xSize = Math.max(xSize, xMin + 1);
            ySize = Math.max(ySize, yMin + 1);
            zSize = Math.max(zSize, zMin + 1);
        }

        public void add(int x, int y, int z, ELT element) {
            ensureSize(x, y, z);
            grid.get(x).get(y).get(z).add(element);
        }

        public List<ELT> get(int x, int y, int z) {
            if (x < xSize && y < ySize && z < zSize) {
                return grid.get(x).get(y).get(z);

            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public void forEach(Consumer<? super ELT> action) {
            for (ArrayList<ArrayList<List<ELT>>> lists : grid) {
                for (ArrayList<List<ELT>> list : lists) {
                    for (List<ELT> elements : list) {
                        for (ELT elt : elements) {
                            action.accept(elt);
                        }
                    }
                }
            }
        }

        @Override
        public Iterator<ELT> iterator() {
            if (xSize == 0 || ySize == 0 || zSize == 0) return Collections.emptyIterator();

            return new ListGridItr();
        }

        private class ListGridItr implements Iterator<ELT> {
            Iterator<ArrayList<ArrayList<List<ELT>>>> xItr = grid.iterator();
            Iterator<ArrayList<List<ELT>>> yItr = xItr.next().iterator();
            Iterator<List<ELT>> zItr = yItr.next().iterator();
            Iterator<ELT> bucketItr = zItr.next().iterator();
            ELT next;

            ListGridItr() {
                progress();
            }

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public ELT next() {
                ELT result = next;

                boolean hasNext = progress();
                if (!hasNext && result == null) {
                    throw new NoSuchElementException();
                }

                return result;
            }

            boolean progress() {
                while (!bucketItr.hasNext()) {
                    if (!zItr.hasNext()) {
                        if (!yItr.hasNext()) {
                            if (!xItr.hasNext()) {
                                next = null;
                                return false;
                            }
                            yItr = xItr.next().iterator();
                        }
                        zItr = yItr.next().iterator();
                    }
                    bucketItr = zItr.next().iterator();
                }

                next = bucketItr.next();
                return true;
            }
        }
    }
}
