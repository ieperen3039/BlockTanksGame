package NG.Blocks;

import NG.Blocks.Types.PieceType;
import NG.Blocks.Types.PieceTypeJoint;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Shapes.Shape;
import NG.Tools.Directory;
import NG.Tools.Toolbox;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joml.*;

import java.io.IOException;
import java.lang.Math;
import java.nio.file.Path;
import java.util.*;

import static NG.Blocks.Types.AbstractPiece.BLOCK_BASE;

/**
 * a collection of block types. parsing is done as described in res/blocks/readme.txt
 * @author Geert van Ieperen created on 16-8-2019.
 */
public class FilePieceTypeCollection implements PieceTypeCollection {
    public static final float SCALE = 100;
    private static final int COLLISION_FACE_LIMIT = 200;

    private final String manufacturer;
    private final Map<String, PieceType> blocks = new HashMap<>();

    public FilePieceTypeCollection(Path path) throws IOException {
        JsonNode root = new ObjectMapper().readTree(path.resolve("blocks.json").toFile());
        manufacturer = root.findValue("manufacturer").textValue();
        Map<String, PieceType> allBlocks = new HashMap<>();

        /* blocks */

        JsonNode blocksNode = root.findValue("blocks");
        Iterator<Map.Entry<String, JsonNode>> fields = blocksNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> blockNode = fields.next();
            String name = blockNode.getKey();
            JsonNode block = blockNode.getValue();

            JsonNode meshNode = block.findValue("graphic");
            MeshFile mesh;
            if (meshNode != null) {
                String meshFile = meshNode.textValue();
                mesh = MeshFile.loadFile(path.resolve(meshFile),
                        new Vector3f(-BLOCK_BASE / 2, -BLOCK_BASE / 2, 0),
                        new Vector3f(SCALE, SCALE, SCALE)
                );

            } else {
                mesh = MeshFile.EMPTY_FILE;
            }

            JsonNode sizeNode = block.findValue("size");
            Vector3i size = (sizeNode != null) ? readVector3i(sizeNode) : null;

            JsonNode shapeNode = block.findValue("collision");
            Shape shape;
            if (shapeNode != null) {
                String shapeFile = shapeNode.textValue();
                shape = MeshFile.loadFile(path.resolve(shapeFile),
                        new Vector3f(-BLOCK_BASE / 2, -BLOCK_BASE / 2, 0),
                        new Vector3f(SCALE, SCALE, SCALE)
                ).getShape();

                if (size == null) {
                    AABBf bb = shape.getBoundingBox();
                    size = new Vector3i(
                            (int) Math.ceil(bb.maxX - bb.minX),
                            (int) Math.ceil(bb.maxY - bb.minY),
                            (int) Math.ceil(bb.maxZ - bb.minZ)
                    );
                }

            } else {
                if (size == null) throw new IOException("block " + name + " has no size and no hitbox");

                shape = BasicBlocks.get(size).hitbox;
            }

            JsonNode massNode = block.findValue("mass");
            float mass = (massNode != null) ? massNode.floatValue() : 0;

            List<Vector3ic> connections = new ArrayList<>();
            JsonNode mConnectionNode = block.findValue("topPoints");
            if (mConnectionNode != null) {
                for (JsonNode point : mConnectionNode) {
                    connections.add(readVector3i(point));
                }
            }
            int femaleStart = connections.size();

            JsonNode fConnectionNode = block.findValue("bottomPoints");
            if (fConnectionNode != null) {
                for (JsonNode point : fConnectionNode) {
                    connections.add(readVector3i(point));
                }
            }
            PieceType pieceType;

            pieceType = new PieceType(name, mesh, shape, size, mass, connections, femaleStart);

            JsonNode hiddenNode = block.findValue("hidden");
            if (hiddenNode == null || !hiddenNode.asBoolean()) {
                blocks.put(name, pieceType);
            }

            allBlocks.put(name, pieceType);
        }

        /* joints */

        JsonNode jointsNode = root.findValue("joints");
        fields = jointsNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> jointNode = fields.next();
            String name = jointNode.getKey();
            JsonNode joint = jointNode.getValue();

            JsonNode bottomNode = joint.findValue("bottom");
            PieceType bottomPiece;
            if (bottomNode.isArray()) {
                bottomPiece = BasicBlocks.get(readVector3i(bottomNode));
            } else {
                bottomPiece = allBlocks.get(bottomNode.textValue());
            }

            JsonNode topNode = joint.findValue("top");
            PieceType topPiece;
            if (topNode.isArray()) {
                topPiece = BasicBlocks.get(readVector3i(topNode));
            } else {
                topPiece = allBlocks.get(topNode.textValue());
            }

            JsonNode axisNode = joint.findValue("axis");
            char axis = axisNode.textValue().charAt(0);

            boolean hasAngleLimit = false;
            float minAngle = 0;
            float maxAngle = 0;

            JsonNode minAngleNode = joint.findValue("minAngle");
            if (minAngleNode != null) {
                hasAngleLimit = true;

                minAngle = (float) Math.toRadians(minAngleNode.doubleValue());

                JsonNode maxAngleNode = joint.findValue("maxAngle");
                maxAngle = (float) Math.toRadians(maxAngleNode.doubleValue());
            }

            JsonNode jOffNode = joint.findValue("jointOffset");
            Vector3fc jointOffset = readVector3f(jOffNode).mul(SCALE, SCALE, SCALE);

            JsonNode hOffNode = joint.findValue("headOffset");
            Vector3fc headOffset = readVector3f(hOffNode).mul(SCALE, SCALE, SCALE);

            blocks.put(name, new PieceTypeJoint(name, bottomPiece, topPiece, axis, jointOffset, headOffset, hasAngleLimit, minAngle, maxAngle));
        }

        cheatCache.putAll(blocks); // TODO remove
    }

    public FilePieceTypeCollection(String mapName) throws IOException {
        this(Directory.blocks.getPath(mapName));
    }

    @Override
    public String getCategory() {
        return manufacturer;
    }

    private static Vector3i readVector3i(JsonNode vecNode) {
        assert vecNode.isArray() && vecNode.size() == 3 : vecNode;
        return new Vector3i(
                vecNode.get(0).intValue(),
                vecNode.get(1).intValue(),
                vecNode.get(2).intValue()
        );
    }

    private static Vector3f readVector3f(JsonNode vecNode) {
        assert vecNode.isArray() && vecNode.size() == 3 : vecNode;
        return new Vector3f(
                vecNode.get(0).floatValue(),
                vecNode.get(1).floatValue(),
                vecNode.get(2).floatValue()
        );
    }

    /**
     * @param name a (partial) name where is searched by as defined in the descriptor file
     * @return a list with type names closest to the given name first
     */
    public List<PieceType> getSortedByName(String name) {
        List<PieceType> types = new ArrayList<>(blocks.values());
        types.sort(Comparator.comparingInt(a -> Toolbox.hammingDistance(a.name, name)));
        return types;
    }

    @Override
    public PieceType getByName(String name) {
        return blocks.get(name);
    }

    @Override
    public Collection<PieceType> getBlocks() {
        return Collections.unmodifiableCollection(blocks.values());
    }
}
