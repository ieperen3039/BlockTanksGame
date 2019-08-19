package NG.Blocks;

import java.util.Collection;

/**
 * @author Geert van Ieperen created on 16-8-2019.
 */
public interface BlockTypeCollection {

    /**
     * @return the name that must be displayed as to identify this collection of block types
     */
    String getCategory();

    /**
     * @param name an exact tile name as defined in the descriptor file
     * @return the block if it exists, null otherwise
     */
    BlockType getByName(String name);

    /**
     * @return the blocks provided by this collection. May be called multiple times, and may change for automatically
     * generated block types.
     */
    Collection<BlockType> getBlocks();
}
