package NG.Rendering.MeshLoading;

import NG.DataStructures.Generic.Color4f;
import NG.Shapes.BasicShape;
import NG.Shapes.CustomShape;
import NG.Shapes.Shape;
import NG.Tools.Logger;
import NG.Tools.Vectors;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Geert van Ieperen created on 28-2-2019.
 */
public class MeshFile {
    private final List<Vector2fc> textureCoords;
    private final List<Vector3fc> vertices;
    private final List<Vector3fc> normals;
    private final List<Mesh.Face> faces;
    private final List<Color4f> colors;
    private final String name;

    public MeshFile(
            String name, List<Vector2fc> textureCoords, List<Vector3fc> vertices,
            List<Vector3fc> normals, List<Mesh.Face> faces, List<Color4f> colors
    ) {
        this.name = name;
        this.textureCoords = textureCoords;
        this.vertices = vertices;
        this.normals = normals;
        this.faces = faces;
        this.colors = colors;
    }

    public boolean isTextured() {
        return !getTextureCoords().isEmpty();
    }

    public boolean isColored() {
        return !getColors().isEmpty();
    }

    public List<Vector2fc> getTextureCoords() {
        return textureCoords;
    }

    public List<Vector3fc> getVertices() {
        return vertices;
    }

    public List<Vector3fc> getNormals() {
        return normals;
    }

    public List<Color4f> getColors() {
        return colors;
    }

    public List<Mesh.Face> getFaces() {
        return faces;
    }

    public Mesh getMesh() {
        if (isTextured()) {
            return new TexturedMesh(this);
        } else {
            return new FlatMesh(getVertices(), getNormals(), getFaces());
        }
    }

    public Shape getShape(){
        return new BasicShape(vertices, normals, faces);
    }

