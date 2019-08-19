package NG.GUIMenu.Components;

import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.GUIMenu.NGFonts;
import NG.InputHandling.MouseRelativeClickListener;
import NG.InputHandling.MouseReleaseListener;
import NG.Tools.Logger;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.Collection;

import static NG.GUIMenu.Frames.SFrameLookAndFeel.UIComponent.BUTTON_ACTIVE;
import static NG.GUIMenu.Frames.SFrameLookAndFeel.UIComponent.BUTTON_PRESSED;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

/**
 * A Button that may execute actions for both left and right clicks upon release.
 * @author Geert van Ieperen. Created on 22-9-2018.
 */
public class SButton extends SComponent implements MouseReleaseListener, MouseRelativeClickListener {
    public static final int BUTTON_MIN_WIDTH = 250;
    public static final int BUTTON_MIN_HEIGHT = 30;

    private Collection<Runnable> leftClickListeners = new ArrayList<>();
    private Collection<Runnable> rightClickListeners = new ArrayList<>();
    private final int minHeight;
    private final int minWidth;

    private String text;
    private boolean isPressed = false;

    /**
     * a button with no associated action (a dead button)
     * @param text the text of the button
     * @see #addLeftClickListener(Runnable)
     */
    public SButton(String text) {
        this(text, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT);
    }

    /**
     * a button with a basic associated action
     * @param text   the text of the button
     * @param action the action that is executed upon (releasing a) left click
     */
    public SButton(String text, Runnable action) {
        this(text, action, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT);
    }

    /**
     * a button with no associated action (a dead button)
     * @param text   the text of the button
     * @param width  the minimal width of this button
     * @param height the minimal height of this button
     * @see #addLeftClickListener(Runnable)
     */
    public SButton(String text, int width, int height) {
        this.minWidth = width;
        this.minHeight = height;
        setText(text);
    }

    /**
     * a button with a basic associated action
     * @param text   the text of the button
     * @param action the action that is executed upon (releasing a) left click
     * @param width  the minimal width of this button
     * @param height the minimal height of this button
     */
    public SButton(String text, Runnable action, int width, int height) {
        this(text, width, height);
        leftClickListeners.add(action);
    }

    /**
     * a button with both a left and a right click action
     * @param text         the text of the button
     * @param onLeftClick  the action that is executed upon (releasing a) left click
     * @param onRightClick the action that is executed upon (releasing a) right click
     * @param width        the minimal width of this button
     * @param height       the minimal height of this button
     */
    public SButton(String text, Runnable onLeftClick, Runnable onRightClick, int width, int height) {
        this(text, onLeftClick, width, height);
        rightClickListeners.add(onRightClick);
    }

    public SButton(String text, Runnable action, BProps properties){
        this(text, action, properties.width, properties.height);
        setGrowthPolicy(properties.doGrowInWidth, properties.doGrowInHeight);
    }

    @Override
    public int minWidth() {
        return minWidth;
    }

    @Override
    public int minHeight() {
        return minHeight;
    }

    public void addLeftClickListener(Runnable action) {
        leftClickListeners.add(action);
    }

    public void addRightClickListeners(Runnable action) {
        rightClickListeners.add(action);
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        design.draw(isPressed ? BUTTON_PRESSED : BUTTON_ACTIVE, screenPosition, dimensions);
        design.drawText(screenPosition, dimensions, text, NGFonts.TextType.REGULAR, SFrameLookAndFeel.Alignment.CENTER);
    }

    @Override
    public void onClick(int button, int xSc, int ySc) {
        isPressed = true;
    }

    @Override
    public void onRelease(int button, int xSc, int ySc) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            leftClickListeners.forEach(Runnable::run);

        } else if (button == GLFW_MOUSE_BUTTON_RIGHT) {
            rightClickListeners.forEach(Runnable::run);

        } else {
            Logger.DEBUG.print("button clicked with " + button + " which has no action");
        }
        isPressed = false;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " (" + getText() + ")";
    }

    public static class BProps {
        public int width;
        public int height;
        public boolean doGrowInWidth;
        public boolean doGrowInHeight;

        public BProps(int width, int height, boolean doGrowInWidth, boolean doGrowInHeight) {
            this.width = width;
            this.height = height;
            this.doGrowInWidth = doGrowInWidth;
            this.doGrowInHeight = doGrowInHeight;
        }
    }
}
