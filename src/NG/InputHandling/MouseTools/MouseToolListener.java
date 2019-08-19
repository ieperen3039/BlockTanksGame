package NG.InputHandling.MouseTools;

/**
 * @author Geert van Ieperen. Created on 27-11-2018.
 */
public interface MouseToolListener {

    /**
     * checks whether an input click can be handled by this object
     * @param tool the current mouse tool
     * @param xSc  the screen x position of the mouse
     * @param ySc  the screen y position of the mouse
     * @return true iff the click has been handled by this object
     */
    boolean checkMouseClick(MouseTool tool, int xSc, int ySc);

    /**
     * checks whether a scroll action can be handled by this object
     * @param xSc  the screen x position of the mouse
     * @param ySc  the screen y position of the mouse
     * @param value the amount of scrolling
     * @return true iff the scroll has been handled by this object
     */
    default boolean checkMouseScroll(int xSc, int ySc, float value) {
        return false;
    }
}
