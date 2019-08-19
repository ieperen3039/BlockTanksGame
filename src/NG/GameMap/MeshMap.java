package NG.GameMap;

import NG.Camera.Camera;
import NG.CollisionDetection.BoundingBox;
import NG.CollisionDetection.Collision;
import NG.Core.Game;
import NG.DataStructures.Generic.AveragingQueue;
import NG.DataStructures.Generic.Color4f;
import NG.DataStructures.Vector3fx;
import NG.Entities.Entity;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Rendering.GLFWWindow;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Shapes.CustomShape;
import NG.Tools.Logger;
import NG.Tools.Vectors;
import org.joml.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Geert van Ieperen created on 8-8-2019.
 */
public class MeshMap extends AbstractMap {
    private static final float MESH_TILE_SIZE = 100; // at least twice as big as the planes of the map, preferably bigger
    private static final Vector3fc TILE_SIZE_VEC = new Vector3f(MESH_TILE_SIZE, MESH_TILE_SIZE, MESH_TILE_SIZE);
    private final MapChunk[][][] grid;
    private final Path filePath;
    private Vector3i size;
    private final BoundingBox boundingBox;
    private Game game;
    private AveragingQueue culledChunks = new AveragingQueue(2);

    public MeshMap(Path map) throws IOException {
        filePath = map;
        MeshFile file = MeshFile.loadFile(map, new Vector3f(), new Vector3f(10));
        Logger.DEBUG.print("Loaded map " + file + " with " + file.getFaces().size() + " planes");

        boundingBox = new BoundingBox();
        for (Vector3fc v : file.getVertices()) {
            boundingBox.union(v);
        }

        HashMap<Vector3i, CustomShape> shapes = file.splitToShapes(MESH_TILE_SIZE, true);

        Vector3i min = new Vector3i();
        Vector3i max = new Vector3i();
        for (Vector3i v : shapes.keySet()) {
            max.max(v);
            min.min(v);
        }

        size = new Vector3i(max).add(1, 1, 1).sub(min);
        grid = new MapChunk[size.x][size.y][size.z];

        shapes.forEach((p, s) -> {
            int x = p.x - min.x;
            int y = p.y - min.y;
            int z = p.z - min.z;
            grid[x][y][z] = new MeshChunk(this, x, y, z, s.toMeshFile());
        });
    }

    @Override
    public void init(Game game) throws Exception {
        this.game = game;
    }

    @Override
    protected Collision getTileIntersect(Vector3fc origin, Vector3fc direction, int xCoord, int yCoord, int zCoord) {
        if (xCoord > size.x || yCoord > size.y || zCoord > size.z) return null;

        MapChunk mapChunk = grid[xCoord][yCoord][zCoord];
        if (mapChunk == null) return Collision.NONE;

        return mapChunk.getIntersection(origin, direction);
    }

    @Override
    public Vector3ic getMapSize() {
        return size;
    }

    @Override
    public Vector3fc getTileSize() {
        return TILE_SIZE_VEC;
    }

    @Override
    public Collection<MapChunk> getChunks() {
        int xSize = size.x;
        int ySize = size.y;
        int zSize = size.z;

        return new AbstractCollection<>() {
            @Override
            public Iterator<MapChunk> iterator() {
                return new Iterator<>() {
                    int x = 0;
                    int y = 0;
                    int z = 0;
                    MapChunk[] zChunks;

                    @Override
                    public boolean hasNext() {
                        return x < size.x;
                    }

                    @Override
                    public MapChunk next() {
                        MapChunk tgt = zChunks[z];
                        do {
                            z++;
                            if (z == zSize) {
                                z = 0;
                                y++;
                                if (y == ySize) {
                                    y = 0;
                                    x++;
                                }
                                if (x < xSize) {
                                    zChunks = grid[x][y];
                                }
                            }
                        } while (zChunks[z] == null && x < size.x);

                        return tgt;
                    }
                };
            }

            @Override
            public int size() {
                return xSize * ySize * zSize;
            }
        };
    }

    @Override
    public Vector3i getCoordinate(float x, float y, float z) {
        return new Vector3i(
                (int) ((x + boundingBox.minX) / MESH_TILE_SIZE),
                (int) ((y + boundingBox.minY) / MESH_TILE_SIZE),
                (int) ((z + boundingBox.minZ) / MESH_TILE_SIZE)
        );
    }

    @Override
    public Vector3fx getPosition(int x, int y, int z) {
        return new Vector3fx(
                (x + 0.5f) * MESH_TILE_SIZE - boundingBox.minX,
                (y + 0.5f) * MESH_TILE_SIZE - boundingBox.minY,
                (z + 0.5f) * MESH_TILE_SIZE - boundingBox.minZ
        );
    }

    @Override
    public void draw(SGL gl) {
        assert gl.getPosition(Vectors.O).equals(Vectors.O) : "gl object not placed at origin";
        float renderTime = 0; // map is static

        ShaderProgram shader = gl.getShader();
        boolean isMaterialShader = shader instanceof MaterialShader;
        if (isMaterialShader) {
            ((MaterialShader) shader).setMaterial(Material.ROUGH, new Color4f(85, 153, 0, 1));
        }

        GLFWWindow window = game.get(GLFWWindow.class);
        Matrix4f viewProjection = game.get(Camera.class)
                .getViewProjection((float) window.getWidth() / window.getHeight());
        FrustumIntersection fic = new FrustumIntersection().set(viewProjection, false);
        int numOfCulled = 0;

        for (int x = 0; x < grid.length; x++) {
            MapChunk[][] row = grid[x];

            for (int y = 0; y < row.length; y++) {
                MapChunk[] chunks = row[y];

                for (int z = 0; z < chunks.length; z++) {
                    MapChunk chunk = chunks[z];
                    if (chunk == null) continue;

                    boolean isVisible = fic.testAab(
                            x * MESH_TILE_SIZE, y * MESH_TILE_SIZE, z * MESH_TILE_SIZE,
                            (x + 1) * MESH_TILE_SIZE, (y + 1) * MESH_TILE_SIZE, (z + 1) * MESH_TILE_SIZE
                    );

                    if (isVisible) {
                        chunk.draw(gl, renderTime);
                    } else {
                        numOfCulled++;
                    }
                }
            }
        }

        culledChunks.add(numOfCulled);
    }

    @Override
    public boolean checkMouseClick(MouseTool tool, int xSc, int ySc) {
        return checkMouseClick(tool, xSc, ySc, game);
    }

    @Override // workaround, no need to store entire mesh
    public void writeToDataStream(DataOutputStream out) throws IOException {
        out.writeUTF(filePath.toString());
    }

    public MeshMap(DataInputStream in) throws IOException {
        this(new File(in.readUTF()).toPath());
    }

    @Override
    public void cleanup() {
        getChunks().forEach(Entity::dispose);
    }

    private static class MeshChunk extends AbstractChunk {
        MeshChunk(GameMap parent, int xCoord, int yCoord, int zCoord, MeshFile elt) {
            super(parent, xCoord, yCoord, zCoord, elt);
        }

        @Override
        public void update(float gameTime) {

        }
    }
}
