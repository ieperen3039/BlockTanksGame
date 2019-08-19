package NG.GameMap;

import NG.CollisionDetection.Collision;
import NG.Core.Game;
import NG.DataStructures.Vector3fx;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Rendering.MatrixStack.SGL;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * an empty map that does absolutely nothing at all
 * @author Geert van Ieperen created on 28-2-2019.
 */
public class EmptyMap extends AbstractMap {
    public EmptyMap() {
    }

    @Override
    public void init(Game game) throws Exception {

    }

    @Override
    public Vector3i getCoordinate(float x, float y, float z) {
        return new Vector3i((int) x, (int) y, (int) z);
    }

    @Override
    public Vector3fx getPosition(int x, int y, int z) {
        return new Vector3fx(x, y, z);
    }

    @Override
    public void draw(SGL gl) {

    }

    @Override
    public Vector3ic getMapSize() {
        return new Vector3i();
    }

    @Override
    public Vector3fc getTileSize() {
        return new Vector3f(1, 1, 1);
    }

    @Override
    public Collection<MapChunk> getChunks() {
        return Collections.emptyList();
    }

    @Override
    public boolean checkMouseClick(MouseTool tool, int xSc, int ySc) {
        return false;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void writeToDataStream(DataOutputStream out) throws IOException {

    }

    public EmptyMap(DataInputStream in) {

    }

    @Override
    protected Collision getTileIntersect(
            Vector3fc origin, Vector3fc direction, int xCoord, int yCoord, int zCoord
    ) {
        return Collision.NONE;
    }
}
