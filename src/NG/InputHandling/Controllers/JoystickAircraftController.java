package NG.InputHandling.Controllers;

import NG.Settings.KeyNameMapper;
import NG.Tools.Logger;

import static NG.Settings.KeyNameMapper.*;
import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Geert van Ieperen created on 10-4-2018.
 */
public class JoystickAircraftController implements AircraftControls {

    private int joystick;
    private final byte[] buttons = new byte[14];
    private final float[] axes = new float[6];

    private int PITCH_UP = XBOX_AXIS_LS_DOWN;
    private int PITCH_DOWN = XBOX_AXIS_LS_UP;
    private int ROLL_RIGHT = XBOX_AXIS_LS_RIGHT;
    private int ROLL_LEFT = XBOX_AXIS_LS_LEFT;
    private int THROTTLE = XBOX_AXIS_LT_UP;
    private int BREAK = XBOX_AXIS_RT_UP;
    private int YAW_LEFT = XBOX_BUTTON_LB;
    private int YAW_RIGHT = XBOX_BUTTON_RB;
    private int PRIMARY_FIRE = XBOX_BUTTON_A;
    private int SECONDARY_FIRE = XBOX_BUTTON_B;

    public JoystickAircraftController() {
        joystick = GLFW_JOYSTICK_1;

        while (joystick <= GLFW_JOYSTICK_LAST && !glfwJoystickPresent(joystick)) joystick++;
        if (joystick > GLFW_JOYSTICK_LAST) {
            Logger.ERROR.print("No controller connected!");
            return;
        }

        update();
    }

    @Override
    public void update() {
        if (joystick > GLFW_JOYSTICK_LAST && !findController()) return;

        glfwGetJoystickButtons(joystick).get(buttons);
        glfwGetJoystickAxes(joystick).get(axes);
    }

    /**
     * searches all input ports for a connected joystick controller
     * @return true iff a controller is found
     */
    private boolean findController() {
        joystick = GLFW_JOYSTICK_1;
        while (joystick <= GLFW_JOYSTICK_LAST && !glfwJoystickPresent(joystick)) joystick++;
        return joystick <= GLFW_JOYSTICK_LAST;
    }

    @Override
    public float throttle() {
        return getAxis(THROTTLE, BREAK);
    }

    @Override
    public float pitch() {
        return getAxis(PITCH_UP, PITCH_DOWN);
    }

    @Override
    public float yaw() {
        return getAxis(YAW_LEFT, YAW_RIGHT);
    }

    @Override
    public float roll() {
        return getAxis(ROLL_LEFT, ROLL_RIGHT);
    }

    @Override
    public boolean primaryFire() {
        return KeyNameMapper.getXBoxValue(PRIMARY_FIRE, axes, buttons) > 0.5f;
    }

    @Override
    public boolean secondaryFire() {
        return KeyNameMapper.getXBoxValue(SECONDARY_FIRE, axes, buttons) > 0.5f;
    }

    private float getAxis(int up, int down) {
        float upVal = getXBoxValue(up, axes, buttons);
        float downVal = getXBoxValue(down, axes, buttons);
        return (upVal + downVal) / 2;
    }
}
