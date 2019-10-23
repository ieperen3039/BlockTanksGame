package NG.Blocks.Types;

import NG.Blocks.BasicBlocks;
import NG.DataStructures.Generic.Color4f;
import org.joml.Vector3ic;

public abstract class PieceType {
    public final String name;
    public final Vector3ic dimensions; // in block dimensions
    public final String manufacturer;
    public final float mass;

    public PieceType(String name, String manufacturer, float mass, Vector3ic size) {
        this.name = name;
        this.manufacturer = manufacturer;
        this.mass = mass * BasicBlocks.BLOCK_WEIGHT;
        this.dimensions = size;
    }

    public abstract PieceTypeBlock getRootType();

    public abstract AbstractPiece getInstance(Vector3ic position, int zRotation, Color4f color);
}
