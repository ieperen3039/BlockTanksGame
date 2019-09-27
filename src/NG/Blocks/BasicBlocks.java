package NG.Blocks;

import NG.Blocks.Types.PieceType;
import NG.Shapes.CustomShape;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static NG.Blocks.Types.AbstractPiece.BLOCK_BASE;
import static NG.Blocks.Types.AbstractPiece.BLOCK_HEIGHT;

/**
 * @author Geert van Ieperen created on 16-8-2019.
 */
public class BasicBlocks implements PieceTypeCollection {
    private static final Map<Vector3ic, PieceType> cache = new HashMap<>();
    private static final Pattern namingScheme = Pattern.compile("block (\\d+)x(\\d+)x(\\d+)");
    public static final float BLOCK_BASE_H = BLOCK_BASE / 2;
    private static final String CATEGORY = "Floating Bricks";

    public BasicBlocks() {
        for (int l = 1; l <= 4; l *= 2) {
            for (int h = l; h <= 8; h *= 2) {
                get(h, l, 1);
                get(h, l, 3);
            }
        }

        get(1, 3, 1);
        get(1, 3, 3);
        get(2, 3, 1);
        get(2, 3, 3);
        get(2, 5, 1);

        PieceTypeCollection.allCollections.put(getCategory(), this);
    }

    public static PieceType get(Vector3ic size) {
        return get(size.x(), size.y(), size.z(), size);
    }

    public static PieceType get(int xSize, int ySize, int zSize) {
        Vector3i size = new Vector3i(xSize, ySize, zSize);
        return get(xSize, ySize, zSize, size);
    }

    private static PieceType get(int xSize, int ySize, int zSize, Vector3ic size) {
        if (cache.containsKey(size)) return cache.get(size);

        if (zSize == 0) {
            return getPlate(xSize, ySize);
        }

        CustomShape block = new CustomShape(new Vector3f(BLOCK_BASE / 2, BLOCK_BASE / 2, BLOCK_HEIGHT / 2));

        Vector3f PPP = new Vector3f(xSize * BLOCK_BASE - BLOCK_BASE_H, ySize * BLOCK_BASE - BLOCK_BASE_H, zSize * BLOCK_HEIGHT);
        Vector3f PPN = new Vector3f(xSize * BLOCK_BASE - BLOCK_BASE_H, ySize * BLOCK_BASE - BLOCK_BASE_H, 0);
        Vector3f PNP = new Vector3f(xSize * BLOCK_BASE - BLOCK_BASE_H, -BLOCK_BASE_H, zSize * BLOCK_HEIGHT);
        Vector3f PNN = new Vector3f(xSize * BLOCK_BASE - BLOCK_BASE_H, -BLOCK_BASE_H, 0);
        Vector3f NPP = new Vector3f(-BLOCK_BASE_H, ySize * BLOCK_BASE - BLOCK_BASE_H, zSize * BLOCK_HEIGHT);
        Vector3f NPN = new Vector3f(-BLOCK_BASE_H, ySize * BLOCK_BASE - BLOCK_BASE_H, 0);
        Vector3f NNP = new Vector3f(-BLOCK_BASE_H, -BLOCK_BASE_H, zSize * BLOCK_HEIGHT);
        Vector3f NNN = new Vector3f(-BLOCK_BASE_H, -BLOCK_BASE_H, 0);

        block.addQuad(PPP, PPN, PNN, PNP);
        block.addQuad(PPN, NPN, NNN, PNN);
        block.addQuad(NPN, NPP, NNP, NNN);
        block.addQuad(NPP, PPP, PNP, NNP);
        block.addQuad(PPP, PPN, NPN, NPP);
        block.addQuad(PNP, PNN, NNN, NNP);

        int nrOfStuds = xSize * ySize;
        Vector3ic[] connections = new Vector3ic[nrOfStuds * 2];
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                connections[x * ySize + y] = new Vector3i(x, y, zSize);
                connections[x * ySize + y + nrOfStuds] = new Vector3i(x, y, 0);
            }
        }

        PieceType newBlock = new PieceType(
                String.format("block %dx%dx%d", xSize, ySize, zSize),
                CATEGORY, block.toMeshFile(), block.toShape(), size,
                xSize * ySize * zSize, Arrays.asList(connections), nrOfStuds
        );

        cache.put(size, newBlock);

        return newBlock;
    }

    public static PieceType getPlate(int xSize, int ySize) {
        CustomShape block = new CustomShape();
        block.addQuad(
                new Vector3f(xSize * BLOCK_BASE - BLOCK_BASE_H, ySize * BLOCK_BASE - BLOCK_BASE_H, 0),
                new Vector3f(xSize * BLOCK_BASE - BLOCK_BASE_H, -BLOCK_BASE_H, 0),
                new Vector3f(-BLOCK_BASE_H, ySize * BLOCK_BASE - BLOCK_BASE_H, 0),
                new Vector3f(-BLOCK_BASE_H, -BLOCK_BASE_H, 0)
        );
        int nrOfStuds = xSize * ySize;
        Vector3ic[] connections = new Vector3ic[nrOfStuds];
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                connections[x * ySize + y] = new Vector3i(x, y, 0);
            }
        }

        // plates are not added to the cache
        return new PieceType(
                String.format("plate %dx%d", xSize, ySize),
                CATEGORY, block.toMeshFile(), block.toShape(), new Vector3i(xSize, ySize, 0),
                xSize * ySize * 0.01f, Arrays.asList(connections), nrOfStuds
        );
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    public PieceType getByName(String name) {
        Matcher matcher = namingScheme.matcher(name);
        if (matcher.matches()) {
            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));
            int z = Integer.parseInt(matcher.group(3));
            return get(x, y, z);
        }
        return null;
    }

    @Override
    public Collection<PieceType> getBlocks() {
        List<PieceType> types = new ArrayList<>(cache.values());
        types.sort(Comparator.comparing(t -> t.name));
        return types;
    }
}
