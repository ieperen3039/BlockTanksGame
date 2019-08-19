package NG.InputHandling;

/**
 * @author Geert van Ieperen. Created on 18-11-2018.
 */
public interface MousePositionListener {
    /**
     * is called whenever the mouse moves
     * @param xPos the current x coordinate of the mouse
     * @param yPos the current y coordinate of the mouse
     */
    void mouseMoved(int xPos, int yPos);
}
