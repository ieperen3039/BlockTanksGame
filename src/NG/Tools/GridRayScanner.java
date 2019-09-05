package NG.Tools;

import NG.CollisionDetection.Collision;
import org.joml.*;

import java.lang.Math;
import java.util.function.Function;

/**
 * @author Geert van Ieperen created on 15-8-2019.
 */
public class GridRayScanner {
    private final Vector3fc maxCoord;
    private Function<Vector3f, Vector3f> coordMap;
    private final Vector3fc minCoord;
    private Intersectable target;

    public GridRayScanner(Vector3ic gridSize, Function<Vector3f, Vector3f> toCoordinate, Intersectable target) {
        this(new Vector3i(), gridSize, toCoordinate, target);
    }

    public GridRayScanner(
            Vector3ic minCoord, Vector3ic maxCoord, Function<Vector3f, Vector3f> toCoordinate, Intersectable target
    ) {
        this.minCoord = new Vector3f(minCoord);
        this.maxCoord = new Vector3f(maxCoord);
        this.coordMap = toCoordinate;
        this.target = target;
    }

    /**
     * calculates the collision of the given ray with this map. if no such collision exists, it returns the empty
     * collision.
     * @param origin     the origin of the ray
     * @param direction  the direction of the ray
     * @param isInfinite if false, only the line piece of origin to (origin + direction) is considered. If true, the ray
     *                   from origin to infinity in the given direction is considered.
     * @return the resulting collision, or {@link Collision#NONE} if no collision occurs
     */
    public Collision getIntersection(Vector3fc origin, Vector3fc direction, boolean isInfinite) {
        if (!Vectors.isScalable(direction)) return Collision.NONE;

        Vector3f coordPos = coordMap.apply(new Vector3f(origin));
        Vector3f coordDir = coordMap.apply(new Vector3f(origin).add(direction)).sub(coordPos);

        Vector2f worldClip = new Vector2f();
        boolean isOnWorld = Intersectionf.intersectRayAab(
                coordPos, coordDir,
                minCoord, maxCoord,
                worldClip
        );
        if (!isOnWorld) return Collision.NONE;
        if (!isInfinite && worldClip.x > 1) return Collision.NONE;

        float adjMin = Math.max(worldClip.x, 0);
        float adjMax = isInfinite ? worldClip.y : Math.min(worldClip.y, 1);

        // transform coordPos + coordDir to the entire region to be checked
        coordPos.add(new Vector3f(coordDir).mul(adjMin));
        if (isInfinite) {
            coordDir.mul(adjMax - adjMin);
        } else {
            coordDir.mul(1 - adjMin);
        }
        if (!Vectors.isScalable(coordDir)) return Collision.NONE;

        Vector3i lineTraverse = new Vector3i((int) coordPos.x, (int) coordPos.y, (int) coordPos.z);
        while (lineTraverse != null) {
            int xCoord = lineTraverse.x;
            int yCoord = lineTraverse.y;
            int zCoord = lineTraverse.z;

            Collision coll = target.getIntersection(origin, direction, xCoord, yCoord, zCoord);

            if (coll.isCollision() && (isInfinite || coll.getCollisionTime() < 1)) {
                coll.convertToGlobal(Vectors.Matrix.IDENTITY);
                return coll;
            }

            // no luck, try next coordinate
            lineTraverse = nextCoordinate(xCoord, yCoord, zCoord, coordPos, coordDir);
        }

        return Collision.NONE;
    }

    /**
     * assuming you are at (xCoord, yCoord, zCoord), computes the next coordinate hit by the given ray
     * @param xCoord    the current x coordinate
     * @param yCoord    the current y coordinate
     * @param zCoord    the current z coordiante
     * @param origin    the origin of the ray
     * @param direction the direction of the ray, may not be zero
     * @return the next coordinate hit by the ray. If the previous coordinate was not hit by the ray, return a
     * coordinate closer to the ray than this one (will eventually return coordinates on the ray)
     */
    public static Vector3i nextCoordinate(
            int xCoord, int yCoord, int zCoord, Vector3fc origin, Vector3fc direction
    ) {
        boolean xIsPos = direction.x() > 0;
        boolean yIsPos = direction.y() > 0;
        boolean zIsPos = direction.z() > 0;

        float xIntersect = Intersectionf.intersectRayPlane(
                origin.x(), origin.y(), origin.z(), direction.x(), direction.y(), direction.z(),
                (xIsPos ? xCoord + 1 : xCoord), yCoord, zCoord, (xIsPos ? -1 : 1), 0, 0,
                1e-3f
        );
        float yIntersect = Intersectionf.intersectRayPlane(
                origin.x(), origin.y(), origin.z(), direction.x(), direction.y(), direction.z(),
                xCoord, (yIsPos ? yCoord + 1 : yCoord), zCoord, 0, (yIsPos ? -1 : 1), 0,
                1e-3f
        );
        float zIntersect = Intersectionf.intersectRayPlane(
                origin.x(), origin.y(), origin.z(), direction.x(), direction.y(), direction.z(),
                xCoord, yCoord, (zIsPos ? zCoord + 1 : zCoord), 0, 0, (zIsPos ? -1 : 1),
                1e-3f
        );
        Logger.WARN.print(origin, direction, xIntersect, yIntersect, zIntersect);

        if (xIntersect >= 1 && yIntersect >= 1 && zIntersect >= 1) return null;

        // in case of parallel
        if (xIntersect < 0) xIntersect = 1;
        if (yIntersect < 0) yIntersect = 1;
        if (zIntersect < 0) zIntersect = 1;

        Vector3i next = new Vector3i(xCoord, yCoord, zCoord);

        if (xIntersect <= yIntersect && xIntersect <= zIntersect) next.add((xIsPos ? 1 : -1), 0, 0);
        if (yIntersect <= xIntersect && yIntersect <= zIntersect) next.add(0, (yIsPos ? 1 : -1), 0);
        if (zIntersect <= xIntersect && zIntersect <= yIntersect) next.add(0, 0, (zIsPos ? 1 : -1));

        return next;
    }

    public interface Intersectable {
        /**
         * computes the intersection of a ray on the given coordinate
         * @param origin    the origin of the ray in real space
         * @param direction the direction of the ray
         * @param xCoord    the x coordinate
         * @param yCoord    the y coordinate
         * @param zCoord    the z coordinate
         * @return the first intersection of the ray with this tile, {@link Float#POSITIVE_INFINITY} if it does not hit
         * and null if the given coordinate is not on the map.
         */
        Collision getIntersection(Vector3fc origin, Vector3fc direction, int xCoord, int yCoord, int zCoord);
    }
}
