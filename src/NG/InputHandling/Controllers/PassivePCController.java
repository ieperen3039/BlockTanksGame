package NG.InputHandling.Controllers;

import NG.Core.Game;
import NG.InputHandling.KeyPressListener;
import NG.InputHandling.KeyReleaseListener;
import NG.InputHandling.MousePositionListener;
import NG.InputHandling.MouseToolCallbacks;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static NG.InputHandling.Controllers.PassivePCController.Action.*;
import static NG.Settings.KeyNameMapper.*;
import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Geert van Ieperen created on 31-10-2017.
 */
public class PassivePCController
        implements MousePositionListener, AircraftControls, KeyPressListener, KeyReleaseListener {
    private static final float THROTTLE_MODIFIER = 1f;
    private static final float PITCH_MODIFIER = 1f;
    private static final float YAW_MODIFIER = 1f;
    private static final float ROLL_MODIFIER = 1f;

    private final MouseToolCallbacks callbacks;

    protected int mouseX;
    protected int mouseY;

    private int lastUpdateMouseX;
    private int lastUpdateMouseY;

    enum Action {
        PITCH_UP,
        PITCH_DOWN,
        ROLL_RIGHT,
        ROLL_LEFT,
        THROTTLE,
        BREAK,
        YAW_LEFT,
        YAW_RIGHT,
        PRIMARY_FIRE,
        SECONDARY_FIRE
    }

    private EnumMap<Action, Integer> keyMapping;
    private Map<Integer, Boolean> keyPresses;

    protected PassivePCController(Game game) {
        callbacks = game.get(MouseToolCallbacks.class);
        callbacks.addKeyPressListener(this);
        callbacks.addMousePositionListener(this);

        keyMapping = new EnumMap<>(Map.of(
                PITCH_UP, MOUSE_DOWN,
                PITCH_DOWN, MOUSE_UP,
                ROLL_RIGHT, MOUSE_RIGHT,
                ROLL_LEFT, MOUSE_LEFT,
                THROTTLE, GLFW_KEY_W,
                BREAK, GLFW_KEY_S,
                YAW_LEFT, GLFW_KEY_A,
                YAW_RIGHT, GLFW_KEY_D,
                PRIMARY_FIRE, GLFW_MOUSE_BUTTON_LEFT,
                SECONDARY_FIRE, GLFW_MOUSE_BUTTON_RIGHT
        ));

        keyPresses = new HashMap<>();
        for (Integer keycode : keyMapping.values()) {
            keyPresses.put(keycode, false);
        }
    }

    @Override
    public void update() {
        // Poll for events at the active window
        glfwPollEvents();
    }

    @Override
    public float throttle() {
        return getAxisValue(THROTTLE, BREAK, THROTTLE_MODIFIER);
    }

    @Override
    public float pitch() {
        return getAxisValue(PITCH_UP, PITCH_DOWN, PITCH_MODIFIER);
    }

    @Override
    public float yaw() {
        return getAxisValue(YAW_RIGHT, YAW_LEFT, YAW_MODIFIER);
    }

    @Override
    public float roll() {
        return getAxisValue(ROLL_RIGHT, ROLL_LEFT, ROLL_MODIFIER);
    }

    @Override
    public boolean primaryFire() {
        return keyPresses.get(keyMapping.get(PRIMARY_FIRE));
    }

    @Override
    public boolean secondaryFire() {
        return keyPresses.get(keyMapping.get(PRIMARY_FIRE));
    }

    @Override
    public void mouseMoved(int xPos, int yPos) {
        mouseX = xPos;
        mouseY = yPos;
    }


    @Override
    public void cleanUp() {
        callbacks.removeListener(this);
    }

    private float getAxisValue(Action upAction, Action downAction, float modifier) {
        float upValue = getKeyValue(keyMapping.get(upAction));
        float downValue = getKeyValue(keyMapping.get(downAction));
        return modifier * (upValue + downValue) / 2;
    }

    private float getKeyValue(int keyCode){
        switch (keyCode) {
            case MOUSE_DOWN:
                return -getMouseY();

            case MOUSE_UP:
                return getMouseY();

            case MOUSE_RIGHT:
                return getMouseX();

            case MOUSE_LEFT:
                return -getMouseX();

            default:
                return keyPresses.get(keyCode) ? 1 : 0;
        }
    }

    protected float getMouseX() {
        int deltaX = mouseX - lastUpdateMouseX;
        lastUpdateMouseX = mouseX;
        return normalize(deltaX);
    }

    protected float getMouseY() {
        int deltaY = mouseY - lastUpdateMouseY;
        lastUpdateMouseY = mouseY;
        return normalize(deltaY);
    }

    /** returns a sigmoid function of the given float */
    protected float normalize(float val) {
        return (val / (1 + Math.abs(val)));
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

