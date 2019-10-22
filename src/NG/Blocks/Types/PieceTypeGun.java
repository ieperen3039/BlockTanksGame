package NG.Blocks.Types;

import NG.DataStructures.Generic.Color4f;
import org.joml.Vector3fc;
import org.joml.Vector3ic;

public class PieceTypeGun extends PieceType {
    public final float reloadTime;
    public final float rotationSpeed;
    public final PieceType topPiece;
    public final Vector3fc jointOffset;
    public final float horzAngle;
    public final float vertMinAngle;
    public final float vertMaxAngle;

    public PieceTypeGun(
            String name, String manufacturer, PieceType bottomPiece, PieceType topPiece, Vector3fc jointOffset,
            float horzAngle, float vertMinAngle, float vertMaxAngle, float reloadTime, float rotationSpeed, float mass
    ) {
        super(
                name, manufacturer, bottomPiece.meshFile, bottomPiece.hitbox, bottomPiece.dimensions,
                mass, bottomPiece.connections, bottomPiece.femaleStart
        );

        this.topPiece = topPiece;
        this.jointOffset = jointOffset;
        this.horzAngle = horzAngle;
        this.vertMinAngle = vertMinAngle;
        this.vertMaxAngle = vertMaxAngle;
        this.reloadTime = reloadTime;
        this.rotationSpeed = rotationSpeed;
    }

    @Override
    public AbstractPiece getInstance(Vector3ic position, int zRotation, Color4f color) {
        return new GunPiece(position, zRotation, color, this);
    }
}
