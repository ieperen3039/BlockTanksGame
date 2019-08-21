package NG.Blocks.Types;

import NG.DataStructures.Generic.Color4f;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Shapes.CustomShape;
import NG.Shapes.Shape;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.Collections;

import static NG.Blocks.Types.BlockPiece.BLOCK_BASE;

/**
 * @author Geert van Ieperen created on 20-8-2019.
 */
public class PieceTypeWheel extends PieceType {
    public PieceTypeWheel(String name, MeshFile mesh, float radius, float mass) {
        super(
                name, mesh, getWheelBox(radius),
                new Vector3i((int) (2 * radius / BLOCK_BASE + 1), (int) (2 * radius / BLOCK_BASE + 1), 1),
                mass, Collections.emptyList(), 0
        );
    }

    @Override
    public BlockPiece getInstance(Vector3ic position, Color4f color) {
        return new WheelPiece(this, position, color);
    }

    /**
     * creates an octagonal cylinder, topped at one side only
     * @param radius the maximum radius of the cylinder
     * @return an octagonal cylinder at x = [-radius ... radius], y = [-radius ... radius], z = [0 ... 1]
     */
    private static Shape getWheelBox(float radius) {
        CustomShape frame = new CustomShape(new Vector3f(0, 0, 0.5f));
        float sqRad = (float) Math.sqrt(2 * radius * radius);

        Vector3fc A = new Vector3f(radius, 0, 0);
        Vector3fc B = new Vector3f(sqRad, sqRad, 0);
        Vector3fc C = new Vector3f(0, radius, 0);
        Vector3fc D = new Vector3f(-sqRad, sqRad, 0);
        Vector3fc E = new Vector3f(-radius, 0, 0);
        Vector3fc F = new Vector3f(-sqRad, -sqRad, 0);
        Vector3fc G = new Vector3f(0, -radius, 0);
        Vector3fc H = new Vector3f(sqRad, -sqRad, 0);
        Vector3fc A2 = new Vector3f(radius, 0, 1);
        Vector3fc B2 = new Vector3f(sqRad, sqRad, 1);
        Vector3fc C2 = new Vector3f(0, radius, 1);
        Vector3fc D2 = new Vector3f(-sqRad, sqRad, 1);
        Vector3fc E2 = new Vector3f(-radius, 0, 1);
        Vector3fc F2 = new Vector3f(-sqRad, -sqRad, 1);
        Vector3fc G2 = new Vector3f(0, -radius, 1);
        Vector3fc H2 = new Vector3f(sqRad, -sqRad, 1);

        frame.addQuad(A, B, B2, A2);
        frame.addQuad(B, C, C2, B2);
        frame.addQuad(C, D, D2, C2);
        frame.addQuad(D, E, E2, D2);
        frame.addQuad(E, F, F2, E2);
        frame.addQuad(F, G, G2, F2);
        frame.addQuad(G, H, H2, G2);
        frame.addQuad(H, A, A2, H2);

        frame.addQuad(A2, B2, C2, D2);
        frame.addQuad(D2, E2, H2, A2);
        frame.addQuad(E2, F2, G2, H2);

        return frame.toShape();
    }
}
