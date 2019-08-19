package NG.GUIMenu.Components;

import NG.GUIMenu.Frames.SFrameLookAndFeel;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.function.Supplier;

import static NG.GUIMenu.Frames.SFrameLookAndFeel.UIComponent.PANEL;
import static NG.GUIMenu.Frames.SFrameLookAndFeel.UIComponent.SELECTION;

/**
 * @author Geert van Ieperen. Created on 28-9-2018.
 */
public class SProgressBar extends SComponent {
    private int minWidth;
    private int minHeight;
    private final Supplier<Float> progress;

    public SProgressBar(int minWidth, int minHeight, Supplier<Float> progressSource) {
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.progress = progressSource;
        setGrowthPolicy(false, false);
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
        design.draw(PANEL, screenPosition, dimensions);
        Vector2i bar = new Vector2i(dimensions.x, (int) (dimensions.y * progress.get()));
        design.draw(SELECTION, screenPosition, bar);
    }
}
