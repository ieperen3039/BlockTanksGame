package NG.GameMap;

import NG.Camera.Camera;
import NG.CollisionDetection.Collision;
import NG.Core.Game;
import NG.DataStructures.Generic.AABBi;
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
import java.nio.file.Path;
import java.util.Random;
import java.util.*;

import static NG.Blocks.FilePieceTypeCollection.SCALE;

/**
 * @author Geert van Ieperen created on 8-8-2019.
 */
public class MeshMap extends AbstractMap {
    private static final float MESH_TILE_SIZE = 200; // at least 5 times as big as the planes of the map, preferably bigger
    private static final Vector3fc TILE_SIZE_VEC = new Vector3f(MESH_TILE_SIZE, MESH_TILE_SIZE, MESH_TILE_SIZE);
    private MapChunk[][][] grid;
    private AABBi gridRange;
    private Vector3ic coordOffset;
    private Vector3ic size;
    private Game game;
    private AveragingQueue culledChunks = new AveragingQueue(2);
    private File binaryFile;

    public MeshMap(Path path, boolean alwaysReload) throws IOException {
        String fullName = path.getFileName().toString();
        String baseName = fullName.substring(0, fullName.lastIndexOf('.'));
        binaryFile = path.getParent().resolve(baseName + ".binary").toFile();

        if (!alwaysReload && binaryFile.exists()) {
            try (DataInputStream in = Storable.getInputStream(binaryFile)){
                readFromDataStream(in);
                return;

            } catch (Throwable ex){
                Logger.DEBUG.print("Reading map binary caused an " + ex.getClass().getSimpleName());
            }
        }

        DataOutputStream out = Storable.getOutputStream(binaryFile);
        Logger.INFO.print("Creating binary of " + path.getFileName() + "...");

        MeshFile file = MeshFile.loadFile(path, Vectors.O, new Vector3f(SCALE, SCALE, SCALE));
        Logger.DEBUG.print("Loaded map with " + file.getFaces().size() + " planes");

        HashMap<Vector3i, CustomShape> shapes = file.splitToShapes(MESH_TILE_SIZE, true);
        int nrOfChunks = shapes.size();
        Logger.DEBUG.print("Divided map in " + nrOfChunks + " chunks");

        Vector3i min = new Vector3i();
        Vector3i max = new Vector3i();
        for (Vector3i v : shapes.keySet()) {
            max.max(v);
            min.min(v);
        }

        coordOffset = min;
        out.writeInt(min.x);
        out.writeInt(min.y);
        out.writeInt(min.z);

        size = new Vector3i(max).add(1, 1, 1).sub(min);
        out.writeInt(size.x());
        out.writeInt(size.y());
        out.writeInt(size.z());

        grid = new MapChunk[size.x()][size.y()][size.z()];
        gridRange = new AABBi(0, 0, 0, size.x() - 1, size.y() - 1, size.z() - 1);

        out.writeInt(nrOfChunks);
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

        out.close();
        Logger.DEBUG.print("Done loading map, created file " + binaryFile + " of " + out.size() / 1024 + " kB");
    }

    private void readFromDataStream(DataInputStream in) throws IOException {
        coordOffset = new Vector3i(in.readInt(), in.readInt(), in.readInt());
        size = new Vector3i(in.readInt(), in.readInt(), in.readInt());
        grid = new MapChunk[size.x()][size.y()][size.z()];
        gridRange = new AABBi(0, 0, 0, size.x() - 1, size.y() - 1, size.z() - 1);

        int nrOfChunks = in.readInt();
        for (int i = 0; i < nrOfChunks; i++) {
            int x = in.readInt();
            int y = in.readInt();
            int z = in.readInt();
            MeshFile file = new MeshFile(in);
            grid[x][y][z] = new MeshChunk(this, x, y, z, file);
        }
    }

    @Override
    public void init(Game game) throws Exception {
        this.game = game;
        game.executeOnRenderThread(() -> {
            for (MapChunk chunk : getChunks()) {
                chunk.loadMesh();
            }
        });
    }

    @Override
    protected Collision getTileIntersect(Vector3fc origin, Vector3fc direction, int xCoord, int yCoord, int zCoord) {
        if (!gridRange.contains(xCoord, yCoord, zCoord)) return Collision.NONE;

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
        int nrOfChunks = size.x() * size.y() * size.z();

        return new AbstractCollection<>() {
            @Override
            public Iterator<MapChunk> iterator() {
                return new ChunkItr();
            }

            @Override
            public int size() {
                return nrOfChunks;
            }
        };
    }

    @Override
    public Vector3i getCoordinate(float x, float y, float z) {
        return new Vector3i(
                (int) (x / MESH_TILE_SIZE) - coordOffset.x(),
                (int) (y / MESH_TILE_SIZE) - coordOffset.y(),
                (int) (z / MESH_TILE_SIZE) - coordOffset.z()
        );
    }

    @Override
    protected Vector3f exactToCoordinate(float x, float y, float z) {
        return new Vector3f(
                (x / MESH_TILE_SIZE) - coordOffset.x(),
                (y / MESH_TILE_SIZE) - coordOffset.y(),
                (z / MESH_TILE_SIZE) - coordOffset.z()
        );
    }

    @Override
    public Vector3fx getPosition(int x, int y, int z) {
        return new Vector3fx(
                (x + coordOffset.x() + 0.5f) * MESH_TILE_SIZE,
                (y + coordOffset.y() + 0.5f) * MESH_TILE_SIZE,
                (z + coordOffset.z() + 0.5f) * MESH_TILE_SIZE
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

                    boolean isVisible = chunk.getHitbox().testFustrum(fic);

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
        try (InputStream in = new FileInputStream(binaryFile)) {
            in.transferTo(out);
        }
    }

    private MeshMap(DataInputStream in) throws IOException {
        readFromDataStream(in);
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
            return x < size.x();
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
                if (z == size.z()) {
                    z = 0;
                    y++;
                    if (y == size.y()) {
                        y = 0;
                        x++;
                    }
                    if (x == size.x()) {
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
        public void preUpdate(float gameTime) {

        }
    }
}
