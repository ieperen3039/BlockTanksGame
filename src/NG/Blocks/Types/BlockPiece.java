package NG.Blocks.Types;

import NG.DataStructures.Generic.Color4f;
import NG.Shapes.Shape;
import org.joml.Vector3ic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 14-8-2019.
 */
public class BlockPiece extends AbstractPiece {
    private PieceTypeBlock type;

    protected BlockPiece(PieceTypeBlock type, Vector3ic position, int zRotation, Color4f color) {
        super(position, zRotation, color);
        this.type = type;
    }

    @Override
    public String toString() {
        return type + " " + getHitBox();
    }

    @Override
    public PieceTypeBlock getBaseType() {
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

    public BlockPiece(DataInputStream in, PieceTypeBlock[] typeMap) throws IOException {
        super(in);
        type = typeMap[in.readInt()];
    }

    @Override
    public Shape getShape() {
        return getBaseType().hitbox;
    }
}
