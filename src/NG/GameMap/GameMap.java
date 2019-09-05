package NG.GameMap;

import NG.Core.GameAspect;
import NG.DataStructures.Vector3fx;
import NG.DataStructures.Vector3fxc;
import NG.InputHandling.MouseTools.MouseToolListener;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.Collection;

/**
 * An object that represents the world where all other entities stand on. This includes both the graphical and the
 * physical representation. The map considers a difference between coordinates and position, in that a coordinate may be
 * of different magnitude than an equivalent position.
 * @author Geert van Ieperen. Created on 29-9-2018.
 */
public interface GameMap extends GameAspect, MouseToolListener, Storable {

    /**
     * maps a real position to the nearest coordinate.
     * @param position a position in real space
     * @return the coordinate that is closest to the given position.
     * @see #getCoordinate(Vector3fxc)
     */
    Vector3i getCoordinate(Vector3fxc position);

    Vector3i getCoordinate(float x, float y, float z);

    /**
     * maps a coordinate to a real position
     * @param mapCoord a coordinate on the map
     * @return a vector such that {@link #getCoordinate(Vector3fxc)} will result in {@code mapCoord}
     */
    Vector3fx getPosition(Vector3ic mapCoord);

    Vector3fx getPosition(int x, int y, int z);

    /**
     * draws the map on the screen.
     * @param gl the gl object to draw with
     */
    void draw(SGL gl);

    /**
     * the number of coordinates in x and y direction. The real (floating-point) size can be computed by {@code getMapSize() * }{@link #getTileSize()}
     */
    Vector3ic getMapSize();

    /**
     * the size of a single individual tile.
     */
    Vector3fc getTileSize();

    /**
     * @return this map represented as a collection of entities
     */
    Collection<MapChunk> getChunks();

    interface ChangeListener {
        /** is called when the map is changed */
        void onMapChange();
    }
}
