package NG.Rendering.MeshLoading;

import NG.DataStructures.Generic.Color4f;
import NG.Shapes.BasicShape;
import NG.Shapes.CustomShape;
import NG.Shapes.Shape;
import NG.Storable;
import NG.Tools.Logger;
import NG.Tools.Vectors;
import org.joml.*;

import java.io.*;
import java.lang.Math;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Geert van Ieperen created on 28-2-2019.
 */
public class MeshFile implements Storable {
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
            return new TexturedMesh(this.getVertices(), this.getFaces(), this.getNormals(), this.getTextureCoords());
        } else {
            return new FlatMesh(getVertices(), getNormals(), getFaces());
        }
    }

    public Shape getShape() {
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
     * @param path    the path to the file to load
     * @param offset  the offset of the origin in the mesh, applied after the scaling
     * @param scaling the scaling applied to the model
     * @return the data contained in the file
     * @throws java.io.FileNotFoundException if the file could not be found
     * @throws IOException                   if the file name has no extension
     */
    public static MeshFile loadFile(Path path, Vector3fc offset, Vector3fc scaling) throws IOException {
        String fileName = path.getFileName().toString();

        if (!fileName.contains(".")) throw new IOException("File name has no extension: " + fileName);
        String extension = fileName.substring(fileName.lastIndexOf('.'));

        try {
            switch (extension) {
                case ".obj":
                    return FileLoaders.loadOBJ(offset, scaling, path, fileName);
                case ".ply":
                    return FileLoaders.loadPLY(offset, scaling, path, fileName);
                case ".mesbi":
                    FileInputStream fin = new FileInputStream(path.toFile());
                    return new MeshFile(new DataInputStream(fin));
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


    @Override
    public void writeToDataStream(DataOutputStream out) throws IOException {
        out.writeUTF(name);

        out.writeInt(vertices.size());
        for (Vector3fc v : vertices) {
            Storable.writeVector3f(out, v);
        }
        out.writeInt(normals.size());
        for (Vector3fc n : normals) {
            Storable.writeVector3f(out, n);
        }
        int nrOfTex = textureCoords.size();
        out.writeInt(nrOfTex);
        for (Vector2fc t : textureCoords) {
            out.writeFloat(t.x());
            out.writeFloat(t.y());
        }
        int nrOfCol = colors.size();
        out.writeInt(nrOfCol);
        for (Color4f c : colors) {
            out.writeFloat(c.red);
            out.writeFloat(c.green);
            out.writeFloat(c.blue);
            out.writeFloat(c.alpha);
        }
        out.writeInt(faces.size());
        for (Mesh.Face face : faces) {
            out.writeByte(face.size());
            for (int vi : face.vert) {
                out.writeInt(vi);
            }
            for (int ni : face.norm) {
                out.writeInt(ni);
            }

            if (nrOfTex != 0) {
                for (int tex : face.tex) {
                    out.writeInt(tex);
                }
            }
            if (nrOfCol != 0) {
                for (int ci : face.col) {
                    out.writeInt(ci);
                }
            }
        }
    }

    public MeshFile(DataInputStream in) throws IOException {
        name = in.readUTF();

        int vSize = in.readInt();
        vertices = new ArrayList<>(vSize);
        for (int i = 0; i < vSize; i++) {
            vertices.add(Storable.readVector3f(in));
        }
        int nSize = in.readInt();
        normals = new ArrayList<>(nSize);
        for (int i = 0; i < nSize; i++) {
            normals.add(Storable.readVector3f(in));
        }
        int tSize = in.readInt();
        textureCoords = new ArrayList<>(tSize);
        for (int i = 0; i < tSize; i++) {
            textureCoords.add(new Vector2f(in.readFloat(), in.readFloat()));
        }
        int cSize = in.readInt();
        colors = new ArrayList<>(cSize);
        for (int i = 0; i < cSize; i++) {
            colors.add(new Color4f(
                    in.readFloat(),
                    in.readFloat(),
                    in.readFloat(),
                    in.readFloat()
            ));
        }
        int fSize = in.readInt();
        faces = new ArrayList<>(fSize);
        for (int i = 0; i < fSize; i++) {
            int nVert = in.readByte(); // nr of vertices of this face
            int[] vert = new int[nVert];
            int[] norm = new int[nVert];
            int[] tex = null;
            int[] col = null;

            for (int j = 0; j < nVert; j++) {
                vert[j] = in.readInt();
            }
            for (int j = 0; j < nVert; j++) {
                norm[j] = in.readInt();
            }

            if (tSize != 0) {
                tex = new int[nVert];
                for (int j = 0; j < nVert; j++) {
                    tex[j] = in.readInt();
                }
            }
            if (cSize != 0) {
                col = new int[nVert];
                for (int j = 0; j < nVert; j++) {
                    col[j] = in.readInt();
                }
            }

            faces.add(new Mesh.Face(vert, norm, tex, col));
        }
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
     * @return a map that maps coordinates to containers, which has all planes of this mesh at least once, possibly
     * split.
     */
    public HashMap<Vector3i, CustomShape> splitToShapes(float containerSize, boolean doExact) {
        HashMap<Vector3i, CustomShape> world = new HashMap<>();
        List<Vector3fc> vertices = getVertices();
        Vector3i coord = new Vector3i();
        Vector3i min = new Vector3i();
        Vector3i max = new Vector3i();

        int bloat = 0;

        List<Mesh.Face> faceList = getFaces();
        int[] ptr = new int[2];
        Logger.printOnline(() -> String.format(
                "dividing map: %d/%d (%1.01f%%) (added %d faces)",
                ptr[0], faceList.size(), (ptr[0] * 100f / faceList.size()), ptr[1])
        );
        for (int j = 0; j < faceList.size(); j++) {
            ptr[0] = j;
            ptr[1] = bloat;
            Mesh.Face face = faceList.get(j);

            Vector3fc[] faceVecs = new Vector3fc[face.size()];

            min.set(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
            max.set(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

            for (int i = 0; i < face.size(); i++) {
                Vector3fc v = vertices.get(face.vert[i]);
                faceVecs[i] = v;
                coord.set((int) Math.floor(v.x() / containerSize), (int) Math.floor(v.y() / containerSize), (int) Math.floor(v
                        .z() / containerSize));
                max.max(coord);
                min.min(coord);
            }

            if (!doExact) max.set(min);

            Vector3i key = new Vector3i();
            for (int x = min.x; x <= max.x; x++) {
                for (int z = min.z; z <= max.z; z++) {
                    for (int y = min.y; y <= max.y; y++) {
                        // on average, this is executed (tileSize / containerSize) times - usually only once

                        Vector3f normal = new Vector3f();
                        for (int ind : face.norm) {
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
                        if (!world.containsKey(key)) {
                            world.put(new Vector3i(key), new CustomShape());
                        }
                        world.get(key).addPlane(normal, faceVecs);
                        bloat++;
                    }
                }
            }
            bloat--;
        }
        if (doExact) Logger.DEBUG.printf("Mesh split: Increased number of faces by %1.02f%%", 100f * bloat / faces.size());
        return world;
    }

    public static class UnsupportedMeshFileException extends IOException {
        public UnsupportedMeshFileException(String fileName) {
            super(fileName);
        }
    }

    public static final MeshFile EMPTY_FILE = new MeshFile("empty", Collections.emptyList(), Collections.emptyList(),
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList()
    ){
        @Override
        public Mesh getMesh() {
            return FlatMesh.EMPTY_MESH;
        }
    };
}
