package NG.GameMap;

import NG.CollisionDetection.BoundingBox;
import NG.CollisionDetection.Collision;
import NG.Entities.StaticEntity;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Shapes.Shape;
import NG.Tools.Vectors;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author Geert van Ieperen created on 12-8-2019.
 */
public abstract class AbstractChunk extends StaticEntity implements MapChunk {
    private MeshFile meshFile;
    private Mesh mesh = null;
    private final Shape shape;
    private final BoundingBox boundingBox;

    public AbstractChunk(GameMap parent, int xCoord, int yCoord, int zCoord, MeshFile file) {
        super(parent.getPosition(xCoord, yCoord, zCoord), 0, new Quaternionf());
        meshFile = file;
        shape = file.getShape();
        boundingBox = new BoundingBox(shape.getBoundingBox(), new Vector3f());
    }

    @Override
    public void draw(SGL gl, float renderTime) {
        if (mesh == null) {
            loadMesh();
        }
        // translated by default
        gl.render(mesh, this);
    }

    @Override
    public void loadMesh() {
        if (mesh != null) return;
        mesh = meshFile.getMesh();
        meshFile = null;
    }

    @Override
    public BoundingBox getHitbox(float time) {
        return boundingBox;
    }

    @Override
    public Collision getIntersection(Vector3fc origin, Vector3fc direction) {
        Collision intersection = shape.getIntersection(origin, direction);
        intersection.convertToGlobal(Vectors.Matrix.IDENTITY);
        return intersection;
    }

    @Override
    public List<Vector3f> getShapePoints(List<Vector3f> dest, float gameTime) {
        List<Vector3fc> points = shape.getPoints();

        int nrOfPoints = points.size();
        for (int i = 0; i < nrOfPoints; i++) {
            Vector3fc point = points.get(i);

            if (i < dest.size()) {
                dest.get(i).set(point);
            } else {
                dest.add(new Vector3f(point));
            }
        }

        while (dest.size() > nrOfPoints){
            dest.remove(nrOfPoints);
        }

        return dest;
    }

    @Override
    public void writeToDataStream(DataOutputStream out) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dispose() {
        if (mesh != null) mesh.dispose();
        super.dispose();
    }
}
