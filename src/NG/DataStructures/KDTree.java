package NG.DataStructures;

import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.*;

/**
 * @author Geert van Ieperen created on 14-8-2019.
 */
public class KDTree<T> extends AbstractMap<Vector3ic, T> implements Iterable<T> {
    private KDNode<T> root;

    @Override
    public Set<Entry<Vector3ic, T>> entrySet() {
        HashSet<Entry<Vector3ic, T>> elts = new HashSet<>(root.size);
        root.addEntries(elts);
        return elts;
    }

    @Override
    public void clear() {
        root = null;
    }

    @Override
    public Collection<T> values() {
        if (root == null) {
            // wrapper for when this is empty
            return new AbstractCollection<T>() {
                @Override
                public Iterator<T> iterator() {
                    if (root == null) return Collections.emptyIterator();
                    return root.iterator();
                }

                @Override
                public int size() {
                    return KDTree.this.size();
                }
            };
        }

        return root;
    }

    @Override
    public T get(Object key) {
        if (root == null) return null;
        return root.subtree((Vector3ic) key).element;
    }

    public int size() {
        if (root == null) return 0;
        return root.size;
    }

    public T get(int xCoord, int yCoord, int zCoord) {
        return get(new Vector3i(xCoord, yCoord, zCoord));
    }

    @Override
    public T put(Vector3ic key, T value) {
        if (root == null) {
            root = new KDNode<>(key, value, 0);
        }
        return root.add(value, key);
    }

    @Override
    public Iterator<T> iterator() {
        if (root == null) return Collections.emptyIterator();
        return root.iterator();
    }

    private static class KDNode<T> extends AbstractCollection<T> {
        private T element;
        private Vector3ic position;
        private int componentInd;

        private KDNode<T> higher = null;
        private KDNode<T> lower = null;
        private int size;
        private int specificValue;

        private KDNode(Vector3ic pos, T elt, int depth) {
            this.element = elt;
            this.position = new Vector3i(pos);
            this.componentInd = depth % 3;
            this.size = 1;
            this.specificValue = pos.get(depth);
        }

        public T add(T elt, Vector3ic coord) {
            int eltValue = coord.get(componentInd);
            if (eltValue < specificValue) {
                if (lower == null) {
                    lower = new KDNode<>(coord, elt, componentInd + 1);
                    return null;

                } else {
                    return lower.add(elt, coord);
                }

            } else if (eltValue > specificValue) {
                if (higher == null) {
                    higher = new KDNode<>(coord, elt, componentInd + 1);
                    return null;

                } else {
                    return higher.add(elt, coord);
                }
            }

            T prev = element;
            element = elt;
            return prev;
        }

        public List<T> findAll(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
            ArrayList<T> result = new ArrayList<>();
            findAll(new Vector3i(xMin, yMin, zMin), new Vector3i(xMax, yMax, zMax), result);
            return result;
        }

        public List<T> findAll(Vector3ic min, Vector3ic max) {
            ArrayList<T> result = new ArrayList<>();
            findAll(min, max, result);
            return result;
        }

        private void findAll(Vector3ic min, Vector3ic max, List<T> result) {
            int least = min.get(componentInd);
            int most = max.get(componentInd);

            if (specificValue < least) {
                if (lower != null) {
                    lower.findAll(min, max, result);
                }
                return;

            } else if (specificValue > most) {
                if (higher != null) {
                    higher.findAll(min, max, result);
                }
                return;
            }

            if (lower != null) {
                lower.findAll(min, max, result);
            }
            result.add(element);
            if (higher != null) {
                higher.findAll(min, max, result);
            }
        }

        private void addEntries(Set<Entry<Vector3ic, T>> dest) {
            if (lower != null) lower.addEntries(dest);
            dest.add(new SimpleEntry<>(position, element));
            if (higher != null) higher.addEntries(dest);
        }

        public KDNode<T> subtree(Vector3ic key) {
            int coordValue = key.get(componentInd);

            if (lower != null && coordValue < specificValue) {
                return lower.subtree(key);

            } else if (higher != null && coordValue > specificValue) {
                return higher.subtree(key);
            } else {
                return this;
            }
        }

        private List<KDNode<T>> parentsOf(Vector3ic key) {
            int coordValue = key.get(componentInd);

            if (lower != null && coordValue < specificValue) {
                List<KDNode<T>> childs = lower.parentsOf(key);
                childs.add(lower);
                return childs;

            } else if (higher != null && coordValue > specificValue) {
                List<KDNode<T>> childs = higher.parentsOf(key);
                childs.add(higher);
                return childs;

            } else {
                return new ArrayList<>();
            }
        }

        /**
         * @param target         the target position
         * @param nrOfNeighbours the number of neighbours to be searched for
         * @return a list of nearby neighbours of the given target position. If there are not enough elements in this
         * tree, only the available elements are returned.
         */
        public List<KDNode<T>> getNeighbours(Vector3ic target, int nrOfNeighbours) {
            NearestNeighborList<KDNode<T>> nnl = new NearestNeighborList<>(nrOfNeighbours);
            getNeighbours(this, target, new HRect(), Integer.MAX_VALUE, nnl);
            return nnl.asList();
        }

