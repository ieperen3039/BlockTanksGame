package NG.InputHandling.MouseTools;

import NG.Entities.Entity;
import NG.GUIMenu.Components.SComponent;
import NG.InputHandling.MouseMoveListener;
import NG.InputHandling.MouseRelativeClickListener;
import NG.InputHandling.MouseReleaseListener;
import NG.InputHandling.MouseScrollListener;
import org.joml.Vector3fc;

/**
 * Determines the behaviour of clicking
 * @author Geert van Ieperen. Created on 22-11-2018.
 */
public interface MouseTool
        extends MouseMoveListener, MouseReleaseListener, MouseRelativeClickListener, MouseScrollListener {

    /**
     * applies the functionality of this tool to the given component
     * @param component a component where has been clicked on
     * @param xSc       screen x position of the mouse in pixels from left
     * @param ySc       screen y position of the mouse in pixels from top
     */
    void apply(SComponent component, int xSc, int ySc);

    /**
     * applies the functionality of this tool to the given entity
     * @param entity an entity that is clicked on using this tool, always not null
     * @param xSc       screen x position of the mouse in pixels from left
     * @param ySc       screen y position of the mouse in pixels from top
     */
    void apply(Entity entity, int xSc, int ySc);

    /**
     * applies the functionality of this tool to the given position in the world
     * @param position a position in the world where is clicked.
     * @param xSc       screen x position of the mouse in pixels from left
     * @param ySc       screen y position of the mouse in pixels from top
     */
    void apply(Vector3fc position, int xSc, int ySc);
}
