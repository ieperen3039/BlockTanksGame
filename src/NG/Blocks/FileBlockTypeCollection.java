package NG.Blocks;

import NG.Rendering.MeshLoading.MeshFile;
import NG.Tools.Directory;
import NG.Tools.Toolbox;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static NG.Blocks.Block.BLOCK_BASE;

/**
 * a collection of block types
 * @author Geert van Ieperen created on 16-8-2019.
 */
public class FileBlockTypeCollection implements BlockTypeCollection {
    private final String manufacturer;
    private final Map<String, BlockType> blocks = new HashMap<>();

    public FileBlockTypeCollection(Path path) throws IOException {
        JsonNode root = new ObjectMapper().readTree(path.resolve("blocks.json").toFile());
        manufacturer = root.findValue("manufacturer").textValue();

        JsonNode blocksNode = root.findValue("blocks");
        Iterator<Map.Entry<String, JsonNode>> fields = blocksNode.fields();
        while (fields.hasNext()){
            Map.Entry<String, JsonNode> blockNode = fields.next();
            String name = blockNode.getKey();
            JsonNode block = blockNode.getValue();

            JsonNode meshNode = block.findValue("file");
            String meshFile = meshNode.textValue();
            MeshFile mesh = MeshFile.loadFile(path.resolve(meshFile),
                    new Vector3f(-BLOCK_BASE/2, -BLOCK_BASE/2, 0),
                    new Vector3f(100, 100, 100)
            );

            JsonNode sizeNode = block.findValue("size");
            Vector3i size = readVector3i(sizeNode);

            JsonNode massNode = block.findValue("mass");
            float mass = massNode.floatValue();

            List<Vector3ic> connections = new ArrayList<>();
            JsonNode mConnectionNode = block.findValue("topPoints");
            for (JsonNode point : mConnectionNode) {
                connections.add(readVector3i(point));
            }
            int femaleStart = connections.size();

            JsonNode fConnectionNode = block.findValue("bottomPoints");
            for (JsonNode point : fConnectionNode) {
                connections.add(readVector3i(point));
            }

            BlockType blockType = new BlockType(name, mesh, mesh.getShape(), size, mass, connections, femaleStart);

            blocks.put(name, blockType);
        }
    }

    public FileBlockTypeCollection(String mapName) throws IOException {
        this(Directory.blocks.getPath(mapName));
    }

    @Override
    public String getCategory() {
        return manufacturer;
    }

    private static Vector3i readVector3i(JsonNode vecNode) {
        assert vecNode.isArray() && vecNode.size() == 3;
        return new Vector3i(
                vecNode.get(0).intValue(),
                vecNode.get(1).intValue(),
                vecNode.get(2).intValue()
        );
    }

    /**
     * @param name a (partial) name where is searched by as defined in the descriptor file
     * @return a list with type names closest to the given name first
     */
    public List<BlockType> getSortedByName(String name){
        List<BlockType> types = new ArrayList<>(blocks.values());
        types.sort(Comparator.comparingInt(a -> Toolbox.hammingDistance(a.name, name)));
        return types;
    }

    @Override
    public BlockType getByName(String name){
        return blocks.get(name);
    }

    @Override
    public Collection<BlockType> getBlocks() {
        return Collections.unmodifiableCollection(blocks.values());
    }
}
