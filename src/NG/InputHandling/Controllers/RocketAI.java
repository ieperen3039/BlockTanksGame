package NG.InputHandling.Controllers;

import NG.DataStructures.Vector3fx;
import NG.DataStructures.Vector3fxc;
import NG.Entities.Entity;
import NG.Entities.MovingEntity;
import NG.Entities.State;
import NG.Tools.Vectors;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen. Created on 21-7-2018.
 */
public class RocketAI implements AircraftControls {
    private final float rollFactor;
    private final float pitchFactor;
    private final float yawFactor;
    private static final float THROTTLE_DOT_IGNORE = (float) Math.toRadians(90);

    public final MovingEntity projectile;

    protected Entity target;
    private final float pSpeed;
    private Vector3fx projectilePos;
    protected Vector3fx targetPos;

    protected Vector3f vecToTarget;
    protected Vector3f xVec;
    protected Vector3f yVec;
    protected Vector3f zVec;
    protected float explodeDistSq;
    private final boolean doExtrapolate;

    /**
     * a controller that tries to send the projectile in the anticipated direction of target, assuming the given speed
     * @param projectile      the projectile that is controlled by this controller
     * @param target          the target entity that this projectile tries to hit
     * @param projectileSpeed the assumed (and preferably over-estimated) maximum speed of the given projectile
     * @param explodeDistance only if the controlled entity is within a range of explodeDistance, primaryFire() will
     *                        return true
     */
    public RocketAI(MovingEntity projectile, Entity target, float projectileSpeed, float explodeDistance) {
        this.projectile = projectile;
        this.target = target;
        this.pSpeed = projectileSpeed;
        this.explodeDistSq = explodeDistance * explodeDistance;
        doExtrapolate = true;
        projectilePos = new Vector3fx();
        targetPos = new Vector3fx();
        rollFactor = 0.1f;
        pitchFactor = 3f;
        yawFactor = 3f;
    }

    /**
     * a controller that sends the given projectile to the given target
     * @param projectile the projectile that is controlled by this controller
     * @param target     the target entity that this projectile tries to hit
     * @param fireDist   only if the controlled entity is within a range of fireDist, primaryFire() will return true
     * @param rollFac    the roll output is multiplied with this factor
     * @param pitchFac   the pitch output is multiplied with this factor
     * @param yawFac     the yaw output is multiplied with this factor
     */
    public RocketAI(MovingEntity projectile, Entity target, float fireDist, float rollFac, float pitchFac, float yawFac) {
        this.projectile = projectile;
        this.target = target;
        this.pSpeed = 0;
        this.explodeDistSq = fireDist * fireDist;
        doExtrapolate = false;
        projectilePos = new Vector3fx();
        rollFactor = rollFac;
        pitchFactor = pitchFac;
        yawFactor = yawFac;
    }

    @Override
    public void update(float gameTime) {
        if (target.isDisposed()) return;

        State projState = projectile.getStateAt(gameTime);
        projectilePos.set(projState.position());
        targetPos.set(getTargetPosition(gameTime));

        new Vector3fx(targetPos)
                .sub(projectilePos)
                .toVector3f(vecToTarget)
                .normalize();

        Quaternionfc projOrientation = projState.orientation();
        xVec = Vectors.newX().rotate(projOrientation);
        yVec = Vectors.newY().rotate(projOrientation);
        zVec = Vectors.newZ().rotate(projOrientation);

        xVec.normalize();
        yVec.normalize();
        zVec.normalize();
    }

    protected Vector3fxc getTargetPosition(float gameTime) {
        if (target == null) {
            return new Vector3fx();
        }

        State tgtState = target.getStateAt(gameTime);
        Vector3fxc tPos = tgtState.position();
        if (doExtrapolate) {
            return extrapolateTarget(tgtState.velocity(), tPos, projectilePos, pSpeed);

        } else {
            return tPos;
        }
    }

    private boolean arrivesWithin(Vector3fxc tPos, float time) {
        return projectilePos.distanceSquared(tPos) < (time * pSpeed * pSpeed);
    }

    /**
     * @param tVel   velocity of the target
     * @param tPos   current position of the target
     * @param sPos   current position of the source
     * @param sSpeed the estimated speed of source
     * @return position S such that if a position on sPos would move with a speed of sSpeed toward S, he would meet the
     *         target at S
     */
    public static Vector3fxc extrapolateTarget(Vector3fc tVel, Vector3fxc tPos, Vector3fxc sPos, float sSpeed) {
        Vector3f relPos = new Vector3fx(tPos).sub(sPos).toVector3f();

        // || xA + B || = v
        // with A is the target velocity and B the relative position of the target to the projectile
        // solving for x gives a quadratic function, which can be solved using the ABC formula
        float a = tVel.lengthSquared() - sSpeed * sSpeed;
        float b = 2 * tVel.dot(relPos);
        float c = relPos.lengthSquared();
        float d = (b * b) - (4 * a * c);

        if (d > 0) {
            // one of these solutions is negative
            float lambda1 = (-b + (float) Math.sqrt(d)) / (2 * a);
            float lambda2 = (-b - (float) Math.sqrt(d)) / (2 * a);
            float exFac = Math.max(lambda1, lambda2);
            new Vector3fx(tPos).add(tVel.mul(exFac, relPos));
        }

        return tPos;
    }

    @Override
    public float throttle() {
        if (targetPos != null && arrivesWithin(targetPos, 1f)) {
            return 1;
        }

        float dot = xVec.dot(vecToTarget);
        dot += THROTTLE_DOT_IGNORE;
        return bound(dot, 0, 1);
    }

    @Override
    public float pitch() {
        float dot = zVec.dot(vecToTarget);
        return bound(dot * pitchFactor, -1, 1);
    }

    @Override
    public float yaw() {
        float dot = -yVec.dot(vecToTarget);
        return bound(dot * yawFactor, -1, 1);
    }

    @Override
    public float roll() {
        Vector3f cross = vecToTarget.cross(xVec, new Vector3f());
        float dot = zVec.dot(cross);
        return bound(dot * rollFactor, -1, 1);
    }


    public static float bound(float input, float lower, float upper) {
        return (input < lower) ? lower : ((input > upper) ? upper : input);
    }

    @Override
    public boolean primaryFire() {
        return targetPos != null && projectilePos.distanceSquared(targetPos) < explodeDistSq;
    }

    @Override
    public boolean secondaryFire() {
        return false;
    }
}
