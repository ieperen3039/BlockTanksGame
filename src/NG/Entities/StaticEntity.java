package NG.Entities;

import NG.CollisionDetection.Collision;
import NG.DataStructures.Vector3fxc;
import org.joml.Quaternionf;

/**
 * an entity that sits still and doesn't move
 * @author Geert van Ieperen created on 26-7-2019.
 */
public abstract class StaticEntity implements Entity {
    private FixedState state;
    private boolean isDisposed = false;

    public StaticEntity(State state) {
        this.state = new FixedState(state);
    }

    public StaticEntity(Vector3fxc position, float currentTime, Quaternionf orientation){
        this(new FixedState(position, orientation, currentTime));
    }

    @Override
    public void preUpdate(float gameTime, float deltaTime) {
        state.update(gameTime);
    }

    @Override
    public State getStateAt(float gameTime) {
        FixedState fixedState = new FixedState(state);
        fixedState.update(gameTime);
        return fixedState;
    }

    @Override
    public void postUpdate(){
    }

    @Override
    public void collideWith(Entity other, Collision collision, float collisionTime) {
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
