package NG.Blocks.Types;

import NG.DataStructures.Generic.Color4f;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Shapes.Shape;
import org.joml.Vector3fc;
import org.joml.Vector3ic;

import java.util.List;

/**
 * @author Geert van Ieperen created on 20-8-2019.
 */
public class PieceTypePropeller extends PieceType {
    public final MeshFile propeller;
    public final Vector3fc propellerOffset;
    public final float maxRotSpeed = 10;
    public final float maxForce = 10_000;

    public PieceTypePropeller(
            String name, String category, MeshFile axisMesh, Shape axisShape, Vector3ic axisSize, float mass,
            List<Vector3ic> connections, int femaleStart, MeshFile propeller, Vector3fc propellerOffset
    ) {
        super(
                name, category, axisMesh, axisShape, axisSize, mass, connections, femaleStart
        );
        this.propeller = propeller;
        this.propellerOffset = propellerOffset;
    }

    public PieceTypePropeller(
            String name, String manufacturer, PieceType axisPiece, MeshFile propMesh, Vector3fc propOffset
    ) {
        this(
                name, manufacturer, axisPiece.meshFile, axisPiece.hitbox, axisPiece.dimensions, axisPiece.mass,
                axisPiece.connections, axisPiece.femaleStart, propMesh, propOffset
        );
    }

    @Override
    public AbstractPiece getInstance(Vector3ic position, int zRotation, Color4f color) {
        return new PropellerPiece(this, position, color);
    }
}
