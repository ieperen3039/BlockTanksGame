package NG.CollisionDetection;

import NG.DataStructures.Generic.AveragingQueue;
import NG.DataStructures.Generic.ConcurrentArrayList;
import NG.DataStructures.Generic.Pair;
import NG.DataStructures.Generic.PairList;
import NG.DataStructures.Vector3fxc;
import NG.Entities.Entity;
import NG.Entities.MovingEntity;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import org.joml.AABBf;
import org.joml.RayAabIntersection;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.util.Comparator.comparing;

/**
 * @author Geert van Ieperen created on 10-3-2018.
 */
public class CollisionDetection {
    private static final int MAX_COLLISION_ITERATIONS = 5;
    private static final int INSERTION_SORT_BOUND = 64;

    private CollisionEntity[] xLowerSorted;
    private CollisionEntity[] yLowerSorted;
    private CollisionEntity[] zLowerSorted;

    private AveragingQueue avgCollisions;

    private Collection<Entity> staticEntities;
    private Collection<Entity> dynamicEntities;
    private Collection<Entity> newEntities;
    private float previousTime;

    /**
     * @see #CollisionDetection(Collection)
     */
    public CollisionDetection(Entity... entities) {
        this(Arrays.asList(entities));
    }

    /**
     * Collects the given entities and allows collision and physics calculations to influence these entities.
     * @param staticEntities a list of fixed entities. Entities in this collection should not move, but if they do,
     *                       dynamic objects might phase through when moving in opposite direction. Apart from this
     *                       case, the collision detection still functions.
     */
    public CollisionDetection(Collection<Entity> staticEntities) {
        this.staticEntities = Collections.unmodifiableCollection(staticEntities);
        this.dynamicEntities = new CopyOnWriteArrayList<>();
        this.newEntities = new ConcurrentArrayList<>();

        Logger.printOnline(() ->
                String.format("Collision pair count average: %1.01f", avgCollisions.average())
        );

        int nOfEntities = staticEntities.size();
        xLowerSorted = new CollisionEntity[nOfEntities];
        yLowerSorted = new CollisionEntity[nOfEntities];
        zLowerSorted = new CollisionEntity[nOfEntities];

        populate(staticEntities, xLowerSorted, yLowerSorted, zLowerSorted);

        avgCollisions = new AveragingQueue(5);
    }

    /**
     * populates the given arrays, and sorts the arrays on the lower coordinate of the hitboxes (x, y and z
     * respectively)
     */
    private void populate(
            Collection<? extends Entity> entities,
            CollisionEntity[] xLowerSorted, CollisionEntity[] yLowerSorted, CollisionEntity[] zLowerSorted
    ) {
        int i = 0;
        for (Entity entity : entities) {
            CollisionEntity asCollisionEntity = new CollisionEntity(entity);
            xLowerSorted[i] = asCollisionEntity;
            yLowerSorted[i] = asCollisionEntity;
            zLowerSorted[i] = asCollisionEntity;
            i++;
        }

        if (entities.size() < INSERTION_SORT_BOUND) {
            Toolbox.insertionSort(xLowerSorted, comparing(CollisionEntity::xLower));
            Toolbox.insertionSort(yLowerSorted, comparing(CollisionEntity::yLower));
            Toolbox.insertionSort(zLowerSorted, comparing(CollisionEntity::zLower));

        } else {
            Arrays.sort(xLowerSorted, comparing(CollisionEntity::xLower));
            Arrays.sort(yLowerSorted, comparing(CollisionEntity::yLower));
            Arrays.sort(zLowerSorted, comparing(CollisionEntity::zLower));
        }
    }

    /**
     * @param gameTime the time of the next game-tick
     */
    public void processCollisions(float gameTime) {

        /** -- clean and restore invariants -- */

        // remove disposed entities
        List<Entity> removeEntities = new ArrayList<>();

        for (Entity e : dynamicEntities) {
            if (e.isDisposed()) {
                removeEntities.add(e);
            }
        }

        if (!removeEntities.isEmpty()) {
            deleteEntities(removeEntities);
            removeEntities.clear();
        }

        // add new entities
        newEntities.removeIf(Entity::isDisposed);
        if (!newEntities.isEmpty()) {
            dynamicEntities.addAll(newEntities);
            mergeNewEntities(newEntities);
            newEntities.clear();
        }

        // update representation
        for (CollisionEntity entity : entityArray()) {
            entity.update(gameTime);
        }

        /** -- analyse the collisions -- */

        /* As a single collision may result in a previously not-intersecting pair to collide,
         * we shouldn't re-use the getIntersectingPairs method nor reduce by non-collisions.
         * On the other hand, we may assume collisions of that magnitude appear seldom
         */
        PairList<CollisionEntity, CollisionEntity> pairs = getIntersectingPairs();

        IntStream.range(0, pairs.size())
                .parallel()
                .forEach(n -> {
                    int checksLeft = MAX_COLLISION_ITERATIONS;
                    CollisionEntity left = pairs.left(n);
                    CollisionEntity right = pairs.right(n);

                    boolean didCollide;
                    do {
                        didCollide = checkCollisionPair(left, right, gameTime);
                    } while (didCollide && (--checksLeft > 0));
                });

        previousTime = gameTime;
    }

