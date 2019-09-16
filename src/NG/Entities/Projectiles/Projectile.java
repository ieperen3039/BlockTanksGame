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

import java.util.Collections;
import java.util.List;

/**
 * @author Geert van Ieperen created on 2-4-2019.
 */
public abstract class Projectile extends MovingEntity {
    private static final BoundingBox zeroBox = new BoundingBox(0, 0, 0, 0, 0, 0);

    protected final Game game;
    private float spawnTime;
    private Entity source;

    public Projectile(Game game, Entity source, float spawnTime, MutableState spawnState) {
        super(spawnState);
        this.game = game;
        this.source = source;
        this.spawnTime = spawnTime;
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

        cleanStatesUntil(now);
    }

    /**
     * @param gl         draw the projectile, without additional positioning
     * @param renderTime the current rendering time
     */
    protected abstract void drawProjectile(SGL gl, float renderTime);

    @Override
    public BoundingBox getHitbox() {
        return zeroBox;
    }

    @Override
    public List<Vector3f> getShapePoints(List<Vector3f> dest) {
        if (dest.size() != 1) dest = Collections.singletonList(new Vector3f());

        state.position().toVector3f(dest.get(0));

        return dest;
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return other != this && other != source;
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
