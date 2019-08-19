package NG.Blocks;

import NG.DataStructures.Generic.AABBi;
import NG.DataStructures.Generic.Pair;
import org.joml.Vector3ic;

import java.util.*;

/**
 * @author Geert van Ieperen created on 18-8-2019.
 */
public class BucketGrid3i<T> extends AbstractCollection<T> {
    private final ListGrid<Element> grid;
    private int xMin;
    private int yMin;
    private int zMin;
    private int bucketSize;
    private int size = 0;

    /**
     * @param xMin lowest value the x component of a position vector may assume
     * @param yMin lowest value the y component of a position vector may assume
     * @param zMin lowest value the z component of a position vector may assume
     * @param xMax highest value the x component of a position vector may assume
     * @param yMax highest value the y component of a position vector may assume
     * @param zMax highest value the z component of a position vector may assume
     * @param bucketSize a large bucket size makes queries longer, a small bucket size makes edits longer
     */
    public BucketGrid3i(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, int bucketSize) {
        this.grid = new ListGrid<>((xMax - xMin) / bucketSize, (yMax - yMin) / bucketSize, (zMax - zMin) / bucketSize);
        this.xMin = xMin;
        this.yMin = yMin;
        this.zMin = zMin;
        this.bucketSize = bucketSize;
    }

    private List<Element> bucketOf(int x, int y, int z){
        return grid.get((x - xMin) / bucketSize, (y - yMin) / bucketSize, (z - zMin) / bucketSize);
    }

    private List<List<Element>> bucketsOf(AABBi hitBox){

        List<List<Element>> result = new ArrayList<>(/*product of differences*/);
        int xMin = (hitBox.xMin - this.xMin) / bucketSize;
        int yMin = (hitBox.yMin - this.yMin) / bucketSize;
        int zMin = (hitBox.zMin - this.zMin) / bucketSize;
        int xMax = (hitBox.xMax - this.xMin) / bucketSize;
        int yMax = (hitBox.yMax - this.yMin) / bucketSize;
        int zMax = (hitBox.zMax - this.zMin) / bucketSize;
        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    result.add(grid.get(x, y, z));
                }
            }
        }

        return result;
    }

    public void add(T element, AABBi hitBox){
        Element e = new Element(hitBox, element);
        int xMin = (hitBox.xMin - this.xMin) / bucketSize;
        int yMin = (hitBox.yMin - this.yMin) / bucketSize;
        int zMin = (hitBox.zMin - this.zMin) / bucketSize;
        int xMax = (hitBox.xMax - this.xMin) / bucketSize;
        int yMax = (hitBox.yMax - this.yMin) / bucketSize;
        int zMax = (hitBox.zMax - this.zMin) / bucketSize;

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    grid.add(x, y, z, e);
                }
            }
        }

        size++;
    }

    public List<T> get(AABBi range){
        List<T> results = new ArrayList<>();
        List<List<Element>> buckets = bucketsOf(range);

        for (List<Element> bucket : buckets) {
            for (Element elt : bucket) {
                if (elt.left.intersects(range)){
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
            if (elt.left.intersects(x, y, z)){
                return elt.right;
            }
        }
        return null;
    }

    public void remove(T element, AABBi hitBox){
        for (List<Element> list : bucketsOf(hitBox)) {
            list.removeIf(elt -> elt.right.equals(element));
        }

        size--;
    }

    public Collection<T> values(){
        HashSet<T> set = new HashSet<>();
        for (ArrayList<ArrayList<List<Element>>> lists : grid.grid) {
            for (ArrayList<List<Element>> list : lists) {
                for (List<Element> elements : list) {
                    for (Element elt : elements) {
                        set.add(elt.right);
                    }
                }
            }
        }
        return set;
    }

    @Override
    public Iterator<T> iterator() {
        return values().iterator();
    }

    @Override
    public int size() {
        return size;
    }

    public List<T> get(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
        return get(new AABBi(xMin, yMin, zMin, xMax, yMax, zMax));
    }

    private class Element extends Pair<AABBi, T> {
        private Element(AABBi key, T value) {
            super(key, value);
        }
    };

    private static class ListGrid<ELT> {
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

            grid.ensureCapacity(xMin);

            for (int x = 0; x < xMin; x++) {
                ArrayList<ArrayList<List<ELT>>> yzRow = x < grid.size() ? grid.get(x) : new ArrayList<>(yMin);
                yzRow.ensureCapacity(yMin);

                for (int y = 0; y < yMin; y++) {
                    ArrayList<List<ELT>> zRow = y < yzRow.size() ? yzRow.get(y) : new ArrayList<>(zMin);
                    zRow.ensureCapacity(zMin);

                    for (int z = zRow.size(); z < zMin; z++) {
                        zRow.add(new ArrayList<>());
                    }
                    yzRow.add(zRow);
                }
                grid.add(yzRow);
            }

            xSize = Math.max(xSize, xMin);
            ySize = Math.max(ySize, yMin);
            zSize = Math.max(zSize, zMin);
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
    }
}
