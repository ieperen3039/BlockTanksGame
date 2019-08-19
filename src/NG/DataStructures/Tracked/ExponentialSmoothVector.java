package NG.DataStructures.Tracked;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import static java.lang.StrictMath.pow;

/**
 * @author Geert van Ieperen
 *         created on 29-10-2017.
 */
public class ExponentialSmoothVector extends TrackedObject<Vector3fc> implements SmoothTracked<Vector3fc> {

    private final double preservedFraction;

    /**
     * a vector that reduces its distance to its target with a preset fraction
     * @param preservedFraction the fraction that must be preserved per second
     */
    public ExponentialSmoothVector(Vector3fc initial, double preservedFraction) {
        super(initial);
        this.preservedFraction = preservedFraction;
    }

    /**
     * @param target the target vector where this vector will move to.
     *               Note that this vector will be changed, supplying a new instance is advised
     * @param deltaTime the time since the last updatePosition, to allow speed and acceleration
     */
    @Override
    public void updateFluent(Vector3fc target, float deltaTime) {
        float deceleration = (float) pow(preservedFraction, deltaTime);

        Vector3f next = new Vector3f(target)
                .sub(current())
                .mul(1 - deceleration)
                .add(current());

        update(next);
    }
}
