package NG.GUIMenu.Components;

import NG.GUIMenu.Frames.SFrameLookAndFeel;
import org.joml.Vector2ic;

/**
 * @author Geert van Ieperen. Created on 21-9-2018.
 */
public class SFiller extends SComponent {
    private final int minWidth;
    private final int minHeight;

    public SFiller(int minWidth, int minHeight) {
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        setGrowthPolicy(true, true);
    }

    public SFiller() {
        this(0, 0);
    }

    @Override
    public int minWidth() {
        return minWidth;
    }

    @Override
    public int minHeight() {
        return minHeight;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
    }

    @Override
    public SComponent getComponentAt(int xRel, int yRel) {
        return null;
    }
}
