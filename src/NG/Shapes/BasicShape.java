package NG.Shapes;

import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Shapes.Primitives.Plane;
import org.joml.AABBf;
import org.joml.Vector3fc;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Geert van Ieperen created on 11-11-2017.
 */
public class BasicShape implements Shape {

    private final AABBf boundingBox;
    private List<Vector3fc> vertices;
    private List<Plane> triangles;

    public BasicShape(MeshFile model) {
        this(model.getVertices(), model.getNormals(), model.getFaces());
    }

    /**
     * reads a model from the given file.
     */
    public BasicShape(
            List<Vector3fc> vertices, List<Vector3fc> normals, List<Mesh.Face> faces
    ) {
        this.vertices = Collections.unmodifiableList(vertices);
        this.triangles = faces.parallelStream()
                .map(f -> Plane.faceToPlane(f, vertices, normals))
                .collect(Collectors.toList());

        boundingBox = vertices.parallelStream()
                .collect(AABBf::new, AABBf::union, AABBf::union);
    }

    @Override
    public List<? extends Plane> getPlanes() {
        return Collections.unmodifiableList(triangles);
    }

    @Override
    public List<Vector3fc> getPoints() {
        return Collections.unmodifiableList(vertices);
    }
    @Override
    public AABBf getBoundingBox() {
        return boundingBox;
    }

}
