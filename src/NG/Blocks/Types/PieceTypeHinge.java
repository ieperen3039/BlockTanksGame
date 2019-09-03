package NG.Blocks.Types;

import NG.DataStructures.Generic.Color4f;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Shapes.Shape;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geert van Ieperen created on 26-8-2019.
 */
public class PieceTypeHinge extends PieceType {
    public final float size;
    public final List<Vector3ic> axes;
    public final List<Vector3fc> hingeOffsets; // vector to start of wheel axis

    public PieceTypeHinge(
            String name, MeshFile file, Shape hitbox, Vector3ic size, float mass,
            List<Vector3ic> connections, int femaleStart, float axisSize, List<Short> axes,
            List<Vector3fc> hingeOffsets
    ) {
        super(name, file, hitbox, size, mass, connections, femaleStart);
        this.size = axisSize;
        this.hingeOffsets = hingeOffsets;

        List<Vector3ic> axisList = new ArrayList<>(axes.size());
        for (short b : axes) {
            axisList.add(new Vector3i(
                    (b == 'x') ? 1 : 0,
                    (b == 'y') ? 1 : 0,
                    (b == 'z') ? 1 : 0
            ));
        }
        this.axes = axisList;
    }

    @Override
    public AbstractPiece getInstance(Vector3ic position, int zRotation, Color4f color) {
        return new WheelBasePiece(this, position, 0, color);
    }
}
