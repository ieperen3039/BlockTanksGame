package NG.ConstructionMode;

import NG.Blocks.BlockSubGrid;
import NG.Blocks.BlocksConstruction;
import NG.Blocks.PieceTypeCollection;
import NG.Blocks.Types.BlockPiece;
import NG.Blocks.Types.PieceType;
import NG.Camera.Camera;
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
import NG.Tools.Logger;
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
    private BlocksConstruction.GridModificator modificator;
    private BlockPiece currentBlock;
    private Color4f color = Color4f.BLUE;

    public static final Color4f[] COLORS = new Color4f[]{
            new Color4f(1, 0.1f, 0.1f),
            new Color4f(0, 0, 1),
            new Color4f(1f, 1f, 0.1f), // yellow
            new Color4f(0, 1f, 0) // green
    };
    private Runnable returnProcedure;

    public ConstructionMenu(Runnable exit, PieceTypeCollection... blockTypes) {
        super(new BaseLF());
        returnProcedure = exit;

        SButton optionsButton = new SButton("Menu", () -> addElement(getMenu()), 0, 100);
        optionsButton.setGrowthPolicy(true, false);

        SComponentArea blockTypeArea = new SComponentArea(400, 80);

        SButton[] categoryTiles = new SButton[blockTypes.length];
        for (int i = 0; i < blockTypes.length; i++) {
            PieceTypeCollection typeSet = blockTypes[i];
            Collection<PieceType> blocks = typeSet.getBlocks();

            assert !blocks.isEmpty(); // can't handle this yet

            SButton[] buttons = new SButton[blocks.size()];
            int j = 0;
            for (PieceType type : blocks) {
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

        SButton selectNextSubgrid = new SButton("<", () -> {
            modificator.next();
            Camera cam = game.get(Camera.class); // TODO camera rotation upon switching subgrid
//            cam.set(cam.getFocus(), constructor.getGrid().getWorldRotation());
        });
        SButton selectPrevSubgrid = new SButton(">", () -> modificator.previous());

        SPanel mainPanel = SPanel.row(false, false,
                SPanel.column(false, true,
                        optionsButton,
                        categorySelection,
                        blockTypeArea,
                        SPanel.row(colorOptions),
                        SPanel.row(selectNextSubgrid, selectPrevSubgrid)
                ),
                SFiller.get()
        );

        display(mainPanel);
    }

    private SComponent getMenu() {
        SButton.BProps bProps = new SButton.BProps(300, 80, false, false);

        return SPanel.row(
                SFiller.get(),
                SPanel.column(
                        SFiller.get(),
                        new SButton("Save", () -> saveConstruction(construction, "temp.conbi"), bProps),
                        new SButton("Exit", () -> returnProcedure.run(), bProps),
                        SFiller.get()
                ),
                SFiller.get()
        );
    }

    private void saveConstruction(BlocksConstruction construction, String fileName) {

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
                    gl.pushMatrix();
                    {
                        BlockSubGrid g = modificator.getGrid();
                        gl.translate(g.getWorldPosition());
                        gl.rotate(g.getWorldRotation());
                        currentBlock.draw(gl, null);
                    }
                    gl.popMatrix();
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
        modificator = construction.getSubgridModificator();
        game.get(MouseToolCallbacks.class).addKeyPressListener(this);
        game.get(GameState.class).addEntity(construction);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        game.get(MouseToolCallbacks.class).removeListener(this);
    }

    private void select(PieceType type) {
        Vector3ic position = currentBlock == null ? new Vector3i() : currentBlock.getPosition();
        currentBlock = type.getInstance(position, 0, SELECTION_COLOR);
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
                if (modificator.canAttach(currentBlock)) {
                    BlockPiece newAttachment = currentBlock.copy();

                    newAttachment.color = color;
                    modificator.add(newAttachment);

                    Logger.DEBUG.print("Added " + newAttachment);
                }
                break;
        }
    }
}
