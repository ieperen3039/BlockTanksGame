package NG.GUIMenu;

import NG.DataStructures.Generic.Color4f;
import org.joml.Vector2i;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;

/**
 * @author Geert van Ieperen created on 18-5-2019.
 */
public interface GUIPainter {
    /**
     * sets the basic fill color of this painter to the given color
     * @param color a color, where the alpha value gives the opacity of the object
     */
    void setFillColor(Color4f color);

    /**
     * sets the basic stroke syle of this painter
     * @param color the color of the stroke, if alpha is less than 1, the edge of the fill underneath will be visible
     * @param width the width of the stroke in pixels
     */
    void setStroke(Color4f color, int width);

    /**
     * draws a rectangle using the basic fill and stroke style
     * @see #rectangle(int, int, int, int, Color4f, Color4f, int)
     */
    void rectangle(int x, int y, int width, int height);

    /**
     * draws a rectangle on the given position with the given style. After this method call, the colors are reset to the
     * basic colors
     * @param x           the x position in pixels relative to the leftmost position on the GL frame
     * @param y           the y position in pixels relative to the topmost position on the GL frame
     * @param width       the width of this rectangle in pixels
     * @param height      the height of this rectangle in pixels
     * @param fillColor   the color used for the background of this rectangle
     * @param strokeColor the color used for the line around this rectangle
     * @param strokeWidth the width of the line around this rectangle
     */
    void rectangle(
            int x, int y, int width, int height, Color4f fillColor, Color4f strokeColor, int strokeWidth
    );

    /**
     * draws a circle with the given middle and radius
     * @param x      the x coordinate of the middle of this circle
     * @param y      the y coordinate of the middle of this circle
     * @param radius the radius of this circle
     */
    void circle(int x, int y, int radius);

    /**
     * Draws a polygon by drawing a line along the given points, connecting the last point with the first.After this
     * method call, the colors are reset to the basic colors
     * @param fillColor   the color used for the background of this polygon
     * @param strokeColor the color used for the line around this polygon
     * @param strokeWidth the width of the line around this polygon
     * @param points      the points used to draw this polygon
     */
    void polygon(Color4f fillColor, Color4f strokeColor, int strokeWidth, Vector2i... points);

    /**
     * Draws a polygon by drawing a line along the given points, connecting the last point with the first.
     * @param points the points used to draw this polygon
     */
    void polygon(Vector2i... points);

    /**
     * draw a line along the given coordinates
     * @param points (x, y) pairs of screen coordinates
     */
    void line(int strokeWidth, Color4f strokeColor, Vector2i... points);

    /**
     * @param x         x coordinate of the top-left position of the text
     * @param y         y coordinate of the top-left position of the text
     * @param size      font size in pixels
     * @param font      the font to use
     * @param alignment the alignment, one of One of:<br><table><tr>
     *                  <td>{@link Alignment#ALIGN_LEFT}</td>
     *                  <td>{@link Alignment#ALIGN_RIGHT}</td>
     *                  <td>{@link Alignment#ALIGN_TOP}</td>
     *                  <td>{@link Alignment#ALIGN_BOTTOM}</td>
     *                  </tr></table>
     * @param color     the color of the text
     * @param text      the text to write
     */
    void text(int x, int y, float size, NGFonts font, EnumSet<Alignment> alignment, Color4f color, String text);

    enum Alignment {
        ALIGN_LEFT, ALIGN_RIGHT, ALIGN_TOP, ALIGN_BOTTOM
    }

    /** for debugging purposes. Prints the given text in the upper left corner of the screen */
    void printRoll(String text);

    void image(Path filename, int x, int y, int width, int height, float alpha) throws IOException;

    /**
     * executes the action outside the GUI rendering context
     */
    void render(Runnable action);
}
