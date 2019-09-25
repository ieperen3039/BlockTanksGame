package NG.CollisionDetection;

import NG.DataStructures.Generic.Pair;
import NG.Entities.Entity;
import org.joml.Vector3fc;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * @author Geert van Ieperen created on 10-2-2019.
 */
public class PhysicsEngine extends AbstractGameState {
    private final CollisionDetection entities;

    public PhysicsEngine() {
        entities = new CollisionDetection();
    }

    @Override
    public void update(float gameTime, float deltaTime) {
        entities.forEach(entity -> entity.preUpdate(gameTime, deltaTime));
        entities.processCollisions(gameTime);
        entities.forEach(Entity::postUpdate);
    }

    @Override
    public Pair<Entity, Float> getEntityByRay(Vector3fc origin, Vector3fc dir, float gameTime) {
        return entities.rayTrace(origin, dir, gameTime);
    }

    @Override
    public void addEntity(Entity entity) {
        assert entity != null;
        assert !entities.contains(entity) : "duplicate entity";
        entities.addEntity(entity);
    }

    @Override
    public Collection<Entity> entities() {
        return entities.getEntityList();
    }

    @Override
    public void cleanup() {
        entities.cleanup();
    }

    public PhysicsEngine(DataInputStream in) throws IOException {
        entities = new CollisionDetection();
        readFromDataStream(in);
    }
}