    /**
     * @param alpha    one entity
     * @param beta     another entity
     * @param gameTime the time of the next game-tick
     * @return true iff these pairs indeed collided before endTime
     */
    private boolean checkCollisionPair(CollisionEntity alpha, CollisionEntity beta, float gameTime) {
        Entity a = alpha.entity;
        Entity b = beta.entity;

        assert !a.canCollideWith(b) && !b.canCollideWith(a);
        // this may change with previous collisions
        if (a.isDisposed() || b.isDisposed() || a == b) return false;

        Collision aCollision = checkAtoB(alpha, b);
        Collision bCollision = checkAtoB(beta, a);

        Collision combined;
        if (aCollision.isEarlierThan(bCollision)) {
            combined = aCollision.getInverse();
        } else {
            combined = bCollision.getInverse();
        }

        if (!combined.isCollision()) return false;

        float collisionTime = previousTime + combined.getScalar() * (previousTime - gameTime);

        a.collideWith(b, combined, collisionTime);
        b.collideWith(a, combined, collisionTime);

        alpha.update();
        beta.update();

        return true;
    }

    /**
     * checks whether {@code moving} collides with {@code receiving}
     * @param moving   an object holding an entity
     * @param receiver another entity
     * @return A non-collision if no collision occurs, otherwise a collision with scalar [0 ... 1) such that {@code
     * origin + scalar * direction} lies on receiver.
     */
    private Collision checkAtoB(CollisionEntity moving, Entity receiver) {
        List<Vector3f> prev = moving.prevPoints;
        List<Vector3f> next = moving.nextPoints;

        Collision first = Collision.SCALAR_ONE;
        for (int i = 0; i < prev.size(); i++) {
            Vector3f origin = next.get(i);
            Vector3f direction = new Vector3f(prev.get(i)).sub(origin);
            Collision intersection = receiver.getIntersection(origin, direction);

            if (intersection.isEarlierThan(first)) {
                first = intersection;
            }
        }

        return first;
    }

    /**
     * generate a list (possibly empty) of all pairs of objects that may have collided. This can include (parts of) the
     * ground, but not an object with itself. One pair does not occur the other way around.
     * @return a collection of pairs of objects that are close to each other
     */
    private PairList<CollisionEntity, CollisionEntity> getIntersectingPairs() {
        Toolbox.insertionSort(xLowerSorted, comparing(CollisionEntity::xLower));
        Toolbox.insertionSort(yLowerSorted, comparing(CollisionEntity::yLower));
        Toolbox.insertionSort(zLowerSorted, comparing(CollisionEntity::zLower));

        assert testInvariants();

        CollisionEntity[] entityArray = entityArray();
        int nrOfEntities = entityArray.length;

        // initialize id values to correspond to the array
        for (int i = 0; i < nrOfEntities; i++) {
            entityArray[i].id = i;
        }

        AdjacencyMatrix<CollisionEntity> adjacencies = new AdjacencyMatrix<>(nrOfEntities, 3, e -> e.id);

        BiPredicate<CollisionEntity, CollisionEntity> canCollide = (a, b) -> a.entity.canCollideWith(b.entity);
        adjacencies.checkOverlap(xLowerSorted, CollisionEntity::xLower, CollisionEntity::xUpper, canCollide);
        adjacencies.checkOverlap(yLowerSorted, CollisionEntity::yLower, CollisionEntity::yUpper, canCollide);
        adjacencies.checkOverlap(zLowerSorted, CollisionEntity::zLower, CollisionEntity::zUpper, canCollide);

        int nrOfElts = adjacencies.nrOfFoundElements();

        PairList<CollisionEntity, CollisionEntity> allEntityPairs = new PairList<>(nrOfElts);
        adjacencies.forEach((i, j) -> allEntityPairs.add(
                entityArray[i], entityArray[j]
        ));

        avgCollisions.add(nrOfElts);
        return allEntityPairs;
    }

