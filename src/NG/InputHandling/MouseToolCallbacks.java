package NG.InputHandling;

import NG.Core.Game;
import NG.Core.GameAspect;
import NG.DataStructures.Tracked.TrackedFloat;
import NG.GUIMenu.HUDManager;
import NG.InputHandling.MouseTools.DefaultMouseTool;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Rendering.GLFWWindow;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import org.joml.Vector2i;
import org.lwjgl.glfw.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

/**
 * A callback handler specialized on a tycoon-game
 * @author Geert van Ieperen. Created on 18-11-2018.
 */
public class MouseToolCallbacks implements GameAspect, KeyMouseCallbacks {
    private final Collection<KeyPressListener> keyPressListeners = new ArrayList<>();
    private final Collection<KeyReleaseListener> keyReleaseListeners = new ArrayList<>();
    private final Collection<MousePositionListener> mousePositionListeners = new ArrayList<>();

    private DefaultMouseTool DEFAULT_MOUSE_TOOL;

    private final ExecutorService taskScheduler = Executors.newSingleThreadExecutor();

    private Game game;
    private KeyTypeListener keyTypeListener = null;
    private MouseTool currentTool;

    @Override
    public void init(Game game) {
        if (this.game != null) return;
        this.game = game;

        DEFAULT_MOUSE_TOOL = new DefaultMouseTool(game);
        currentTool = DEFAULT_MOUSE_TOOL;

        GLFWWindow target = game.get(GLFWWindow.class);
        Vector2i mousePosition = target.getMousePosition();
        target.setCallbacks(new KeyPressCallback(), new MouseButtonPressCallback(), new MouseMoveCallback(mousePosition), new MouseScrollCallback());
        target.setTextCallback(new CharTypeCallback());
    }

    @Override
    public void cleanup() {
        keyPressListeners.clear();
        keyReleaseListeners.clear();
        mousePositionListeners.clear();
        keyTypeListener = null;
        taskScheduler.shutdown();
    }

    @Override
    public void setMouseTool(MouseTool tool) {
        currentTool = Objects.requireNonNullElse(tool, DEFAULT_MOUSE_TOOL);
        Logger.DEBUG.print("Set mousetool to " + currentTool);
    }

    @Override
    public MouseTool getMouseTool() {
        return currentTool;
    }

    @Override
    public boolean mouseIsOnMap() {
        GLFWWindow window = game.get(GLFWWindow.class);
        Vector2i pos = window.getMousePosition();
        // out of bounds
        if (pos.x < 0 || pos.y < 0 || pos.x > window.getWidth() || pos.y > window.getHeight()) {
            return false;
        }

        List<HUDManager> overlays = game.getAll(HUDManager.class);
        for (HUDManager hud : overlays) {
            if (hud.covers(pos.x, pos.y)) return false;
        }
        return true;
    }

    @Override
    public MouseTool getDefaultMouseTool() {
        return DEFAULT_MOUSE_TOOL;
    }

    @Override
    public void addMousePositionListener(MousePositionListener listener) {
        mousePositionListeners.add(listener);
    }

    @Override
    public void addKeyPressListener(KeyPressListener listener) {
        keyPressListeners.add(listener);
    }

    @Override
    public void addKeyReleaseListener(KeyReleaseListener listener) {
        keyReleaseListeners.add(listener);
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean removeListener(Object listener) {
        boolean mp = mousePositionListeners.remove(listener);
        boolean kp = keyPressListeners.remove(listener);
        boolean kr = keyReleaseListeners.remove(listener);

        return mp || kp || kr;
    }

    /**
     * Sets a listener such that all generated key events are forwarded to this listener.
     * @param listener The new listener, or null to uninstall this listener and allow regular key presses.
     */
    public void setKeyTypeListener(KeyTypeListener listener) {
        keyTypeListener = listener;
    }

    private class KeyPressCallback extends GLFWKeyCallback {
        @Override
        public void invoke(long window, int keyCode, int scanCode, int action, int mods) {
            if (keyCode < 0) return;
            if (action == GLFW_PRESS) {
                keyPressListeners.forEach(l -> execute(() -> l.keyPressed(keyCode)));

            } else if (action == GLFW_RELEASE) {
                keyReleaseListeners.forEach(l -> execute(() -> l.keyReleased(keyCode)));
            }
        }
    }

    private class MouseButtonPressCallback extends GLFWMouseButtonCallback {
        @Override
        public void invoke(long windowHandle, int button, int action, int mods) {
            Vector2i pos = game.get(GLFWWindow.class).getMousePosition();

            if (action == GLFW_PRESS) {
                keyPressListeners.forEach(l -> execute(() -> l.keyPressed(button)));
                execute(() -> currentTool.onClick(button, pos.x, pos.y));

            } else if (action == GLFW_RELEASE) {
                keyReleaseListeners.forEach(l -> execute(() -> l.keyReleased(button)));
                execute(() -> currentTool.onRelease(button, pos.x, pos.y));
            }

        }
    }

    private class MouseMoveCallback extends GLFWCursorPosCallback {
        private TrackedFloat mouseXPos;
        private TrackedFloat mouseYPos;

        MouseMoveCallback(Vector2i mousePos) {
            mouseXPos = new TrackedFloat((float) mousePos.x);
            mouseYPos = new TrackedFloat((float) mousePos.y);
        }

        @Override
        public void invoke(long window, double xpos, double ypos) {
            mouseXPos.update((float) xpos);
            mouseYPos.update((float) ypos);

            for (MousePositionListener listener : mousePositionListeners) {
                listener.mouseMoved((int) xpos, (int) ypos);
            }

            int xDiff = Toolbox.randomToInt(mouseXPos.difference());
            int yDiff = Toolbox.randomToInt(mouseYPos.difference());
            currentTool.mouseMoved(xDiff, yDiff);
        }
    }

    private class MouseScrollCallback extends GLFWScrollCallback {
        @Override
        public void invoke(long window, double xoffset, double yoffset) {
            currentTool.onScroll((float) yoffset);
        }
    }

    private class CharTypeCallback extends GLFWCharCallback {
        @Override
        public void invoke(long window, int codepoint) {
            if (keyTypeListener != null && Character.isAlphabetic(codepoint)) {
                char s = Character.toChars(codepoint)[0];

                execute(() -> keyTypeListener.keyTyped(s));
            }
        }
    }

    private void execute(Runnable action) {
        taskScheduler.submit(() -> {
            try {
                action.run();

            } catch (Throwable ex) {
                // Caught an error while executing an input handler.
                // Look at the second element of the stack trace
                Logger.ERROR.print(ex);
            }
        });
    }
}
