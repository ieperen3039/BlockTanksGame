package NG.Entities.Projectiles;

import NG.CollisionDetection.BoundingBox;
import NG.CollisionDetection.Collision;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.Entities.Entity;
import NG.Entities.MovingEntity;
import NG.Entities.MutableState;
import NG.Entities.State;
import NG.Rendering.MatrixStack.SGL;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Collections;
import java.util.List;

/**
 * @author Geert van Ieperen created on 2-4-2019.
 */
public abstract class Projectile extends MovingEntity {
    private static final BoundingBox zeroBox = new BoundingBox(0, 0, 0, 0, 0, 0);

    protected Game game;
    private float spawnTime = Float.NEGATIVE_INFINITY;
    private Entity source;

    public Projectile(MutableState spawnState) {
        super(spawnState);
    }

    @Override
    public void preUpdate(Game game, float gameTime, float deltaTime) {
        if (gameTime > spawnTime) {
            super.preUpdate(game, gameTime, deltaTime);
        }
    }

    public void init(Game game, Entity source, float spawnTime){
        this.game = game;
        this.source = source;
        this.spawnTime = spawnTime;
        this.state.setTime(spawnTime);
    }

    @Override
    public void draw(SGL gl, float renderTime) {
        if (isDisposed) return;
        float now = game.get(GameTimer.class).getRendertime();
        if (now < spawnTime) return;

        gl.pushMatrix();
        {
            gl.translateRotate(getStateAt(now));
            drawProjectile(gl, now);
        }
        gl.popMatrix();

        disposeStatesUntil(now);
    }

    /**
     * @param gl         draw the projectile, without additional positioning
     * @param renderTime the current rendering time
     */
    protected abstract void drawProjectile(SGL gl, float renderTime);

    @Override
    public BoundingBox getHitbox(float time) {
        return zeroBox;
    }

    @Override
    public List<Vector3f> getShapePoints(List<Vector3f> dest, float gameTime) {
        if (dest.size() != 1) dest = Collections.singletonList(new Vector3f());

        getStateAt(gameTime).position().toVector3f(dest.get(0));

        return dest;
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return other != this && other != source;
    }

    @Override
    public void collideWith(Entity other, Collision collision, float collisionTime) {
        State collisionState = getStateAt(collisionTime);
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

    @Override
    public Collision getIntersection(Vector3fc origin, Vector3fc direction) {
        assert false : "Something collided with a bullet";
        return Collision.NONE;
    }
}
