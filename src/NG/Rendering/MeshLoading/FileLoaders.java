package NG.Rendering.MeshLoading;

import NG.DataStructures.Generic.Color4f;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Geert van Ieperen created on 6-5-2018.
 */
public final class FileLoaders {

    public static final Pattern SLASH = Pattern.compile("/");

    /**
     * @param offSet offset of the gravity middle in this mesh as the negative of the vector to the gravity middle
     * @param scale  the scaling applied to the loaded object
     * @param path   the path to the object
     * @param name   debug name of the shape
     */
    public static MeshFile loadOBJ(Vector3fc offSet, Vector3fc scale, Path path, String name) throws IOException {
        List<Vector2fc> textureCoords;
        List<Vector3fc> vertices;
        List<Vector3fc> normals;
        List<Mesh.Face> faces;
        List<Color4f> colors;
        textureCoords = new ArrayList<>();
        vertices = new ArrayList<>();
        normals = new ArrayList<>();
        faces = new ArrayList<>();
        colors = new ArrayList<>();

        List<String> lines = Files.readAllLines(path);

        for (String line : lines) {
            String[] tokens = Toolbox.WHITESPACE_PATTERN.split(line);
            switch (tokens[0]) {
                case "v":
                    // Geometric vertex
                    vertices.add(new Vector3f(
                                    Float.parseFloat(tokens[1]),
                                    Float.parseFloat(tokens[2]),
                                    Float.parseFloat(tokens[3])
                            )
                                    .mul(scale)
                                    .add(offSet)
                    );
                    break;
                case "vn":
                    // Vertex normal
                    normals.add(new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    ));
                    break;
                case "vt":
                    textureCoords.add(new Vector2f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2])
                    ));
                    break;
                case "f":
                    faces.add(parseOBJFace(tokens));
                    break;
                default:
                    // Ignore other lines
                    break;
            }
        }

        if (vertices.isEmpty() || faces.isEmpty()) {
            Logger.ERROR.print("Empty mesh loaded: " + path + " (this may result in errors)");
        }

        return new MeshFile(name, textureCoords, vertices, normals, faces, colors);
    }

    /**
     * Read a new PLY file. The resulting file must still be written to the GPU by means of executing {@link
     * MeshFile#getMesh()} on the render thread
     * @param path the path to the .ply file to parse
     * @throws IOException if file not found
     * @throws IOException if file format not supported
     */
    public static MeshFile loadPLY(Vector3fc offSet, Vector3fc scale, Path path, String name) throws IOException {
        int nrOfVertices = -1;
        int nrOfFaces = -1;
        // Open the file as a list of strings
        List<String> lines = Files.readAllLines(path);

        // Check if the file format is correct
        int endHeader = lines.indexOf("end_header");
        if (endHeader == -1) {
            throw new IOException("Unsupported file format. " +
                    "'end_header' keyword is missing");
        }

        List<String> header = new ArrayList<>(lines.subList(0, endHeader)); // exclude "end_header"
        List<String> body = new ArrayList<>(lines.subList(endHeader + 1, lines.size()));

        List<String> colorProps = Arrays.asList("red", "green", "blue");
        boolean hasColors = false;

        for (String line : header) {
            String[] tokens = Toolbox.WHITESPACE_PATTERN.split(line);

            switch (tokens[0]) {
                case "ply":
                case "comment":
                    break;
                case "format":
                    if (!tokens[1].equals("ascii")) {
                        throw new IOException("Not the ASCII format, but " + tokens[1]);
                    }
                    break;
                case "element":
                    if (tokens[1].equals("vertex")) {
                        assert nrOfVertices == -1;
                        nrOfVertices = Integer.parseInt(tokens[2]);

                    } else if (tokens[1].equals("face")) {
                        assert nrOfFaces == -1;
                        nrOfFaces = Integer.parseInt(tokens[2]);

                    } else {
                        throw new IOException("Unsupported element " + tokens[1]);
                    }
                    break;
                case "property":
                    if (colorProps.contains(tokens[1])) {
                        hasColors = true;
                    }

                    break;
                default:
                    throw new IOException("Unsupported keyword " + tokens[0]);
            }
        }

        List<Vector2fc> textureCoords = new ArrayList<>();
        List<Vector3fc> vertices = new ArrayList<>();
        List<Vector3fc> normals = new ArrayList<>();
        List<Mesh.Face> faces = new ArrayList<>();
        List<Color4f> colors = new ArrayList<>();

        // Parse all vertices
        for (int i = 0; i < nrOfVertices; i++) {
            String[] tokens = Toolbox.WHITESPACE_PATTERN.split(body.get(i));

            // Position vector data
            vertices.add(
                    new Vector3f(
                            Float.parseFloat(tokens[0]),
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2])
                    )
                            .mul(scale)
                            .add(offSet)
            );

            // Normal vector data
            normals.add(
                    new Vector3f(
                            Float.parseFloat(tokens[3]),
                            Float.parseFloat(tokens[4]),
                            Float.parseFloat(tokens[5])
                    )
            );

            if (hasColors) {
                // Color data
                colors.add(Color4f.rgb(
                        Integer.parseInt(tokens[6]),
                        Integer.parseInt(tokens[7]),
                        Integer.parseInt(tokens[8])
                ));
            }
        }

        // Parse all faces
        for (int i = nrOfVertices; i < nrOfVertices + nrOfFaces; i++) {
            String[] s = Toolbox.WHITESPACE_PATTERN.split(body.get(i));
            faces.add(parsePLYFace(s));
        }

        return new MeshFile(name, textureCoords, vertices, normals, faces, colors);
    }

    /**
     * parses a face object from a given line of an OBJ formatted file
     * @param tokens a line of a face, split on whitespaces, with 'f' on position 0.
     */
    public static Mesh.Face parseOBJFace(String[] tokens) {
        assert tokens[0].equals("f") : Arrays.toString(tokens);

        int nrOfVerices = tokens.length - 1;

        int[] vert = new int[nrOfVerices];
        int[] norm = new int[nrOfVerices];
        int[] tex = new int[nrOfVerices];

        for (int i = 0; i < nrOfVerices; i++) {
            String[] indices = SLASH.split(tokens[i + 1]);
            vert[i] = readSymbol(indices[0]);
            tex[i] = readSymbol(indices[1]);
            norm[i] = readSymbol(indices[2]);
        }

        if (tex[0] == -1) tex = null;

        return new Mesh.Face(vert, norm, tex, null);
    }

    /**
     * parses a face object from a given line of an PLY formatted file
     * @param tokens a line describing the index of a face
     */
    public static Mesh.Face parsePLYFace(String[] tokens) {
        int nrOfVertices = tokens.length - 1;
        int[] indices = new int[nrOfVertices];

        for (int i = 0; i < nrOfVertices; i++) {
            indices[i] = Integer.parseInt(tokens[i + 1]);
        }

        return new Mesh.Face(indices, indices, indices, indices);
    }

    private static int readSymbol(String index) {
        return index.isEmpty() ? -1 : Integer.parseInt(index) - 1;
    }
}
