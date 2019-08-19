package NG.GUIMenu;

import NG.Core.Game;
import NG.DataStructures.Generic.Color4f;
import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.Tools.Logger;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;

import static NG.GUIMenu.GUIPainter.Alignment.*;
import static NG.GUIMenu.NGFonts.ORBITRON_MEDIUM;

/**
 * Little more than the absolute basic appearance of a GUI
 * @author Geert van Ieperen. Created on 21-9-2018.
 */
public class BaseLF implements SFrameLookAndFeel {
    private static final int INDENT = 5;
    private static final int BUTTON_INDENT = 8;
    private static final int STROKE_WIDTH = 2;
    private static final int TEXT_SIZE_REGULAR = 24;
    private static final int TEXT_SIZE_LARGE = 28;

    private static final Color4f TEXT_COLOR = Color4f.BLACK;
    private static final Color4f PANEL_COLOR = Color4f.WHITE;
    private static final Color4f STROKE_COLOR = Color4f.BLUE;
    private static final Color4f BUTTON_COLOR = Color4f.GREY;
    private static final Color4f TOOLBAR_COLOR = Color4f.WHITE;
    private static final Color4f INPUT_FIELD_COLOR = Color4f.LIGHT_GREY;
    private Color4f SELECTION_COLOR = Color4f.TRANSPARENT_GREY;

    private GUIPainter hud;

    @Override
    public void init(Game game) {
        if (!game.getVersion().isLessThan(2, 0)) {
            Logger.ASSERT.print(this + " is ugly. Please install something better");
        }
    }

    @Override
    public void setPainter(GUIPainter painter) {
        this.hud = painter;
        painter.setFillColor(PANEL_COLOR);
        painter.setStroke(STROKE_COLOR, STROKE_WIDTH);
    }

    @Override
    public GUIPainter getPainter() {
        return hud;
    }

    @Override
    public void drawColored(UIComponent type, Color4f color, Vector2ic pos, Vector2ic dim) {
        int x = pos.x();
        int y = pos.y();
        int width = dim.x();
        int height = dim.y();
        assert width > 0 && height > 0 : String.format("Non-positive dimensions: height = %d, width = %d", height, width);

        switch (type) {
            case SCROLL_BAR_BACKGROUND:
                break;

            case BUTTON_ACTIVE:
            case BUTTON_INACTIVE:
            case SCROLL_BAR_DRAG_ELEMENT:
                drawRoundedRectangle(x, y, width, height, (color == null) ? BUTTON_COLOR : color);
                break;

            case BUTTON_PRESSED:
                Color4f col = ((color == null) ? BUTTON_COLOR : color).darken(0.5f);
                drawRoundedRectangle(x, y, width, height, col);
                break;

            case INPUT_FIELD:
                drawRoundedRectangle(x, y, width, height, (color == null) ? INPUT_FIELD_COLOR : color);
                break;

            case SELECTION:
                drawRoundedRectangle(x, y, width, height, (color == null) ? SELECTION_COLOR : color);
                break;

            case DROP_DOWN_HEAD_CLOSED:
            case DROP_DOWN_HEAD_OPEN:
            case DROP_DOWN_OPTION_FIELD:
            case PANEL:
            case FRAME_HEADER:
            default:
                drawRoundedRectangle(x, y, width, height);
        }
    }

    @Override
    public void draw(UIComponent type, Vector2ic pos, Vector2ic dim) {
        drawColored(type, null, pos, dim);
    }

    private void drawRoundedRectangle(int x, int y, int width, int height, Color4f color) {
        int xMax2 = x + width;
        int yMax2 = y + height;

        hud.polygon(color, STROKE_COLOR, STROKE_WIDTH,
                new Vector2i(x + BUTTON_INDENT, y),
                new Vector2i(xMax2 - BUTTON_INDENT, y),
                new Vector2i(xMax2, y + BUTTON_INDENT),
                new Vector2i(xMax2, yMax2 - BUTTON_INDENT),
                new Vector2i(xMax2 - BUTTON_INDENT, yMax2),
                new Vector2i(x + BUTTON_INDENT, yMax2),
                new Vector2i(x, yMax2 - BUTTON_INDENT),
                new Vector2i(x, y + BUTTON_INDENT)
        );
    }

    private void drawRoundedRectangle(int x, int y, int width, int height) {
        int xMax = x + width;
        int yMax = y + height;

        hud.polygon(
                new Vector2i(x + INDENT, y),
                new Vector2i(xMax - INDENT, y),
                new Vector2i(xMax, y + INDENT),
                new Vector2i(xMax, yMax - INDENT),
                new Vector2i(xMax - INDENT, yMax),
                new Vector2i(x + INDENT, yMax),
                new Vector2i(x, yMax - INDENT),
                new Vector2i(x, y + INDENT)
        );
    }

    @Override
    public void drawText(
            Vector2ic pos, Vector2ic dim, String text, NGFonts.TextType type, Alignment align
    ) {
        int x = pos.x();
        int y = pos.y();
        int width = dim.x();
        int height = dim.y();
        int actualSize = TEXT_SIZE_REGULAR;
        Color4f textColor = TEXT_COLOR;
        NGFonts font = ORBITRON_MEDIUM;

        switch (type) {
            case TITLE:
            case ACCENT:
                actualSize = TEXT_SIZE_LARGE;
                break;
            case RED:
                textColor = new Color4f(0.8f, 0.1f, 0.1f);
                break;
        }

        switch (align) {
            case LEFT:
                hud.text(x, y + (height / 2), actualSize,
                        font, EnumSet.of(ALIGN_LEFT), textColor, text
                );
                break;
            case CENTER:
                hud.text(x + (width / 2), y + (height / 2), actualSize,
                        font, EnumSet.noneOf(GUIPainter.Alignment.class), textColor, text
                );
                break;
            case CENTER_TOP:
                hud.text(x + (width / 2), y, actualSize,
                        font, EnumSet.of(ALIGN_TOP), textColor, text
                );
                break;
            case RIGHT:
                hud.text(x + width, y + (height / 2), actualSize,
                        font, EnumSet.of(ALIGN_RIGHT), textColor, text
                );
                break;
        }
    }

    @Override
    public void drawImage(Vector2ic pos, Vector2ic dim, Path file) throws IOException {
        hud.image(file, pos.x(), pos.y(), dim.x(), dim.y(), 1f);
    }

    @Override
    public void cleanup() {
        hud = null;
    }
}