    public static MeshFile loadFileRequired(Path meshPath) {
        try {
            return loadFile(meshPath, Vectors.O, Vectors.Scaling.UNIFORM);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static MeshFile loadFile(Path file) throws IOException {
        return loadFile(file, Vectors.O, Vectors.Scaling.UNIFORM);
    }

    /**
     * Loads the mesh located at the given file
     * @param file the file to load
     * @param offset the offset of the origin in the mesh, applied after the scaling
     * @param scaling the scaling applied to the model
     * @return the data contained in the file
     * @throws java.io.FileNotFoundException if the file could not be found
     * @throws IOException if the file name has no extension
     */
    public static MeshFile loadFile(Path file, Vector3fc offset, Vector3fc scaling) throws IOException {
        String fileName = file.getFileName().toString();

        if (!fileName.contains(".")) throw new IOException("File name has no extension: " + fileName);
        String extension = fileName.substring(fileName.lastIndexOf('.'));

        try {
            switch (extension) {
                case ".obj":
                    return FileLoaders.loadOBJ(offset, scaling, file, fileName);
                case ".ply":
                    return FileLoaders.loadPLY(offset, scaling, file, fileName);
                default:
                    throw new UnsupportedMeshFileException(fileName);
            }
        } catch (IOException ex) {
            throw new IOException("Error loading " + fileName, ex);
        }
    }


    /**
     * writes an object to the given filename
     * @param targetFile
     * @throws IOException if any problem occurs while creating the file
     */
    public void writeOBJFile(File targetFile) throws IOException {
        PrintWriter writer = new PrintWriter(targetFile, StandardCharsets.UTF_8);
        writeOBJFile(writer);
        writer.close();
        Logger.DEBUG.print("Successfully created obj file: " + targetFile);
    }

    /**
     * writes an object to the given print writer
     * @param writer
     */
    public void writeOBJFile(PrintWriter writer) {
        writer.println("# created using a simple obj writer by Geert van Ieperen");
        writer.println("# calling method: " + Logger.getCallingMethod(2));

        for (Vector3fc vec : vertices) {
            writer.println(String.format(Locale.US, "v %1.08f %1.08f %1.08f", vec.x(), vec.y(), vec.z()));
        }

        for (Vector3fc norm : normals) {
            writer.println(String.format(Locale.US, "vn %1.08f %1.08f %1.08f", norm.x(), norm.y(), norm.z()));
        }

        for (Vector2fc texCoord : textureCoords) {
            writer.println(String.format(Locale.US, "vt %1.08f %1.08f", texCoord.x(), texCoord.y()));
        }

        writer.println("usemtl None");
        writer.println("s off");
        writer.println("");

        if (isTextured()) {
            for (Mesh.Face face : faces) {
                assert face.tex != null;
                writer.print("f ");
                for (int i = 0; i < face.size(); i++) {
                    writer.print(" " + String.format("%d/%d/%d", face.vert[i] + 1, face.tex[i] + 1, face.norm[i] + 1));
                }
                writer.println();
            }
        } else {

            for (Mesh.Face face : faces) {
                writer.print("f ");
                for (int i = 0; i < face.size(); i++) {
                    writer.print(" " + String.format("%d//%d", face.vert[i] + 1, face.norm[i] + 1));
                }
                writer.println();
            }
        }
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * creates multiple shapes, splitting it into sections of size containersize.
     * @param containerSize size of splitted container, which is applied in 3 dimensions
     * @return a list of shapes, each being roughly containersize in size
     */
    public List<Shape> splitList(float containerSize) {
        HashMap<Vector3i, CustomShape> world = splitToShapes(containerSize, false);

        Collection<CustomShape> containers = world.values();
        Logger.DEBUG.print("Loaded model " + this + " in " + containers.size() + " parts");

        List<Shape> shapes = new ArrayList<>(containers.size());
        for (CustomShape frame : containers) {
            shapes.add(frame.toShape());
        }
        return shapes;
    }

    /**
     * creates multiple shapes, splitting it into sections of size containersize.
     * @param containerSize size of splitted container, which is applied in 3 dimensions
     * @return a grid of shapes, such that grid[x][y][z] contains all shapes that cover {@code (x, y, z) *
     * containerSize} until {@code (x + 1, y + 1, z + 1) * containerSize}
     */
    public Shape[][][] splitGrid(float containerSize) {
        HashMap<Vector3i, CustomShape> map = splitToShapes(containerSize, true);

        Vector3i min = new Vector3i();
        Vector3i max = new Vector3i();
        for (Vector3i v : map.keySet()) {
            max.max(v);
            min.min(v);
        }
        Vector3i size = new Vector3i(max).sub(min);

        Shape[][][] grid = new Shape[size.x][size.y][size.z];
        map.forEach((p, s) -> {
            int x = p.x - min.x;
            int y = p.y - min.y;
            int z = p.z - min.z;
            grid[x][y][z] = s.toShape();
        });

        return grid;
    }

    /**
     * creates multiple shapes from this file, splitting it into sections of size containersize. The keys of the
     * returned map indicate the index a tile would have in a grid
     * @param containerSize the minimum size of the resulting sections
     * @param doExact       Planes may span multiple containers. When true, these planes will occur in each of these
     *                      containers, possibly split in new planes. When false, the planes only occur in the container
     *                      with the lowest coordinate.
     * @return a map that maps coordinates to containers, which has all planes of this mesh at least once, possibly split.
     */
    public HashMap<Vector3i, CustomShape> splitToShapes(float containerSize, boolean doExact) {
        HashMap<Vector3i, CustomShape> world = new HashMap<>();
        List<Vector3fc> vertices = getVertices();
        Vector3i coord = new Vector3i();
        Vector3i min = new Vector3i();
        Vector3i max = new Vector3i();

        for (Mesh.Face f : getFaces()) {
            Vector3fc[] edges = new Vector3fc[f.size()];
            min.zero();
            max.zero();

            for (int i = 0; i < f.size(); i++) {
                Vector3fc v = vertices.get(f.vert[i]);
                edges[i] = v;
                coord.set((int) (v.x() / containerSize), (int) (v.y() / containerSize), (int) (v.z() / containerSize));
                max.max(coord);
                min.min(coord);
            }

            if (!doExact) max.set(min);

            Vector3i key = new Vector3i();
            for (int x = min.x; x < max.x; x++) {
                for (int z = min.z; z < max.z; z++) {
                    for (int y = min.y; y < max.y; y++) {
                        // on average, this is executed (tileSize / containerSize) times - usually only once

                        Vector3f normal = new Vector3f();
                        for (int ind : f.norm) {
                            if (ind < 0) continue;
                            normal.add(getNormals().get(ind));
                        }
                        if (Vectors.isScalable(normal)) {
                            normal.normalize();
                        } else {
                            normal = null;
                            Logger.DEBUG.printSpamless(toString(), this + " has at least one not-computed normal");
                        }

                        key.set(x, y, z);
                        if (!world.containsKey(key)){
                            world.put(new Vector3i(key), new CustomShape());
                        }
                        world.get(key).addPlane(normal, edges);
                    }
                }
            }
        }
        return world;
    }

    public static class UnsupportedMeshFileException extends IOException {
        public UnsupportedMeshFileException(String fileName) {
            super(fileName);
        }
    }
}
