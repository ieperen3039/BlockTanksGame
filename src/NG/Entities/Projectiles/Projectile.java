package NG.Entities.Projectiles;

import NG.CollisionDetection.BoundingBox;
import NG.CollisionDetection.Collision;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.Entities.Entity;
import NG.Entities.MovingEntity;
import NG.Rendering.MatrixStack.SGL;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Collections;
import java.util.List;

/**
 * @author Geert van Ieperen created on 2-4-2019.
 */
public abstract class Projectile implements MovingEntity {
    protected final Game game;
    private final BoundingBox zeroBox = new BoundingBox(0, 0, 0, 0, 0, 0);
    private float spawnTime;
    private Entity source;

    private boolean isDisposed = false;

    public Projectile(Game game, Entity source, float spawnTime) {
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
    }

    /**
     * @param gl         draw the projectile, without additional positioning
     * @param renderTime the current rendering time
     */
    protected abstract void drawProjectile(SGL gl, float renderTime);

    @Override
    public List<Vector3f> getShapePoints(List<Vector3f> dest) {
        if (dest.size() != 1) dest = Collections.singletonList(new Vector3f());

        getCurrentState().position().toVector3f(dest.get(0));

        return dest;
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
    public BoundingBox getBoundingBox() {
        return zeroBox;
    }

    @Override
    public Collision getIntersection(Vector3fc origin, Vector3fc direction) {
        return Collision.NONE;
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return other != this && other != source;
    }

    @Override
    public void collideWith(Entity other, Collision collision, float collisionTime) {
        assert canCollideWith(other);
        dispose();
    }
}
