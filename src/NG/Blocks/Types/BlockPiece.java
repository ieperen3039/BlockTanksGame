package NG.Blocks.Types;

import NG.DataStructures.Generic.Color4f;
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
        super(position, zRotation, color);
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
    public void write(DataOutputStream out, Map<PieceType, Integer> typeMap) throws IOException {
        Integer typeID = typeMap.computeIfAbsent(type, t -> typeMap.size());
        out.writeInt(typeID);
    }

    public BlockPiece(DataInputStream in, PieceType[] typeMap) throws IOException {
        super(in);
        type = typeMap[in.readInt()];
    }
}
