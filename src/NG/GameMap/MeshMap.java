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
import NG.Storable;
import NG.Tools.Logger;
import NG.Tools.Vectors;
import org.joml.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.*;

/**
 * @author Geert van Ieperen created on 8-8-2019.
 */
public class MeshMap extends AbstractMap {
    private static final float MESH_TILE_SIZE = 20; // at least 5 times as big as the planes of the map, preferably bigger
    private static final Vector3fc TILE_SIZE_VEC = new Vector3f(MESH_TILE_SIZE, MESH_TILE_SIZE, MESH_TILE_SIZE);
    private final MapChunk[][][] grid;
    private Vector3i size;
    private final BoundingBox boundingBox;
    private Game game;
    private AveragingQueue culledChunks = new AveragingQueue(2);
    private Path binaryFile;

    public MeshMap(Path path) throws IOException {
        String fullName = path.getFileName().toString();
        String baseName = fullName.substring(0, fullName.lastIndexOf('.'));
        binaryFile = path.getParent().resolve(baseName + ".mesbi");

//        Files.delete(binaryFile); // DEBUG - RELOADS THE MAP FROM SCRATCH

        if (Files.exists(binaryFile)) {
            DataInputStream in = Storable.getInputStream(binaryFile);

            size = new Vector3i(in.readInt(), in.readInt(), in.readInt());
            boundingBox = new BoundingBox(
                    in.readFloat(), in.readFloat(), in.readFloat(),
                    in.readFloat(), in.readFloat(), in.readFloat()
            );

            grid = new MapChunk[size.x][size.y][size.z];

            int nrOfChunks = in.readInt();
            for (int i = 0; i < nrOfChunks; i++) {
                int x = in.readInt();
                int y = in.readInt();
                int z = in.readInt();
                MeshFile file = new MeshFile(in);
                grid[x][y][z] = new MeshChunk(this, x, y, z, file);
            }
            in.close();

        } else {
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(binaryFile.toFile()), 1024 * 1024));
            Logger.INFO.print("Creating binary of " + path.getFileName() + "...");

            MeshFile file = MeshFile.loadFile(path);
            Logger.DEBUG.print("Loaded map with " + file.getFaces().size() + " planes");

            HashMap<Vector3i, CustomShape> shapes = file.splitToShapes(MESH_TILE_SIZE, true);
            int nrOfChunks = shapes.size();
            Logger.DEBUG.print("Divided map in " + nrOfChunks + " chunks");

            boundingBox = new BoundingBox();
            for (Vector3fc v : file.getVertices()) {
                boundingBox.union(v);
            }

            Vector3i min = new Vector3i();
            Vector3i max = new Vector3i();
            for (Vector3i v : shapes.keySet()) {
                max.max(v);
                min.min(v);
            }

            size = new Vector3i(max).add(1, 1, 1).sub(min);
            out.writeInt(size.x);
            out.writeInt(size.y);
            out.writeInt(size.z);

            out.writeFloat(boundingBox.minX);
            out.writeFloat(boundingBox.minY);
            out.writeFloat(boundingBox.minZ);
            out.writeFloat(boundingBox.maxX);
            out.writeFloat(boundingBox.maxY);
            out.writeFloat(boundingBox.maxZ);

            grid = new MapChunk[size.x][size.y][size.z];

            Logger.DEBUG.print("Defined bounds and size, map is " + Vectors.toString(boundingBox.size()) + " in size");


            out.writeInt(nrOfChunks);
            int i = 1;
            for (Map.Entry<Vector3i, CustomShape> entry : shapes.entrySet()) {
                Vector3i p = entry.getKey();
                CustomShape s = entry.getValue();
                int x = p.x - min.x;
                int y = p.y - min.y;
                int z = p.z - min.z;
                MeshFile chunkMesh = s.toMeshFile();

                out.writeInt(x);
                out.writeInt(y);
                out.writeInt(z);
                chunkMesh.writeToDataStream(out);

                grid[x][y][z] = new MeshChunk(this, x, y, z, chunkMesh);
            }
            Logger.DEBUG.newLine();

            out.close();
            Logger.DEBUG.print("Done loading map, created binary " + binaryFile + " of " + out.size() / 1024 + "kB");
        }
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
        int size = this.size.x * this.size.y * this.size.z;

        return new AbstractCollection<>() {
            @Override
            public Iterator<MapChunk> iterator() {
                return new ChunkItr();
            }

            @Override
            public int size() {
                return size;
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

        Random rand = new Random(0);

        for (MapChunk[][] row : grid) {
            for (MapChunk[] chunks : row) {
                for (MapChunk chunk : chunks) {
                    if (chunk == null) continue;

                    if (isMaterialShader) ((MaterialShader) shader).setMaterial(Material.ROUGH, Color4f.rgb(
                            rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), 1));

                    boolean isVisible = chunk.getBoundingBox().testFustrum(fic);

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

    @Override
    public void cleanup() {
        getChunks().forEach(Entity::dispose);
    }

    @Override
    public void writeToDataStream(DataOutputStream out) throws IOException {
        // transfer contents of the binary file
        try (InputStream in = new FileInputStream(binaryFile.toFile())) {
            in.transferTo(out);
        }
    }

    private MeshMap(DataInputStream in) throws IOException {
        size = new Vector3i(in.readInt(), in.readInt(), in.readInt());
        boundingBox = new BoundingBox(
                in.readFloat(), in.readFloat(), in.readFloat(),
                in.readFloat(), in.readFloat(), in.readFloat()
        );

        grid = new MapChunk[size.x][size.y][size.z];

        int nrOfChunks = in.readInt();
        for (int i = 0; i < nrOfChunks; i++) {
            int x = in.readInt();
            int y = in.readInt();
            int z = in.readInt();
            MeshFile file = new MeshFile(in);
            grid[x][y][z] = new MeshChunk(this, x, y, z, file);
        }
    }

    private class ChunkItr implements Iterator<MapChunk> {
        int x = 0;
        int y = 0;
        int z = -1;
        MapChunk[] zChunks;

        public ChunkItr() {
            this.zChunks = grid[0][0];
            progress();
        }

        @Override
        public boolean hasNext() {
            return x < size.x;
        }

        @Override
        public MapChunk next() {
            MapChunk tgt = zChunks[z];
            progress();
            return tgt;
        }

        private void progress() {
            do {
                z++;
                if (z == size.z) {
                    z = 0;
                    y++;
                    if (y == size.y) {
                        y = 0;
                        x++;
                    }
                    if (x == size.x) {
                        return;

                    } else {
                        zChunks = grid[x][y];
                    }
                }
            } while (zChunks[z] == null);
        }
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
