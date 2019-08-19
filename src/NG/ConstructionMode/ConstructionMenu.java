package NG.ConstructionMode;

import NG.Blocks.Block;
import NG.Blocks.BlockType;
import NG.Blocks.BlockTypeCollection;
import NG.Blocks.BlocksConstruction;
import NG.CollisionDetection.BoundingBox;
import NG.CollisionDetection.GameState;
import NG.Core.Game;
import NG.DataStructures.Generic.Color4f;
import NG.GUIMenu.BaseLF;
import NG.GUIMenu.Components.*;
import NG.GUIMenu.SimpleHUD;
import NG.InputHandling.KeyPressListener;
import NG.InputHandling.MouseToolCallbacks;
import NG.Rendering.MatrixStack.SGL;
import NG.Settings.KeyBinding;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.Collection;

/**
 * @author Geert van Ieperen created on 17-8-2019.
 */
public class ConstructionMenu extends SimpleHUD implements KeyPressListener {
    private static final int NUM_SHOWN_ELTS = 10;
    public static final Color4f SELECTION_COLOR = new Color4f(1f, 1f, 1f, 0.5f);
    private BlocksConstruction construction;
    private Block currentBlock;
    private Color4f color = Color4f.BLUE;

    public static final Color4f[] COLORS = new Color4f[]{
            new Color4f(1, 0.1f, 0.1f),
            new Color4f(0, 0, 1),
            new Color4f(1f, 1f, 0.1f), // yellow
            new Color4f(0, 1f, 0) // green
    };

    public ConstructionMenu(BlockTypeCollection... blockTypes) {
        super(new BaseLF());

        SComponentArea blockTypeArea = new SComponentArea(400, 80);

        SButton[] categoryTiles = new SButton[blockTypes.length];
        for (int i = 0; i < blockTypes.length; i++) {
            BlockTypeCollection typeSet = blockTypes[i];
            Collection<BlockType> blocks = typeSet.getBlocks();

            assert !blocks.isEmpty(); // can't handle this yet

            SButton[] buttons = new SButton[blocks.size()];
            int j = 0;
            for (BlockType type : blocks) {
                buttons[j++] = new SButton(type.name, () -> select(type));
            }

            SScrollableList list = new SScrollableList(NUM_SHOWN_ELTS, buttons);
            categoryTiles[i] = new SButton(typeSet.getCategory(), () -> blockTypeArea.show(list));
        }
        SComponent categorySelection = new SScrollableList(5, categoryTiles);

        SComponent[] colorOptions = new SComponent[COLORS.length];
        for (int i = 0; i < COLORS.length; i++) {
            Color4f c = COLORS[i];
            colorOptions[i] = new SColoredButton(50, 50, c, () -> color = c);
        }

        SPanel mainPanel = SPanel.row(false, false,
                SPanel.column(false, true,
                        categorySelection,
                        blockTypeArea,
                        SPanel.row(colorOptions)
                ),
                new SFiller()
        );

        display(mainPanel);
    }

    @Override
    public void init(Game game) throws Exception {
        super.init(game);

        // override to include cursor block
        construction = new BlocksConstruction() {
            @Override
            public void draw(SGL gl, float renderTime) {
                super.draw(gl, renderTime);
                if (currentBlock != null) {
                    currentBlock.draw(gl, null);
                }
            }

            @Override
            public BoundingBox getBoundingBox() {
                return new BoundingBox(
                        Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY,
                        Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY
                );
            }
        };

        game.get(MouseToolCallbacks.class).addKeyPressListener(this);
        game.get(GameState.class).addEntity(construction);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        game.get(MouseToolCallbacks.class).removeListener(this);
    }

    private void select(BlockType type) {
        Vector3ic position = currentBlock == null ? new Vector3i() : currentBlock.getPosition();
        currentBlock = new Block(type, position, 0, SELECTION_COLOR);
    }

    @Override
    public void keyPressed(int keyCode) {
        if (currentBlock == null) return;

        switch (KeyBinding.get(keyCode)) {
            case BLOCK_MOVE_POS_X:
                currentBlock.move(1, 0, 0);
                break;
            case BLOCK_MOVE_POS_Y:
                currentBlock.move(0, 1, 0);
                break;
            case BLOCK_MOVE_POS_Z:
                currentBlock.move(0, 0, 1);
                break;
            case BLOCK_MOVE_NEG_X:
                currentBlock.move(-1, 0, 0);
                break;
            case BLOCK_MOVE_NEG_Y:
                currentBlock.move(0, -1, 0);
                break;
            case BLOCK_MOVE_NEG_Z:
                currentBlock.move(0, 0, -1);
                break;
            case BLOCK_MOVE_ROT_LEFT:
                currentBlock.rotateZ(true);
                break;
            case BLOCK_MOVE_ROT_RIGHT:
                currentBlock.rotateZ(false);
                break;

            case BLOCK_CONFIRM:
                if (construction.canAttach(currentBlock)) {
                    Block newAttachment = currentBlock;
                    currentBlock = new Block(currentBlock);

                    newAttachment.color = color;
                    construction.add(newAttachment);
                }
                break;
        }
    }
}
