package NG.GUIMenu.Frames;

import NG.Core.GameAspect;
import NG.DataStructures.Generic.Color4f;
import NG.GUIMenu.GUIPainter;
import NG.GUIMenu.NGFonts;
import org.joml.Vector2ic;

import java.io.IOException;
import java.nio.file.Path;

/**
 * a stateless mapping from abstract descriptions to drawings in NanoVG
 * @author Geert van Ieperen. Created on 20-9-2018.
 */
public interface SFrameLookAndFeel extends GameAspect {
    /**
     * Draw the given element on the given position
     * @param type the type of element
     * @param pos  the position of the upper left corner of this element in pixels
     * @param dim  the (width, height) of the button in pixels
     */
    void draw(UIComponent type, Vector2ic pos, Vector2ic dim);

    /**
     * Draw the given text on the given position
     * @param type the type of element
     * @param pos  the position of the upper left corner of this element in pixels
     * @param dim  the (width, height) of the button in pixels
     */
    void drawText(
            Vector2ic pos, Vector2ic dim, String text, NGFonts.TextType type, Alignment align
    );

    /**
     * draw a button with an image on it. The image should be scaled uniformly to fit the button
     * @param pos  upper left position of the button
     * @param dim  dimension of the button
     * @param file a path to the file containing the icon to display
     * @throws IOException if the file could not be found or accessed
     */
    void drawImage(Vector2ic pos, Vector2ic dim, Path file) throws IOException;

    /**
     * sets the LF to draw with the specified painter
     * @param painter a new, fresh Painter instance
     */
    void setPainter(GUIPainter painter);

    /**
     * @return the used painter instance
     */
    GUIPainter getPainter();

    enum Alignment {
        LEFT, CENTER, RIGHT,
        CENTER_TOP,
    }

    enum UIComponent {
        /** a simple button, either held down or not held down */
        BUTTON_PRESSED, BUTTON_ACTIVE, BUTTON_INACTIVE,
        /** draw a button with an image on it. The image should be scaled uniformly to fit the button */
        ICON_BUTTON_ACTIVE, ICON_BUTTON_INACTIVE,
        /** The top panel of a dropdown menu. */
        DROP_DOWN_HEAD_CLOSED, DROP_DOWN_HEAD_OPEN,
        /** The background of some elements. */
        DROP_DOWN_OPTION_FIELD, SCROLL_BAR_BACKGROUND, TOOLBAR_BACKGROUND,
        /** The background of a frame */
        PANEL,
        /** the bar on top of a frame carrying the title */
        FRAME_HEADER,
        /** An area with text that hints the user that the text can be changed. */
        INPUT_FIELD,
        /** A marking to indicate that e.g. a textfield is selected. */
        SELECTION,
        /** the drag bar element of a scrollbar */
        SCROLL_BAR_DRAG_ELEMENT,
    }

    /**
     * Draw the given element with the given color on the given position
     * @param type the type of element
     * @param color the color this element must have
     * @param pos  the position of the upper left corner of this element in pixels
     * @param dim  the (width, height) of the button in pixels
     */
    void drawColored(UIComponent type, Color4f color, Vector2ic pos, Vector2ic dim);
}
