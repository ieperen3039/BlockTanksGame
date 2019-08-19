package NG.GUIMenu.Frames;

import NG.GUIMenu.Components.SComponent;
import NG.GUIMenu.Components.SFrame;
import NG.GUIMenu.Components.SToolBar;
import NG.GUIMenu.GUIPainter;
import NG.GUIMenu.HUDManager;

/**
 * A class that manages frames of a game. New {@link SFrame} objects can be added using {@link #addFrame(SFrame)}
 * @author Geert van Ieperen. Created on 29-9-2018.
 */
public interface FrameGUIManager extends HUDManager {

    /**
     * draws every frome, starting from the last to most previously focused.
     * @param painter the object for painting
     * @see HUDManager#draw(GUIPainter)
     */
    void draw(GUIPainter painter);

    /**
     * adds the given frame at a position that the frame manager assumes to be optimal
     * @see #addFrame(SFrame, int, int)
     */
    void addFrame(SFrame frame);

    /**
     * adds a fame on the given position, and focusses it.
     * @param frame the frame to be added.
     * @param x     screen x coordinate in pixels from left
     * @param y     screen y coordinate in pixels from top
     */
    void addFrame(SFrame frame, int x, int y);

    /**
     * brings the given from to the front-most position
     * @param frame a frame that has been added to this manager
     * @throws java.util.NoSuchElementException if the given frame has not been added or has been disposed.
     */
    void focus(SFrame frame);

    /**
     * sets the toolbar of the screen to the given object. Overwrites the current setting.
     * @param toolBar any toolbar, or null to remove the toolbar
     */
    void setToolBar(SToolBar toolBar);

    /** @return the toolbar set with {@link #setToolBar(SToolBar)}, or null if none has been set */
    SToolBar getToolBar();

    @Override
    default void addElement(SComponent component) {
        if (!(component instanceof SFrame)) {
            component = new SFrame(component.toString(), component);
        }

        addFrame((SFrame) component);
    }
}
