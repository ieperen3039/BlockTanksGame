package NG.Blocks.Types;

import NG.DataStructures.Generic.Color4f;
import NG.Storable;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 14-8-2019.
 */
public class BlockPiece extends AbstractPiece {
    private PieceType type;

    protected BlockPiece(PieceType type, Vector3ic position, int zRotation, Color4f color) {
        super(position, zRotation, color, type.getConnections().size());
        this.type = type;
    }

    @Override
    public String toString() {
        return type + " " + getHitBox();
    }

    @Override
    public PieceType getType() {
        return type;
    }

    @Override
    public AbstractPiece copy() {
        return new BlockPiece(type, position, rotation, color);
    }

    @Override
    public void writeToDataStream(DataOutputStream out, Map<PieceType, Integer> typeMap) throws IOException {
        out.writeInt(typeMap.get(type));

        out.writeInt(position.x);
        out.writeInt(position.y);
        out.writeInt(position.z);

        out.writeByte(rotation);
        Storable.writeColor(out, color);
    }

    BlockPiece(DataInputStream in, PieceType[] typeMap) throws IOException {
        super(
                new Vector3i(
                        in.readInt(), in.readInt(), in.readInt()
                ),
                in.readByte(),
                Storable.readColor(in), typeMap[in.readInt()].getConnections().size()
        );
    }
}
