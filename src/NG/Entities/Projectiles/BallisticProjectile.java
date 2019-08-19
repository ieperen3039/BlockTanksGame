package NG.Entities.Projectiles;

import NG.CollisionDetection.Collision;
import NG.Core.Game;
import NG.DataStructures.Interpolation.StateInterpolator;
import NG.Entities.Entity;
import NG.Entities.MovingEntity;
import NG.Entities.MutableState;
import NG.Entities.State;
import NG.Settings.Settings;
import org.joml.Vector3f;

/**
 * @author Geert van Ieperen created on 14-8-2019.
 */
public abstract class BallisticProjectile extends Projectile {
    private MutableState state;
    private StateInterpolator pastStates;

    public BallisticProjectile(Game game, Entity source, MutableState spawnState) {
        super(game, source, spawnState.time());
        state = spawnState.copy();
        pastStates = new StateInterpolator(10, spawnState, spawnState.time());
    }

    @Override
    public State getCurrentState() {
        return state;
    }

    @Override
    public void update(float gameTime) {
        float dt = gameTime - state.time();
        Vector3f fall = new Vector3f(0, 0, -Settings.GRAVITY_CONSTANT * dt);

        state.setVelocity(fall.add(state.velocity()));
        state.update(gameTime);

        pastStates.add(state.copy(), gameTime);
    }

    @Override
    public State getStateAt(float gameTime) {
        return pastStates.getInterpolated(gameTime);
    }

    @Override
    public void setState(State newState) {
        float time = newState.time();
        state.update(time);
        pastStates.add(state.copy(), time);
        state.set(newState);
        pastStates.add(newState.copy(), time);
    }

    @Override
    public void collideWith(Entity other, Collision collision, float collisionTime) {
        State collisionState = pastStates.getInterpolated(collisionTime);
        float tickTime = state.time();

        Vector3f newVelocity;
        if (other instanceof MovingEntity) {
            newVelocity = MovingEntity.sphereCollisionVelocity(this, (MovingEntity) other, collisionTime);
        } else {
            newVelocity = new Vector3f(collisionState.velocity()).reflect(collision.getNormal());
        }

        state.set(collisionState);
        state.setVelocity(newVelocity);

        state.update(tickTime);
    }
}