        // Method Nearest Neighbor from Andrew Moore's thesis. Numbered
        // comments are direct quotes from there. NearestNeighborList solution
        // courtesy of Bjoern Heckel.
        private static <T> void getNeighbours(
                KDNode<T> node,
                Vector3ic target, HRect rectangle,
                int max_dist_sqd, NearestNeighborList<KDNode<T>> nnl
        ) {
            if (node == null) return;

            // 3. pivot := dom-elt field of kd
            Vector3ic pivot = node.position;
            int comp = node.componentInd;
            int nodeValue = node.specificValue;

            int pivot_to_target = (int) pivot.distanceSquared(target);

            // 4. Cut hr into to sub-hyperrectangles left-hr and right-hr.
            // The cut plane is through pivot and perpendicular to the s
            // dimension.
            HRect left_hr = rectangle;
            HRect right_hr = new HRect(left_hr);
            left_hr.max.setComponent(comp, nodeValue);
            right_hr.min.setComponent(comp, nodeValue);

            // 5. target-in-left := target_s <= pivot_s
            boolean target_in_left = target.get(comp) < nodeValue;

            KDNode<T> nearer_kd;
            HRect nearer_hr;
            KDNode<T> further_kd;
            HRect further_hr;

            // 6. if target-in-left then
            // 6.1. nearer-kd := left field of kd and nearer-hr := left-hr
            // 6.2. further-kd := right field of kd and further-hr := right-hr
            if (target_in_left) {
                nearer_kd = node.higher;
                nearer_hr = left_hr;
                further_kd = node.lower;
                further_hr = right_hr;
            }
            //
            // 7. if not target-in-left then
            // 7.1. nearer-kd := right field of kd and nearer-hr := right-hr
            // 7.2. further-kd := left field of kd and further-hr := left-hr
            else {
                nearer_kd = node.lower;
                nearer_hr = right_hr;
                further_kd = node.higher;
                further_hr = left_hr;
            }

            // 8. Recursively call Nearest Neighbor with paramters
            // (nearer-kd, target, nearer-hr, max-dist-sqd), storing the
            // results in nearest and dist-sqd
            KDNode.getNeighbours(nearer_kd, target, nearer_hr, max_dist_sqd, nnl);

            // following line commented out on purpose; was in original code but does nothing, also see 10.1.1
            // KDNode<T> nearest = nnl.getHighest();
            int dist_sqd;

            if (!nnl.isCapacityReached()) {
                dist_sqd = Integer.MAX_VALUE;
            } else {
                dist_sqd = nnl.getMaxPriority();
            }

            // 9. max-dist-sqd := minimum of max-dist-sqd and dist-sqd
            max_dist_sqd = Math.min(max_dist_sqd, dist_sqd);

            // 10. A nearer point could only lie in further-kd if there were some
            // part of further-hr within distance max-dist-sqd of
            // target.
            Vector3i closest = further_hr.closest(target);
            if (closest.distanceSquared(target) < max_dist_sqd) {

                // 10.1 if (pivot-target)^2 < dist-sqd then
                if (pivot_to_target < dist_sqd) {

                    // 10.1.1 nearest := (pivot, range-elt field of kd)
                    // followinng line commented out on purpose; was in original code but does nothing
                    // nearest = kd;

                    // 10.1.2 dist-sqd = (pivot-target)^2
                    dist_sqd = pivot_to_target;

                    // add to nnl
                    nnl.insert(node, (int) dist_sqd);

                    // 10.1.3 max-dist-sqd = dist-sqd
                    // max_dist_sqd = dist_sqd;
                    if (nnl.isCapacityReached()) {
                        max_dist_sqd = nnl.getMaxPriority();
                    } else {
                        max_dist_sqd = Integer.MAX_VALUE;
                    }
                }

                // 10.2 Recursively call Nearest Neighbor with parameters
                // (further-kd, target, further-hr, max-dist_sqd),
                // storing results in temp-nearest and temp-dist-sqd
                KDNode.getNeighbours(further_kd, target, further_hr, max_dist_sqd, nnl);
            }
        }

        public Iterator<T> iterator() {
            return new Iterator<T>() {
                private static final int TO_LEFT_ITR = 1;
                private static final int TO_RIGHT_ITR = 3;
                private static final int DONE = 5;
                Iterator<T> itr = null;
                int state = 0;

                @Override
                public boolean hasNext() {
                    return state < DONE;
                }

                @Override
                public T next() {
                    T elt;

                    if (state == 0) {
                        state = TO_LEFT_ITR;
                        elt = element;
                    } else {
                        elt = itr.next();
                        if (!itr.hasNext()) state++;
                    }

                    if (state == TO_LEFT_ITR) {
                        if (higher == null) {
                            state = TO_RIGHT_ITR;
                        } else {
                            state = 2;
                            itr = higher.iterator();
                        }
                    }

                    if (state == TO_RIGHT_ITR) {
                        if (lower == null) {
                            state = DONE;
                        } else {
                            state = 4;
                            itr = lower.iterator();
                        }
                    }

                    return elt;
                }
            };
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public String toString() {
            return "{" +
                    (lower == null ? "" : lower.toString() + "< ") +
                    element +
                    (higher == null ? "" : " <" + higher.toString()) +
                    "}";
        }

        public static class HRect {
            public Vector3i min;
            public Vector3i max;

            public HRect() {
                min = new Vector3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
                max = new Vector3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
            }

            HRect(HRect source) {
                min = new Vector3i(source.min);
                max = new Vector3i(source.max);
            }

            Vector3i closest(Vector3ic vec) {
                return new Vector3i(vec).min(max).max(min);
            }
        }
    }
}