    public void addEntities(Collection<Entity> entities) {
        newEntities.addAll(entities);
    }

    public void addEntity(Entity entity) {
        assert (!dynamicEntities.contains(entity)) : entity;
        assert (!newEntities.contains(entity)) : entity;

        newEntities.add(entity);
    }

    /**
     * calculates the first entity hit by the given ray
     * @param origin the origin of the ray
     * @param dir    the direction of the ray
     * @return Left: the first entity hit by the ray, or null if no entity is hit.
     * <p>
     * Right: the fraction t such that {@code origin + t * dir} gives the point of collision with this entity. Undefined
     * if {@code left == null}
     */
    public Pair<Entity, Float> rayTrace(Vector3fc origin, Vector3fc dir) {
        assert testInvariants();

        RayAabIntersection sect = new RayAabIntersection(origin.x(), origin.y(), origin.z(), dir.x(), dir.y(), dir.z());

        float fraction = Float.MAX_VALUE;
        Entity suspect = null;

        for (CollisionEntity elt : entityArray()) {
            boolean collide = sect.test(
                    elt.xLower(), elt.yLower(), elt.zLower(),
                    elt.xUpper(), elt.yUpper(), elt.zUpper()
            );
            if (!collide) continue;

            Entity entity = elt.entity;

            float f = entity.getHitbox().intersectRay(origin, dir);
            if (f < fraction) {
                fraction = f;
                suspect = entity;
            }
        }

        return new Pair<>(suspect, fraction);
    }

    private void mergeNewEntities(Collection<Entity> newEntities) {
        int nOfNewEntities = newEntities.size();
        if (nOfNewEntities <= 0) return;

        CollisionEntity[] newXSort = new CollisionEntity[nOfNewEntities];
        CollisionEntity[] newYSort = new CollisionEntity[nOfNewEntities];
        CollisionEntity[] newZSort = new CollisionEntity[nOfNewEntities];

        populate(newEntities, newXSort, newYSort, newZSort);

        xLowerSorted = Toolbox.mergeArrays(xLowerSorted, newXSort, CollisionEntity::xLower);
        yLowerSorted = Toolbox.mergeArrays(yLowerSorted, newYSort, CollisionEntity::yLower);
        zLowerSorted = Toolbox.mergeArrays(zLowerSorted, newZSort, CollisionEntity::zLower);
    }

    /**
     * Remove the selected entities off the entity lists in a robust way. Entities that did not exist are ignored, and
     * doubles are also accepted.
     * @param targets a collection of entities to be removed
     */
    private void deleteEntities(Collection<Entity> targets) {
        xLowerSorted = deleteAll(targets, xLowerSorted);
        yLowerSorted = deleteAll(targets, yLowerSorted);
        zLowerSorted = deleteAll(targets, zLowerSorted);

        dynamicEntities.removeAll(targets);
    }

    private CollisionEntity[] deleteAll(Collection<Entity> targets, CollisionEntity[] array) {
        int xi = 0;
        for (int i = 0; i < array.length; i++) {
            Entity entity = array[i].entity;
            if ((entity != null) && targets.contains(entity)) {
                continue;
            }
            array[xi++] = array[i];
        }
        return Arrays.copyOf(array, xi);
    }

    /**
     * @return an array of the entities, backed by any local representation. Should only be used for querying, otherwise
     * it must be cloned
     */
    private CollisionEntity[] entityArray() {
        return xLowerSorted;
    }

    public Collection<Entity> getEntityList() {
        Collection<Entity> list = new ArrayList<>(staticEntities);
        list.addAll(dynamicEntities);
        list.addAll(newEntities);
        return list;
    }

    public boolean contains(Entity entity) {
        if (staticEntities.contains(entity)) return true;

        if (entity instanceof MovingEntity) {
            return dynamicEntities.contains(entity) || newEntities.contains(entity);
        }
        return false;
    }

    public void forEach(Consumer<Entity> action) {
        for (Entity e : staticEntities) {
            action.accept(e);
        }
        for (Entity e : dynamicEntities) {
            action.accept(e);
        }
        for (Entity e : newEntities) {
            action.accept(e);
        }
    }

    public void cleanup() {
        xLowerSorted = new CollisionEntity[0];
        yLowerSorted = new CollisionEntity[0];
        zLowerSorted = new CollisionEntity[0];

        for (Entity e : staticEntities) {
            e.dispose();
        }

        for (Entity e : dynamicEntities) {
            e.dispose();
        }
        dynamicEntities.clear();

        for (Entity e : newEntities) {
            e.dispose();
        }
        newEntities.clear();
    }

