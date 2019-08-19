package NG.Entities;

import NG.DataStructures.Vector3fx;
import org.joml.Vector3f;

/**
 * An entity is anything that is in the world, excluding the ground itself. Particles and other purely visual elements.
 * @author Geert van Ieperen. Created on 14-9-2018.
 */
public interface MovingEntity extends Entity {
    /**
     * sets the state of this entity, as per teleportation. Unspecified properties should be reset.
     * @param state the new state properties.
     */
    void setState(State state);

    @Override
    default boolean canCollideWith(Entity other) {
        return other != this;
    }

    /**
     * calculates the new velocity of the target entity, when colliding with other on the given moment in time.
     * This assumes the centers of mass of both entities collide as if they where spheres.
     * @param target the entity to recalculate the speed of
     * @param other the entity which is collided with
     * @param collisionTime the moment of collision
     * @return the new velocity
     */
    static Vector3f sphereCollisionVelocity(MovingEntity target, MovingEntity other, float collisionTime) {
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

    float getMass();
}
