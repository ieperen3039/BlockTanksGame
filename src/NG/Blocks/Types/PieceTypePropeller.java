package NG.Blocks.Types;

import NG.DataStructures.Generic.Color4f;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.MeshLoading.MeshFile;
import org.joml.Vector3fc;
import org.joml.Vector3ic;

/**
 * @author Geert van Ieperen created on 20-8-2019.
 */
public class PieceTypePropeller extends PieceType {
    public final Vector3fc propellerOffset;
    public final float maxRotSpeed = 5;
    public final float maxForce = 100000;
    public PieceTypeBlock axisPiece;

    private final MeshFile propeller;
    private Mesh propellerMesh;

    public PieceTypePropeller(
            String name, String manufacturer, PieceTypeBlock axisPiece, MeshFile propMesh, Vector3fc propOffset
    ) {
        super(                name, manufacturer, axisPiece.mass, axisPiece.dimensions);
        this.axisPiece = axisPiece;
        this.propeller = propMesh;
        this.propellerOffset = propOffset;
    }
    /**
     * must only be called on the rendering thread
     * @return the mesh of the propeller
     */
    public Mesh getPropellerMesh() {
        if (propellerMesh == null) {
            propellerMesh = propeller.getMesh();
        }
        return propellerMesh;
    }

    @Override
    public PieceTypeBlock getRootType() {
        return axisPiece;
    }

    @Override
    public AbstractPiece getInstance(Vector3ic position, int zRotation, Color4f color) {
        return new PropellerPiece(this, position, color, 0);
    }
}