    protected static class CollisionEntity {
        public final Entity entity;
        public int id;

        public List<Vector3f> nextPoints;
        public List<Vector3f> prevPoints;

        private BoundingBox nextBoundingBox;
        private AABBf hitbox; // combined of both states

        public CollisionEntity(Entity source) {
            this.entity = source;
            prevPoints = entity.getShapePoints();
        }

        public void update(float gameTime) {
            entity.update(gameTime);

            List<Vector3f> buffer = prevPoints;
            prevPoints = nextPoints;
            nextPoints = entity.getShapePoints(buffer);

            Vector3fxc nextPos = entity.getCurrentState().position();
            BoundingBox prevBoundingBox = nextBoundingBox;
            nextBoundingBox = entity.getHitbox().move(nextPos.toVector3f());

            hitbox = prevBoundingBox.union(nextBoundingBox);
        }

        /**
         * update this element without progressing time (for when the entity has changed state this tick)
         */
        public void update() {
            nextPoints = entity.getShapePoints(nextPoints);
            Vector3fxc nextPos = entity.getCurrentState().position();
            nextBoundingBox = entity.getHitbox().move(nextPos.toVector3f());

            hitbox = hitbox.union(nextBoundingBox);
        }

        public float xUpper() {
            return hitbox.maxX;
        }

        public float yUpper() {
            return hitbox.maxY;
        }

        public float zUpper() {
            return hitbox.maxZ;
        }

        public float xLower() {
            return hitbox.minX;
        }

        public float yLower() {
            return hitbox.minY;
        }

        public float zLower() {
            return hitbox.minZ;
        }

        @Override
        public String toString() {
            return entity.toString();
        }
    }


    /**
     * tests whether the invariants holds. Throws an error if any of the arrays is not correctly sorted or any other
     * assumption no longer holds
     */
    boolean testInvariants() {
        String source = Logger.getCallingMethod(1);
        Logger.DEBUG.printSpamless(source, "\n    " + source + " Checking collision detection invariants");

        // all arrays contain all entities
        Set<Entity> allEntities = new HashSet<>();
        for (CollisionEntity colEty : entityArray()) {
            allEntities.add(colEty.entity);
        }

        for (CollisionEntity collEty : xLowerSorted) {
            if (!allEntities.contains(collEty.entity)) {
                throw new IllegalStateException("Array x does not contain entity " + collEty.entity);
            }
        }
        for (CollisionEntity collEty : yLowerSorted) {
            if (!allEntities.contains(collEty.entity)) {
                throw new IllegalStateException("Array y does not contain entity " + collEty.entity);
            }
        }
        for (CollisionEntity collEty : zLowerSorted) {
            if (!allEntities.contains(collEty.entity)) {
                throw new IllegalStateException("Array z does not contain entity " + collEty.entity);
            }
        }

        // all arrays are of equal length
        if ((xLowerSorted.length != yLowerSorted.length) || (xLowerSorted.length != zLowerSorted.length)) {
            Logger.ERROR.print(Arrays.toString(entityArray()));
            throw new IllegalStateException("Entity arrays have different lengths: "
                    + xLowerSorted.length + ", " + yLowerSorted.length + ", " + zLowerSorted.length
            );
        }

        // x is sorted
        float init = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < xLowerSorted.length; i++) {
            CollisionEntity collisionEntity = xLowerSorted[i];
            if (collisionEntity.xLower() < init) {
                Logger.ERROR.print("Sorting error on x = " + i);
                Logger.ERROR.print(Arrays.toString(xLowerSorted));
                throw new IllegalStateException("Sorting error on x = " + i);
            }
            init = collisionEntity.xLower();
        }

        // y is sorted
        init = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < yLowerSorted.length; i++) {
            CollisionEntity collisionEntity = yLowerSorted[i];
            if (collisionEntity.yLower() < init) {
                Logger.ERROR.print("Sorting error on y = " + i);
                Logger.ERROR.print(Arrays.toString(yLowerSorted));
                throw new IllegalStateException("Sorting error on y = " + i);
            }
            init = collisionEntity.yLower();
        }

        // z is sorted
        init = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < zLowerSorted.length; i++) {
            CollisionEntity collisionEntity = zLowerSorted[i];
            if (collisionEntity.zLower() < init) {
                Logger.ERROR.print("Sorting error on z = " + i);
                Logger.ERROR.print(Arrays.toString(zLowerSorted));
                throw new IllegalStateException("Sorting error on z = " + i);
            }
            init = collisionEntity.zLower();
        }

        return true;
    }
}
