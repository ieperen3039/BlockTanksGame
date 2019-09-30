package NG.Entities;

import NG.CollisionDetection.BoundingBox;
import NG.CollisionDetection.Collision;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

/**
 * An entity is anything that is in the world, excluding the ground itself. Particles and other purely visual elements.
 * @author Geert van Ieperen. Created on 14-9-2018.
 */
public interface Entity extends Storable {
    /**
     * Draws this entity using the provided SGL object. This method may only be called from the rendering loop, and
     * should not change the internal representation of this object. Possible animations should be based on {@link
     * GameTimer#getRendertime()}. Material must be set using {@link SGL#getShader()}.
     * @param gl         the graphics object to be used for rendering. It is initialized at world's origin. (no
     *                   translation or scaling has been applied)
     * @param renderTime the time instance when the rendering must happen
     */
    void draw(SGL gl, float renderTime);

    /**
     * applies physics updates and control updates. The new state is not yet visible to {@link #getStateAt(float)}
     * @param gameTime the current game time
     * @param deltaTime
     * @see State#update(float)
     */
    void preUpdate(float gameTime, float deltaTime);

    /**
     * confirms the current state, making it visible to {@link #getStateAt(float)}
     */
    void postUpdate();

    /**
     * get the state of this entity on the given moment in time, interpolating and extrapolating linearly when
     * necessary.
     * @param gameTime the time where the state must be queried
     * @return the state at the given time using linear interpolation. The default implementation always returns the current state
     */
    State getStateAt(float gameTime);

    /**
     * @return the world-space bounding box of this entity
     * @param time
     */
    BoundingBox getHitbox(float time);

    /**
     * given a point on position {@code origin} and a direction of {@code direction}, calculates a collision with scalar
     * t such that (origin + direction * t) is the first point on this entity
     * @param origin    the origin of the line
     * @param direction the direction and extend of the line
     * @return the first collision of this ray with this entity.
     * @see Collision
     */
    Collision getIntersection(Vector3fc origin, Vector3fc direction);

    /**
     * returns the points of the shape of this entity at the given moment in time
     * @return a list of the exact wolrd-positions of the vertices of the shape of this object. Changes in the list are
     * not reflected in this object.
     * @param gameTime the time where the shape points are requested from
     * @see #getShapePoints(List, float)
     */
    default List<Vector3f> getShapePoints(float gameTime) {
        return getShapePoints(new ArrayList<>(), gameTime);
    }

    /**
     * returns the points of the shape of this entity at the given moment in time.
     * @param dest a list of vectors. If the result requires more or less elements, the redundant elements are deleted,
     *             and required elements are added.
     * @param gameTime
     * @return a list of the exact wolrd-positions of the vertices of the shape of this object. Changes in the list are
     * not reflected in this object.
     */
    List<Vector3f> getShapePoints(List<Vector3f> dest, float gameTime);

    /**
     * @param other another entity
     * @return false if this entity does not respond on a collision with the other entity. In that case, the other
     * entity should also not respond on a collision with this.
     */
    default boolean canCollideWith(Entity other) {
        return (other != this && other instanceof MovingEntity);
    }

    /**
     * process a collision with the other entity, happening at collisionTime. The other entity will be called with this
     * same function, as {@code other.collideWith(this, collisionTime)}.
     * <p>
     * Should not be called if either {@code this.}{@link #canCollideWith(Entity) canCollideWith}{@code (other)} or
     * {@code other.}{@link #canCollideWith(Entity) canCollideWith}{@code (this)}
     * @param other         another entity
     * @param collision
     * @param collisionTime the moment of collision
     */
    void collideWith(Entity other, Collision collision, float collisionTime);

    /**
     * Marks the entity to be invalid, such that the {@link #isDisposed()} method returns true.
     */
    void dispose();

    /**
     * @return true iff this unit should be removed from the game world.
     */
    boolean isDisposed();
}
