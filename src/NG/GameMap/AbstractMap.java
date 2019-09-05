package NG.GameMap;

import NG.CollisionDetection.Collision;
import NG.Core.Game;
import NG.DataStructures.Vector3fx;
import NG.DataStructures.Vector3fxc;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Tools.GridRayScanner;
import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

/**
 * An object that represents the world where all other entities stand on. This includes both the graphical and the
 * physical representation. The map considers a difference between coordinates and position, in that a coordinate may be
 * of different magnitude than an equivalent position.
 * @author Geert van Ieperen. Created on 29-9-2018.
 */
public abstract class AbstractMap implements GameMap {
    protected GridRayScanner gridScanner;

    public boolean checkMouseClick(MouseTool tool, int xSc, int ySc, Game game) {
        Vector3f origin = new Vector3f();
        Vector3f direction = new Vector3f();
        Vectors.windowCoordToRay(game, xSc, ySc, origin, direction);

        Collision intersection = getGridScanner().getIntersection(origin, direction, false);
        if (!intersection.isCollision()) return false;

        tool.apply(intersection.getHitPos(), xSc, ySc);
        return true;
    }

    @Override
    public Vector3i getCoordinate(Vector3fxc position) {
        float x = position.x();
        float y = position.y();
        float z = position.z();
        return getCoordinate(x, y, z);
    }

    @Override
    public Vector3i getCoordinate(float x, float y, float z) {
        Vector3f v = exactToCoordinate(x, y, z);
        return new Vector3i((int) v.x, (int) v.y, (int) v.z);
    }

    @Override
    public Vector3fx getPosition(Vector3ic mapCoord) {
        int x = mapCoord.x();
        int y = mapCoord.y();
        int z = mapCoord.z();
        return getPosition(x, y, z);
    }

    /**
         * computes the intersection of a ray on the given coordinate
         * @param origin    the origin of the ray in real space
         * @param direction the direction of the ray
         * @param xCoord    the x coordinate
         * @param yCoord    the y coordinate
         * @param zCoord the z coordinate
         * @return the first intersection of the ray with this tile, {@link Float#POSITIVE_INFINITY} if it does not hit and
         * null if the given coordinate is not on the map.
         */
    protected abstract Collision getTileIntersect(
            Vector3fc origin, Vector3fc direction, int xCoord, int yCoord, int zCoord
    );

    public GridRayScanner getGridScanner() {
        if (gridScanner == null) {
            gridScanner = new GridRayScanner(getMapSize(), this::exactToCoordinate, this::getTileIntersect);
        }
        return gridScanner;
    }

    public Vector3f exactToCoordinate(Vector3fc origin) {
        float x = origin.x();
        float y = origin.y();
        float z = origin.z();
        return exactToCoordinate(x, y, z);
    }

    protected abstract Vector3f exactToCoordinate(float x, float y, float z);
}
