package NG.GUIMenu.Components;

import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.GUIMenu.LayoutManagers.SingleElementLayout;
import org.joml.Vector2ic;

/**
 * an area with fixed minimum size that can show components or be hidden.
 * Components are stretched to fit the designated area.
 * If the minimum size of the component is too large for this area, an assertion is thrown.
 * @author Geert van Ieperen created on 12-7-2019.
 */
public class SComponentArea extends SContainer {
    private static final SFiller FILLER = new SFiller();
    private int width;
    private int height;
    private boolean hidden = true;

    public SComponentArea(int width, int height) {
        super(new SingleElementLayout());
        this.width = width;
        this.height = height;
    }

    /**
     * removes the current component, and sets this component's visibility to false
     */
    public void hide(){
        add(FILLER, null);
        hidden = true;
    }

    public void show(SComponent element){
        validateLayout();
        assert element.minWidth() < getWidth() : getWidth();
        assert element.minHeight() < getHeight() : getHeight();

        add(element, null);
        hidden = false;
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
        if (hidden) return;
        drawChildren(design, screenPosition);
    }
}
