package NG.Shapes;

import NG.CollisionDetection.Collision;
import NG.Shapes.Primitives.Plane;
import org.joml.AABBf;
import org.joml.Vector3fc;

import java.util.List;

/**
 * @author Geert van Ieperen created on 30-10-2017.
 */
public interface Shape {

    /** @return all planes of this shape. The order of planes is consistent */
    List<? extends Plane> getPlanes();

    /** @return the points of this shape. The order of planes is consistent */
    List<Vector3fc> getPoints();

    /**
     * given a point on position {@code origin} and a direction of {@code direction}, calculates the collision resulting
     * from this ray
     * @param origin    the begin of a line segment
     * @param direction the direction of the line segment
     * @return the scalar t
     */
    default Collision getIntersection(Vector3fc origin, Vector3fc direction) {
        Collision least = Collision.NONE;

        for (Plane plane : getPlanes()) {
            Collision intersectionScalar = plane.getIntersection(origin, direction);

            if (intersectionScalar.isEarlierThan(least)) {
                least = intersectionScalar;
            }
        }

        return least;
    }

    AABBf getBoundingBox();

}
