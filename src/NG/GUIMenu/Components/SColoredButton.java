package NG.GUIMenu.Components;

import NG.DataStructures.Generic.Color4f;
import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.InputHandling.MouseRelativeClickListener;
import NG.InputHandling.MouseReleaseListener;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.List;

import static NG.GUIMenu.Frames.SFrameLookAndFeel.UIComponent.BUTTON_ACTIVE;
import static NG.GUIMenu.Frames.SFrameLookAndFeel.UIComponent.BUTTON_PRESSED;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

/**
 * @author Geert van Ieperen created on 18-8-2019.
 */
public class SColoredButton extends SComponent implements MouseReleaseListener, MouseRelativeClickListener {
    private final int width;
    private final int height;
    private Color4f color;
    private List<Runnable> leftClickListeners = new ArrayList<>();
    private boolean isPressed;

    public SColoredButton(int width, int height, Color4f color, Runnable action) {
        this.width = width;
        this.height = height;
        this.color = color;
        leftClickListeners.add(action);
    }

    @Override
    public int minWidth() {
        return width;
    }

    @Override
    public int minHeight() {
        return height;
    }

    public void addLeftClickListener(Runnable action) {
        leftClickListeners.add(action);
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        design.drawColored(isPressed ? BUTTON_PRESSED : BUTTON_ACTIVE, color, screenPosition, dimensions);
    }

    @Override
    public void onClick(int button, int xSc, int ySc) {
        isPressed = true;
    }

    @Override
    public void onRelease(int button, int xSc, int ySc) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            leftClickListeners.forEach(Runnable::run);
        }
        isPressed = false;
    }
}
