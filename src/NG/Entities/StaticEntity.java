package NG.Entities;

import NG.CollisionDetection.Collision;
import NG.DataStructures.Vector3fxc;
import org.joml.Quaternionf;

/**
 * an entity that sits still and doesn't move
 * @author Geert van Ieperen created on 26-7-2019.
 */
public abstract class StaticEntity implements Entity {
    private State state;
    private boolean isDisposed = false;

    public StaticEntity(State state) {
        this.state = state;
    }

    public StaticEntity(Vector3fxc position, float currentTime, Quaternionf orientation){
        this(new FixedState(position, orientation, currentTime));
    }

    @Override
    public void update(float gameTime) {
        state.update(gameTime);
    }

    @Override
    public State getCurrentState() {
        return state;
    }

    @Override
    public State getStateAt(float gameTime) {
        return state;
    }

    @Override
    public void collideWith(Entity other, Collision collision, float collisionTime) {
        // play sound?
    }

    @Override
    public void dispose() {
        isDisposed = true;
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }
}
