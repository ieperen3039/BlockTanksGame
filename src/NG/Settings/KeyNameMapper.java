package NG.Settings;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Copyright (c) Camilla Löwy <elmindreda@glfw.org>. From github.com/glfw/glfw/blob/master/tests/events.c#L64
 */
@SuppressWarnings("Duplicates")
public final class KeyNameMapper {
    public static final int NONE = -1;

    /** axis indices, do not use for key-binding */
    private static final int XBOX_AXISIND_LS_HOR = 0;
    private static final int XBOX_AXISIND_LS_VERT = 1;
    private static final int XBOX_AXISIND_RS_HOR = 2;
    private static final int XBOX_AXISIND_RS_VERT = 3;
    private static final int XBOX_AXISIND_LT = 4;
    private static final int XBOX_AXISIND_RT = 5;

    // values such that (value % 6) == index
    public static final int XBOX_AXIS_LS_UP = 6;
    public static final int XBOX_AXIS_LS_DOWN = 12;
    public static final int XBOX_AXIS_LS_LEFT = 7;
    public static final int XBOX_AXIS_LS_RIGHT = 13;
    public static final int XBOX_AXIS_RS_UP = 8;
    public static final int XBOX_AXIS_RS_DOWN = 14;
    public static final int XBOX_AXIS_RS_LEFT = 9;
    public static final int XBOX_AXIS_RS_RIGHT = 15;
    public static final int XBOX_AXIS_LT_UP = 10;
    public static final int XBOX_AXIS_LT_DOWN = 16;
    public static final int XBOX_AXIS_RT_UP = 11;
    public static final int XBOX_AXIS_RT_DOWN = 17;

    // values such that (value - 100) == index
    public static final int XBOX_BUTTON_A = 100;
    public static final int XBOX_BUTTON_B = 101;
    public static final int XBOX_BUTTON_X = 102;
    public static final int XBOX_BUTTON_Y = 103;
    public static final int XBOX_BUTTON_LB = 104;
    public static final int XBOX_BUTTON_RB = 105;
    public static final int XBOX_BUTTON_BACK = 106;
    public static final int XBOX_BUTTON_START = 107;
    public static final int XBOX_BUTTON_LS = 108;
    public static final int XBOX_BUTTON_RS = 109;
    public static final int XBOX_BUTTON_UP = 110;
    public static final int XBOX_BUTTON_RIGHT = 111;
    public static final int XBOX_BUTTON_DOWN = 112;
    public static final int XBOX_BUTTON_LEFT = 113;

    public static final int MOUSE_UP = GLFW_MOUSE_BUTTON_LAST + 1;
    public static final int MOUSE_DOWN = MOUSE_UP + 1;
    public static final int MOUSE_LEFT = MOUSE_DOWN + 1;
    public static final int MOUSE_RIGHT = MOUSE_LEFT + 1;

    /** @return false iff the given code refers to a button */
    public static boolean isButton(int keyCode) {
        switch (keyCode) {
            case MOUSE_UP:
            case MOUSE_DOWN:
            case MOUSE_LEFT:
            case MOUSE_RIGHT:
                return false;
            default:
                return true;
        }
    }

    /**
     * @param code    the keycode to query
     * @param axes    the values for the xbox controller axes
     * @param buttons the values for the controller buttons
     * @return if (code) represents a button, return 0 if it is not pressed or 1 if it is pressed. If (code) represents
     * an axis, return 1 if the axis is pushed, and -1 if it is pushed the other direction.
     */
    public static float getXBoxValue(int code, float[] axes, byte[] buttons) {
        if (code == NONE) return 0;
        if (code >= 100) {
            return buttons[code - 100];

        } else {
            float value = axes[code % 6]; // (value % 6) == index
            return Math.max(0, code < 12 ? value : -value); // values >= 12 are toward negative
        }
    }

