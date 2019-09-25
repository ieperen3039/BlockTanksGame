package NG.DataStructures.Generic;

import org.joml.Vector3i;
import org.joml.Vector3ic;

/**
 * @author Geert van Ieperen created on 17-8-2019.
 */
public class AABBi {
    public int xMin, yMin, zMin;
    public int xMax, yMax, zMax;

    public AABBi(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
        this.xMin = xMin;
        this.yMin = yMin;
        this.zMin = zMin;
        this.xMax = xMax;
        this.yMax = yMax;
        this.zMax = zMax;
    }

    /**
     * create hitbox based on a point and a relative other point. travel may be negative
     * @param point  a point to include in this box
     * @param travel a relative distance that is combined with point to make this box
     */
    public AABBi(Vector3ic point, Vector3ic travel) {
        int xa = point.x() + travel.x();
        int ya = point.y() + travel.y();
        int za = point.z() + travel.z();
        int xb = point.x();
        int yb = point.y();
        int zb = point.z();

        xMin = Math.min(xa, xb);
        yMin = Math.min(ya, yb);
        zMin = Math.min(za, zb);
        xMax = Math.max(xa, xb);
        yMax = Math.max(ya, yb);
        zMax = Math.max(za, zb);
    }

    public AABBi() {
        xMin = Integer.MAX_VALUE;
        yMin = Integer.MAX_VALUE;
        zMin = Integer.MAX_VALUE;
        xMax = Integer.MIN_VALUE;
        yMax = Integer.MIN_VALUE;
        zMax = Integer.MIN_VALUE;
    }

    public void union(Vector3ic point) {
        xMin = Math.min(point.x(), xMin);
        yMin = Math.min(point.y(), yMin);
        zMin = Math.min(point.z(), zMin);
        xMax = Math.max(point.x(), xMax);
        yMax = Math.max(point.y(), yMax);
        zMax = Math.max(point.z(), zMax);
    }

    public void union(AABBi point) {
        xMin = Math.min(point.xMin, xMin);
        yMin = Math.min(point.xMin, yMin);
        zMin = Math.min(point.xMin, zMin);
        xMax = Math.max(point.xMax, xMax);
        yMax = Math.max(point.xMax, yMax);
        zMax = Math.max(point.xMax, zMax);
    }

    public boolean intersects(AABBi other) {
        boolean xOk = this.xMin > other.xMax || this.xMax < other.xMin;
        boolean yOk = this.yMin > other.yMax || this.yMax < other.yMin;
        boolean zOk = this.zMin > other.zMax || this.zMax < other.zMin;
        return !(xOk || yOk || zOk);
    }

    public Vector3ic dimensions() {
        return new Vector3i(xMax - xMin, yMax - yMin, zMax - zMin);
    }

    public boolean contains(Vector3ic position) {
        return contains(position.x(), position.y(), position.z());
    }

    public boolean contains(int x, int y, int z) {
        boolean xOk = this.xMin > x || this.xMax < x;
        boolean yOk = this.yMin > y || this.yMax < y;
        boolean zOk = this.zMin > z || this.zMax < z;
        return !(xOk || yOk || zOk);
    }

    @Override
    public String toString() {
        return String.format(
                "([%s, %s], [%s, %s], [%s, %s])",
                asMin(xMin), asMax(xMax), asMin(yMin), asMax(yMax), asMin(zMin), asMax(zMax)
        );
    }

    private static String asMin(int min) {
        return min == Integer.MAX_VALUE ? "-" : Integer.toString(min);
    }

    private static String asMax(int max) {
        return max == Integer.MIN_VALUE ? "-" : Integer.toString(max);
    }

    public Vector3i getMinimum() {
        return new Vector3i(xMin, yMin, zMin);
    }

    public Vector3i getMaximum() {
        return new Vector3i(xMax, yMax, zMax);
    }
}
