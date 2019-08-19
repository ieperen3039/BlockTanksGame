package NG.InputHandling.MouseTools;

import NG.Camera.Camera;
import NG.CollisionDetection.GameState;
import NG.Core.Game;
import NG.Entities.Entity;
import NG.GUIMenu.Components.SComponent;
import NG.GUIMenu.HUDManager;
import NG.GameMap.GameMap;
import NG.InputHandling.MouseMoveListener;
import NG.InputHandling.MouseRelativeClickListener;
import NG.InputHandling.MouseReleaseListener;
import NG.Rendering.GLFWWindow;
import NG.Tools.Logger;
import NG.Tools.Vectors;
import org.joml.Vector2i;
import org.joml.Vector3fc;
import org.lwjgl.glfw.GLFW;

/**
 * A mouse tool that implements the standard behaviour of the pointer user input.
 *
 * <dl>
 * <dt>UI elements:</dt>
 * <dd>components are activated when clicked on, drag and release listeners are respected</dd>
 * <dt>Entities:</dt>
 * <dd>The entity gets selected</dd>
 * <dt>Map:</dt>
 * <dd>If an entity is selected, open an action menu</dd>
 * </dl>
 * @author Geert van Ieperen. Created on 26-11-2018.
 */
public class DefaultMouseTool implements MouseTool {
    private int dragButton = 0;
    private MouseMoveListener dragListener = null;
    private MouseReleaseListener releaseListener = null;
    private int button;

    private Entity selectedEntity = null;

    protected Game game;

    public DefaultMouseTool(Game game) {
        this.game = game;
    }

    @Override
    public void apply(SComponent component, int xSc, int ySc) {
        Logger.DEBUG.print("Clicked on " + component);

        if (component instanceof MouseRelativeClickListener) {
            MouseRelativeClickListener cl = (MouseRelativeClickListener) component;
            // by def. of MouseRelativeClickListener, give relative coordinates
            Vector2i pos = component.getScreenPosition();
            cl.onClick(button, xSc - pos.x, ySc - pos.y);
        }

        if (component instanceof MouseMoveListener) {
            dragListener = (MouseMoveListener) component;
            dragButton = button;

        } else {
            dragListener = null;
        }

        if (component instanceof MouseReleaseListener) {
            releaseListener = (MouseReleaseListener) component;

        } else {
            releaseListener = null;
        }
    }

    @Override
    public void apply(Entity entity, int xSc, int ySc) {
        Logger.DEBUG.print("Clicked on " + entity);

        selectedEntity = entity;
    }

    @Override
    public void apply(Vector3fc position, int xSc, int ySc) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            Logger.DEBUG.print("Right-clicked at " + Vectors.toString(position));
            selectedEntity = null;

        } else {
            if (selectedEntity != null) {
                Logger.DEBUG.print("Clicked at " + Vectors.toString(position) + " with " + selectedEntity + " selected");

            } else {
                Logger.DEBUG.print("Clicked at " + Vectors.toString(position));
            }
        }
    }

    @Override
    public void onRelease(int button, int xSc, int ySc) {
        if (button != dragButton) return;

        dragListener = null;
        if (releaseListener != null) {
            releaseListener.onRelease(button, xSc, ySc);
            releaseListener = null;
        }
    }

    @Override
    public void mouseMoved(int xDelta, int yDelta) {
        if (dragListener == null) return;
        dragListener.mouseMoved(xDelta, yDelta);
    }

    @Override
    public String toString() {
        return "Default MouseTool";
    }

    @Override
    public void onClick(int button, int x, int y) {
        setButton(button);

        if (game.has(HUDManager.class) && game.get(HUDManager.class).checkMouseClick(this, x, y)) return;
        if (game.has(GameState.class) && game.get(GameState.class).checkMouseClick(this, x, y)) return;
        if (game.has(GameMap.class)) game.get(GameMap.class).checkMouseClick(this, x, y);
    }

    protected void setButton(int button) {
        this.button = button;
    }

    @Override
    public void onScroll(float value) {
        Vector2i pos = game.get(GLFWWindow.class).getMousePosition();
        HUDManager gui = game.get(HUDManager.class);
        if (gui.checkMouseScroll(pos.x, pos.y, value)) return;

        game.get(Camera.class).onScroll(value);
    }
}
