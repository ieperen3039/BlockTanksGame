package NG.Shapes;

import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Shapes.Primitives.Plane;
import NG.Tools.Directory;
import NG.Tools.Logger;
import org.joml.AABBf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * A collection of generic shapes
 * @author Geert van Ieperen. Created on 14-9-2018.
 */
public enum GenericShapes implements Mesh, Shape {
    ARROW("general", "arrow.obj"),
    ICOSAHEDRON("general", "icosahedron.obj"),
    INV_CUBE("general", "inverseCube.obj"),
    CUBE("general", "cube.obj"),
    TEXTURED_QUAD("general", "quad.obj"),

    /** a quad of size 2x2 on the xy plane */
    QUAD(makeSingleQuad());

    private MeshFile pars;
    private Mesh mesh = null;
    private final Shape shape;

    GenericShapes(String... path) {
        Path asPath = Directory.meshes.getPath(path);

        try {
            pars = MeshFile.loadFile(asPath);

        } catch (IOException ex) {
            Logger.ERROR.print(ex);
            shape = null;
            mesh = null;
            return;
        }

        shape = new BasicShape(pars);
    }

    GenericShapes(CustomShape frame) {
        pars = frame.toMeshFile();
        shape = frame.toShape();
    }

    @Override
    public void render(SGL.Painter lock) {
        if (mesh == null) {
            mesh = pars.getMesh();
            pars = null;
        }
        mesh.render(lock);
    }

    @Override
    public void dispose() {
        pars = null;
        if (mesh != null) {
            mesh.dispose();
        }
    }

    @Override
    public List<? extends Plane> getPlanes() {
        return shape.getPlanes();
    }

    @Override
    public List<Vector3fc> getPoints() {
        return shape.getPoints();
    }

    @Override
    public AABBf getBoundingBox() {
        return shape.getBoundingBox();
    }

    private static CustomShape makeSingleQuad() {
        CustomShape frame = new CustomShape();
        frame.addQuad(
                new Vector3f(1, 1, 0),
                new Vector3f(-1, 1, 0),
                new Vector3f(-1, -1, 0),
                new Vector3f(1, -1, 0),
                new Vector3f(0, 0, 1)
        );
        return frame;
    }
}
