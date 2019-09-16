package NG.Entities;

import NG.DataStructures.Generic.Pair;
import NG.DataStructures.Interpolation.StateInterpolator;
import NG.DataStructures.Vector3fx;
import org.joml.Vector3f;

/**
 * @author Geert van Ieperen created on 5-9-2019.
 */
public abstract class MovingEntity implements Entity {
    protected MutableState state;
    protected StateInterpolator pastStates;
    protected boolean isDisposed = false;

    public MovingEntity(State spawnState) {
        state = new MutableState(spawnState);
        pastStates = new StateInterpolator(spawnState.copy(), spawnState.time());
    }

    @Override
    public void preUpdate(float gameTime) {
        state.update(gameTime);
    }

    @Override
    public void postUpdate() {
        pastStates.add(state.copy(), state.time());
    }

    @Override
    public State getPhysicsState() {
        return state;
    }

    @Override
    public State getStateAt(float gameTime) {
        Pair<State, Float> lastState = pastStates.getLast();
        if (lastState.right < gameTime){
            lastState.left.interpolateTime(state, gameTime);
        }

        return pastStates.getInterpolated(gameTime);
    }

    /**
         * sets the state of this entity, as per teleportation. Unspecified properties should be reset.
         * @param newState the new state properties.
         */
    public void setState(State newState) {
        float time = newState.time();
        pastStates.add(getStateAt(time), time);
        state.set(newState);
        pastStates.add(newState.copy(), time);
    }

    public void cleanStatesUntil(float minimumTime){
        pastStates.removeUntil(minimumTime);
    }

    @Override
    public void dispose() {
        isDisposed = true;
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return other != this;
    }

    public abstract float getMass();

    /**
     * calculates the new velocity of the target entity, when colliding with other on the given moment in time.
     * This assumes the centers of mass of both entities collide as if they where spheres.
     * @param target the entity to recalculate the speed of
     * @param other the entity which is collided with
     * @param collisionTime the moment of collision
     * @return the new velocity
     */
    public static Vector3f sphereCollisionVelocity(
            MovingEntity target, MovingEntity other, float collisionTime
    ) {
        Vector3f temp = new Vector3f();
        State thisState = target.getStateAt(collisionTime);
        Vector3f thisVel = new Vector3f(thisState.velocity());

        State otherState = other.getStateAt(collisionTime);
        Vector3f otherVel = new Vector3f(otherState.velocity());

        Vector3f otherToThis = new Vector3fx(otherState.position()).sub(thisState.position()).toVector3f();

        float dotProduct = thisVel.sub(otherVel, temp).dot(otherToThis);
        float scalarLeft = ((2 * other.getMass()) / (target.getMass() + other.getMass()));
        float scalarMiddle = dotProduct / otherToThis.lengthSquared();
        thisVel.sub(otherToThis.mul(scalarLeft * scalarMiddle, temp));

        float adjEnergy = (float) Math.sqrt(1 / target.getMass());
        thisVel.add(otherToThis.mul(adjEnergy, temp));

        return thisVel;
    }
}
