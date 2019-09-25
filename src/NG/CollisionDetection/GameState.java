package NG.CollisionDetection;

import NG.Core.GameAspect;
import NG.DataStructures.Generic.Pair;
import NG.Entities.Entity;
import NG.Entities.MovingEntity;
import NG.InputHandling.MouseTools.MouseToolListener;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import org.joml.Vector3fc;

import java.util.Collection;

/**
 * A collection of all entities in the world, all lights present in the world. Allows querying for specific objects and
 * collisions.
 * @author Geert van Ieperen. Created on 21-9-2018.
 */
public interface GameState extends GameAspect, Storable, MouseToolListener {
    /**
     * update the physics and entities of the state
     * @param gameTime
     * @param deltaTime
     */
    void update(float gameTime, float deltaTime);

    /**
     * draws the objects on the screen, according to the state of a {@link NG.Core.GameTimer} object.
     * @param gl the gl object to draw with
     */
    void drawEntities(SGL gl);

    /**
     * checks which entity is hit by the given ray
     * @param origin the origin of the ray
     * @param dir    the direction of the ray
     * @param gameTime
     * @return Left: the first entity hit by the ray, or null if no entity is hit.
     * <p>
     * Right: the fraction t such that {@code origin + t * dir} gives the point of collision with this entity.
     */
    Pair<Entity, Float> getEntityByRay(Vector3fc origin, Vector3fc dir, float gameTime);

    /**
     * adds an entity to the game in a thread-safe way.
     * @param entity the new entity, with only its constructor called
     */
    void addEntity(Entity entity);

    /**
     * removes the given entity from the gameState. This action does not have to be executed immediately.
     * @param entity an entity to be removed
     * @deprecated instead, call {@link MovingEntity#dispose()}
     */
    @Deprecated
    default void removeEntity(MovingEntity entity) {
        entity.dispose();
    }

    /**
     * @return an unmodifiable view of the entities in this game state. May not contain null values
     */
    Collection<Entity> entities();

}
