package NG.GUIMenu.Components;

import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.GUIMenu.NGFonts;
import NG.InputHandling.MouseMoveListener;
import org.joml.Vector2ic;

import static NG.GUIMenu.Frames.SFrameLookAndFeel.UIComponent.BUTTON_ACTIVE;

/**
 * @author Geert van Ieperen. Created on 25-9-2018.
 */
public class SDragEdge extends SComponent implements MouseMoveListener {
    private final SComponent parent;
    private final int width;
    private final int height;

    public SDragEdge(SComponent parent, int width, int height) {
        this.width = width;
        this.height = height;
        this.parent = parent;
        setSize(width, height);
        setGrowthPolicy(false, false);
    }

    @Override
    public int minWidth() {
        return width;
    }

    @Override
    public int minHeight() {
        return height;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        design.draw(BUTTON_ACTIVE, screenPosition, dimensions);
        design.drawText(screenPosition, dimensions, "+", NGFonts.TextType.REGULAR, SFrameLookAndFeel.Alignment.LEFT);
    }

    @Override
    public void mouseMoved(int xDelta, int yDelta) {
        parent.addToSize(xDelta, yDelta);
    }
}
