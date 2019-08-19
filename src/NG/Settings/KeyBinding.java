package NG.Settings;

import NG.Tools.Logger;

import java.util.HashMap;
import java.util.Map;

import static NG.Settings.KeyNameMapper.NONE;
import static NG.Settings.KeyNameMapper.getKeyName;
import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Geert van Ieperen created on 10-4-2018.
 */
public enum KeyBinding {
    NO_ACTION(NONE),

    CAMERA_UP(GLFW_KEY_UP),
    CAMERA_DOWN(GLFW_KEY_DOWN),
    CAMERA_LEFT(GLFW_KEY_LEFT),
    CAMERA_RIGHT(GLFW_KEY_RIGHT),

    EXIT_GAME(GLFW_KEY_ESCAPE),
    TOGGLE_FULLSCREEN(GLFW_KEY_F11),
    TOGGLE_PAUSE(GLFW_KEY_SPACE),
    PRINT_SCREEN(GLFW_KEY_PRINT_SCREEN),
    DEBUG_SCREEN(GLFW_KEY_F10),

    BLOCK_MOVE_POS_X(GLFW_KEY_W),
    BLOCK_MOVE_POS_Y(GLFW_KEY_A),
    BLOCK_MOVE_POS_Z(GLFW_KEY_SPACE),
    BLOCK_MOVE_NEG_X(GLFW_KEY_S),
    BLOCK_MOVE_NEG_Y(GLFW_KEY_D),
    BLOCK_MOVE_NEG_Z(GLFW_KEY_LEFT_SHIFT),
    BLOCK_MOVE_ROT_LEFT(GLFW_KEY_Q),
    BLOCK_MOVE_ROT_RIGHT(GLFW_KEY_E),
    BLOCK_CONFIRM(GLFW_KEY_ENTER),
    BLOCK_REVERT(GLFW_KEY_BACKSPACE);

    private static final Map<Integer, KeyBinding> map = new HashMap<>();

    private int key;

    KeyBinding(int value) {
        key = value;
    }

    /**
     * @return the KeyBinding associated with the given {@link org.lwjgl.glfw.GLFW GLFW} key, or {@link #NO_ACTION} if
     * the given key is not bound
     */
    public static KeyBinding get(int keyCode) {
        // lazy initialisation
        if (map.isEmpty()) {
            for (KeyBinding binding : values()) {
                int key = binding.key;
                if (key == NONE) continue;

                if (map.containsKey(key)) {
                    Logger.WARN.printf(
                            "Action %s is bound to the same key as %s (%s)",
                            binding, map.get(key), getKeyName(key)
                    );
                }

                map.put(key, binding);
            }
        }

        return map.getOrDefault(keyCode, NO_ACTION);
    }

    public int getKey() {
        return key;
    }

    public String keyName() {
        return getKeyName(key);
    }

    /**
     * sets the binding of this key to the new value
     * @param newKey the new key code
     */
    public void setToKey(int newKey) {
        map.remove(key);
        if (map.containsKey(newKey)) {
            map.get(newKey).key = NONE;
        }

        key = newKey;
        map.put(newKey, this); // also unbinds the existing key
    }
}