    public static String getKeyName(int key) {
        switch (key) {
            // Printable keys
            case GLFW_KEY_A:
                return "A";
            case GLFW_KEY_B:
                return "B";
            case GLFW_KEY_C:
                return "C";
            case GLFW_KEY_D:
                return "D";
            case GLFW_KEY_E:
                return "E";
            case GLFW_KEY_F:
                return "F";
            case GLFW_KEY_G:
                return "G";
            case GLFW_KEY_H:
                return "H";
            case GLFW_KEY_I:
                return "I";
            case GLFW_KEY_J:
                return "J";
            case GLFW_KEY_K:
                return "K";
            case GLFW_KEY_L:
                return "L";
            case GLFW_KEY_M:
                return "M";
            case GLFW_KEY_N:
                return "N";
            case GLFW_KEY_O:
                return "O";
            case GLFW_KEY_P:
                return "P";
            case GLFW_KEY_Q:
                return "Q";
            case GLFW_KEY_R:
                return "R";
            case GLFW_KEY_S:
                return "S";
            case GLFW_KEY_T:
                return "T";
            case GLFW_KEY_U:
                return "U";
            case GLFW_KEY_V:
                return "V";
            case GLFW_KEY_W:
                return "W";
            case GLFW_KEY_X:
                return "X";
            case GLFW_KEY_Y:
                return "Y";
            case GLFW_KEY_Z:
                return "Z";
            case GLFW_KEY_1:
                return "1";
            case GLFW_KEY_2:
                return "2";
            case GLFW_KEY_3:
                return "3";
            case GLFW_KEY_4:
                return "4";
            case GLFW_KEY_5:
                return "5";
            case GLFW_KEY_6:
                return "6";
            case GLFW_KEY_7:
                return "7";
            case GLFW_KEY_8:
                return "8";
            case GLFW_KEY_9:
                return "9";
            case GLFW_KEY_0:
                return "0";
            case GLFW_KEY_SPACE:
                return "SPACE";
            case GLFW_KEY_MINUS:
                return "MINUS";
            case GLFW_KEY_EQUAL:
                return "EQUAL";
            case GLFW_KEY_LEFT_BRACKET:
                return "LEFT BRACKET";
            case GLFW_KEY_RIGHT_BRACKET:
                return "RIGHT BRACKET";
            case GLFW_KEY_BACKSLASH:
                return "BACKSLASH";
            case GLFW_KEY_SEMICOLON:
                return "SEMICOLON";
            case GLFW_KEY_APOSTROPHE:
                return "APOSTROPHE";
            case GLFW_KEY_GRAVE_ACCENT:
                return "GRAVE ACCENT";
            case GLFW_KEY_COMMA:
                return "COMMA";
            case GLFW_KEY_PERIOD:
                return "PERIOD";
            case GLFW_KEY_SLASH:
                return "SLASH";
            case GLFW_KEY_WORLD_1:
                return "WORLD 1";
            case GLFW_KEY_WORLD_2:
                return "WORLD 2";

            // Function keys
            case GLFW_KEY_ESCAPE:
                return "ESCAPE";
            case GLFW_KEY_F1:
                return "F1";
            case GLFW_KEY_F2:
                return "F2";
            case GLFW_KEY_F3:
                return "F3";
            case GLFW_KEY_F4:
                return "F4";
            case GLFW_KEY_F5:
                return "F5";
            case GLFW_KEY_F6:
                return "F6";
            case GLFW_KEY_F7:
                return "F7";
            case GLFW_KEY_F8:
                return "F8";
            case GLFW_KEY_F9:
                return "F9";
            case GLFW_KEY_F10:
                return "F10";
            case GLFW_KEY_F11:
                return "F11";
            case GLFW_KEY_F12:
                return "F12";
            case GLFW_KEY_F13:
                return "F13";
            case GLFW_KEY_F14:
                return "F14";
            case GLFW_KEY_F15:
                return "F15";
            case GLFW_KEY_F16:
                return "F16";
            case GLFW_KEY_F17:
                return "F17";
            case GLFW_KEY_F18:
                return "F18";
            case GLFW_KEY_F19:
                return "F19";
            case GLFW_KEY_F20:
                return "F20";
            case GLFW_KEY_F21:
                return "F21";
            case GLFW_KEY_F22:
                return "F22";
            case GLFW_KEY_F23:
                return "F23";
            case GLFW_KEY_F24:
                return "F24";
            case GLFW_KEY_F25:
                return "F25";
            case GLFW_KEY_UP:
                return "UP";
            case GLFW_KEY_DOWN:
                return "DOWN";
            case GLFW_KEY_LEFT:
                return "LEFT";
            case GLFW_KEY_RIGHT:
                return "RIGHT";
            case GLFW_KEY_LEFT_SHIFT:
                return "LEFT SHIFT";
            case GLFW_KEY_RIGHT_SHIFT:
                return "RIGHT SHIFT";
            case GLFW_KEY_LEFT_CONTROL:
                return "LEFT CONTROL";
            case GLFW_KEY_RIGHT_CONTROL:
                return "RIGHT CONTROL";
            case GLFW_KEY_LEFT_ALT:
                return "LEFT ALT";
            case GLFW_KEY_RIGHT_ALT:
                return "RIGHT ALT";
            case GLFW_KEY_TAB:
                return "TAB";
            case GLFW_KEY_ENTER:
                return "ENTER";
            case GLFW_KEY_BACKSPACE:
                return "BACKSPACE";
            case GLFW_KEY_INSERT:
                return "INSERT";
            case GLFW_KEY_DELETE:
                return "DELETE";
            case GLFW_KEY_PAGE_UP:
                return "PAGE UP";
            case GLFW_KEY_PAGE_DOWN:
                return "PAGE DOWN";
            case GLFW_KEY_HOME:
                return "HOME";
            case GLFW_KEY_END:
                return "END";
            case GLFW_KEY_KP_0:
                return "KEYPAD 0";
            case GLFW_KEY_KP_1:
                return "KEYPAD 1";
            case GLFW_KEY_KP_2:
                return "KEYPAD 2";
            case GLFW_KEY_KP_3:
                return "KEYPAD 3";
            case GLFW_KEY_KP_4:
                return "KEYPAD 4";
            case GLFW_KEY_KP_5:
                return "KEYPAD 5";
            case GLFW_KEY_KP_6:
                return "KEYPAD 6";
            case GLFW_KEY_KP_7:
                return "KEYPAD 7";
            case GLFW_KEY_KP_8:
                return "KEYPAD 8";
            case GLFW_KEY_KP_9:
                return "KEYPAD 9";
            case GLFW_KEY_KP_DIVIDE:
                return "KEYPAD DIVIDE";
            case GLFW_KEY_KP_MULTIPLY:
                return "KEYPAD MULTPLY";
            case GLFW_KEY_KP_SUBTRACT:
                return "KEYPAD SUBTRACT";
            case GLFW_KEY_KP_ADD:
                return "KEYPAD ADD";
            case GLFW_KEY_KP_DECIMAL:
                return "KEYPAD DECIMAL";
            case GLFW_KEY_KP_EQUAL:
                return "KEYPAD EQUAL";
            case GLFW_KEY_KP_ENTER:
                return "KEYPAD ENTER";
            case GLFW_KEY_PRINT_SCREEN:
                return "PRINT SCREEN";
            case GLFW_KEY_NUM_LOCK:
                return "NUM LOCK";
            case GLFW_KEY_CAPS_LOCK:
                return "CAPS LOCK";
            case GLFW_KEY_SCROLL_LOCK:
                return "SCROLL LOCK";
            case GLFW_KEY_PAUSE:
                return "PAUSE";
            case GLFW_KEY_LEFT_SUPER:
                return "LEFT SUPER";
            case GLFW_KEY_RIGHT_SUPER:
                return "RIGHT SUPER";
            case GLFW_KEY_MENU:
                return "MENU";

            // mouse buttons
            case GLFW_MOUSE_BUTTON_LEFT:
                return "MOUSE LEFT";
            case GLFW_MOUSE_BUTTON_RIGHT:
                return "MOUSE RIGHT";
            case GLFW_MOUSE_BUTTON_MIDDLE:
                return "MOUSE MIDDLE";
            case GLFW_MOUSE_BUTTON_4:
                return "MOUSE 4";
            case GLFW_MOUSE_BUTTON_5:
                return "MOUSE 5";
            case GLFW_MOUSE_BUTTON_6:
                return "MOUSE 6";
            case GLFW_MOUSE_BUTTON_7:
                return "MOUSE 7";
            case GLFW_MOUSE_BUTTON_8:
                return "MOUSE 8";
            case MOUSE_UP:
                return "MOUSE_UP";
            case MOUSE_DOWN:
                return "MOUSE_DOWN";
            case MOUSE_LEFT:
                return "MOUSE_LEFT";
            case MOUSE_RIGHT:
                return "MOUSE_RIGHT";

            default:
                return "UNKNOWN_KEY";
        }
    }

    public String getJoystickName(int key) {
        switch (key) {
            case 0:
                return "XBOX_BUTTON_A";
            case 1:
                return "XBOX_BUTTON_B";
            case 2:
                return "XBOX_BUTTON_X";
            case 3:
                return "XBOX_BUTTON_Y";
            case 4:
                return "XBOX_BUTTON_LB";
            case 5:
                return "XBOX_BUTTON_RB";
            case 6:
                return "XBOX_BUTTON_BACK";
            case 7:
                return "XBOX_BUTTON_START";
            case 8:
                return "XBOX_BUTTON_LS";
            case 9:
                return "XBOX_BUTTON_RS";
            case 10:
                return "XBOX_BUTTON_UP";
            case 11:
                return "XBOX_BUTTON_RIGHT";
            case 12:
                return "XBOX_BUTTON_DOWN";
            case 13:
                return "XBOX_BUTTON_LEFT";
            default:
                return "UNKNOWN_BUTTON";
        }
    }
}
