package NG.Entities;

import NG.CollisionDetection.AbstractState;
import NG.CollisionDetection.Collision;
import NG.DataStructures.Generic.Pair;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Geert van Ieperen created on 17-8-2019.
 */
public class EntityList extends AbstractState {
    private Collection<Entity> entities = new ArrayList<>();

    @Override
    public void update(float gameTime) {

    }

    @Override
    public Pair<Entity, Float> getEntityByRay(Vector3fc origin, Vector3fc dir) {
        Collision earliest = Collision.SCALAR_ONE;
        Entity ety = null;

        for (Entity e : entities) {
            Collision intersection = e.getIntersection(origin, dir);
            if (intersection.isEarlierThan(earliest)) {
                earliest = intersection;
                ety = e;
            }
        }

        return new Pair<>(ety, earliest.getScalar());
    }

    @Override
    public void addEntity(Entity entity) {
        assert entity != null;
        entities.add(entity);
    }

    @Override
    public Collection<Entity> entities() {
        return entities;
    }

    @Override
    public void cleanup() {
        entities.clear();
    }
}
