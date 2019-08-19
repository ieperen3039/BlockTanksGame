package NG.InputHandling;

import NG.InputHandling.MouseTools.MouseTool;

/**
 * A class that allows binding callbacks of key and mouse listeners.
 * @author Geert van Ieperen. Created on 18-11-2018.
 */
public interface KeyMouseCallbacks {
    /** @return the mouse tool obtained from {@code setMouseTool(null); getMouseTool()} */
    MouseTool getDefaultMouseTool();

    /**
     * @param listener when the mouse moves, the {@link MousePositionListener#mouseMoved(int, int)} method is called
     */
    void addMousePositionListener(MousePositionListener listener);

    /**
     * notifies on key press, also mouse presses
     * @param listener when a key is pressed, receives the {@link org.lwjgl.glfw.GLFW} key that is pressed
     */
    void addKeyPressListener(KeyPressListener listener);

    /** @see #addKeyPressListener(KeyPressListener) */
    void addKeyReleaseListener(KeyReleaseListener listener);

    /** notifies on mouse press
     * @see #addKeyPressListener(KeyPressListener)  */
    default void addMouseButtonListener(KeyPressListener listener){
        addKeyPressListener(listener);
    }

    /**
     * Try to remove the given listener from all of the listener types, except for mouse move listeners. Even if the
     * given listener is of multiple types, all of them are removed.
     * @param listener a previously installed listener
     * @return true iff any listener has been removed
     */
    boolean removeListener(Object listener);

    /**
     * sets the mouse tool to the new value, overwriting the previous tool.
     * @param tool any mouse tool, or null to reset the tool to the default
     */
    void setMouseTool(MouseTool tool);

    /**
     * @return the last selected mouse tool, or null when the default tool is selected.
     */
    MouseTool getMouseTool();

    /**
     * @return true if the mouse is within the bounds of the window and not on a UI element
     */
    boolean mouseIsOnMap();
}
