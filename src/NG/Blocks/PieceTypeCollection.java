package NG.Blocks;

import NG.Blocks.Types.PieceType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 16-8-2019.
 */
public interface PieceTypeCollection {
    Map<String, PieceType> cheatCache = new HashMap<>(); // tracks all loaded piece types

    /**
     * @return the name that must be displayed as to identify this collection of block types
     */
    String getCategory();

    /**
     * @param name an exact tile name as defined in the descriptor file
     * @return the block if it exists, null otherwise
     */
    PieceType getByName(String name);

    /**
     * @return the blocks provided by this collection. May be called multiple times, and may change for automatically
     * generated block types.
     */
    Collection<PieceType> getBlocks();
}
