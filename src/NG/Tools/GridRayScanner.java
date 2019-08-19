package NG.Tools;

import NG.CollisionDetection.Collision;
import org.joml.*;

import java.lang.Math;

/**
 * @author Geert van Ieperen created on 15-8-2019.
 */
public class GridRayScanner {
    private final Vector3fc size;
    private final Vector3fc tileSize;
    private Intersectable target;

    public GridRayScanner(
            int sizeX, int sizeY, int sizeZ, float tileSizeX, float tileSizeY, float tileSizeZ,
            Intersectable target
    ) {
        this.size = new Vector3f(sizeX, sizeY, sizeZ);
        this.tileSize = new Vector3f(tileSizeX, tileSizeY, tileSizeZ);
        this.target = target;
    }

    public GridRayScanner(Vector3ic size, Vector3fc tileSize, Intersectable target) {
        this.size = new Vector3f(size);
        this.tileSize = tileSize;
        this.target = target;
    }

    public Collision getIntersection(Vector3fc origin, Vector3fc direction, boolean isInfinite) {
        if (!Vectors.isScalable(direction)) return Collision.NONE;

        Vector3f coordPos = new Vector3f(origin).div(tileSize);
        Vector3f coordDir = new Vector3f(direction).div(tileSize);

        Vector2f worldClip = new Vector2f();
        boolean isOnWorld = Intersectionf.intersectRayAab(
                coordPos, coordDir,
                Vectors.O, size,
                worldClip
        );
        if (!isOnWorld) return Collision.NONE;

        float adjMin = Math.max(worldClip.x, 0);
        float adjMax = isInfinite ? worldClip.y : Math.min(worldClip.y, 1);

        coordPos.add(new Vector3f(coordDir).mul(adjMin));
        coordDir.mul(adjMax - adjMin);
        Vector3i lineTraverse = new Vector3i((int) coordPos.x, (int) coordPos.y, (int) coordPos.z);

        while (lineTraverse != null) {
            int xCoord = lineTraverse.x;
            int yCoord = lineTraverse.y;
            int zCoord = lineTraverse.z;

            Collision coll = target.getIntersection(origin, direction, xCoord, yCoord, zCoord);
            float secFrac = coll.getScalar();

            if (secFrac >= 0 && (isInfinite || secFrac < 1)) {
                coll.convertToGlobal(Vectors.Matrix.IDENTITY);
                return coll;
            }

            // no luck, try next coordinate
            lineTraverse = nextCoordinate(xCoord, yCoord, zCoord, coordPos, coordDir, 1);
        }

        return Collision.NONE;
    }

    /**
     * assuming you are at (xCoord, yCoord, zCoord), computes the next coordinate hit by the given ray
     * @param xCoord    the current x coordinate
     * @param yCoord    the current y coordinate
     * @param zCoord    the current z coordiante
     * @param origin    the origin of the ray
     * @param direction the direction of the ray
     * @return the next coordinate hit by the ray. If the previous coordinate was not hit by the ray, return a
     * coordinate closer to the ray than this one (will eventually return coordinates on the ray)
     */
    public static Vector3i nextCoordinate(
            int xCoord, int yCoord, int zCoord, Vector3fc origin, Vector3fc direction, float maximum
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

        if (xIntersect >= maximum && yIntersect >= maximum && zIntersect >= maximum) {
            return null;
        }

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
         * @return the first intersection of the ray with this tile, {@link Float#POSITIVE_INFINITY} if it does not hit and
         * null if the given coordinate is not on the map.
         */
        Collision getIntersection(Vector3fc origin, Vector3fc direction, int xCoord, int yCoord, int zCoord);
    }
}
