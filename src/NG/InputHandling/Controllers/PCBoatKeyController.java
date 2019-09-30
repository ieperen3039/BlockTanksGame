package NG.InputHandling.Controllers;

import NG.Core.Game;
import NG.InputHandling.KeyPressListener;
import NG.InputHandling.KeyReleaseListener;
import NG.InputHandling.MouseToolCallbacks;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static NG.InputHandling.Controllers.PCBoatKeyController.Action.*;
import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Geert van Ieperen created on 31-10-2017.
 */
public class PCBoatKeyController implements BoatControls, KeyPressListener, KeyReleaseListener {
    private final MouseToolCallbacks callbacks;

    enum Action {
        THROTTLE, BREAK,
        STARBOARD, PORT,
        FIRE
    }

    private EnumMap<Action, Integer> keyMapping;
    private Map<Integer, Boolean> keyPresses;

    public PCBoatKeyController(Game game) {
        callbacks = game.get(MouseToolCallbacks.class);
        callbacks.addKeyPressListener(this);
        callbacks.addKeyReleaseListener(this);

        keyMapping = new EnumMap<>(Map.of(
                THROTTLE, GLFW_KEY_W,
                BREAK, GLFW_KEY_S,
                STARBOARD, GLFW_KEY_D,
                PORT, GLFW_KEY_A,
                FIRE, GLFW_MOUSE_BUTTON_LEFT
        ));

        keyPresses = new HashMap<>();
        for (Integer keycode : keyMapping.values()) {
            keyPresses.put(keycode, false);
        }
    }

    @Override
    public void update(float gameTime) {
        // Poll for events at the active window
//        glfwPollEvents();
    }

    @Override
    public float throttle() {
        return getAxisValue(THROTTLE, BREAK);
    }

    @Override
    public float steering() {
        return getAxisValue(STARBOARD, PORT);
    }

    @Override
    public boolean fire() {
        return keyPresses.get(keyMapping.get(FIRE));
    }

    @Override
    public void cleanUp() {
        callbacks.removeListener(this);
    }

    private float getAxisValue(Action upAction, Action downAction) {
        int upValue = getKeyValue(upAction);
        int downValue = getKeyValue(downAction);
        return (upValue - downValue);
    }

    private int getKeyValue(Action action){
        int keyCode = keyMapping.get(action);
        return keyPresses.get(keyCode) ? 1 : 0;
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyPresses.containsKey(keyCode)) {
            keyPresses.put(keyCode, true);
        }
    }

    @Override
    public void keyReleased(int keyCode) {
        if (keyPresses.containsKey(keyCode)) {
            keyPresses.put(keyCode, false);
        }
    }
}

