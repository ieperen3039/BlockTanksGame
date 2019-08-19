package NG.GUIMenu.Components;

import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.GUIMenu.NGFonts;
import NG.InputHandling.MouseRelativeClickListener;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static NG.GUIMenu.Frames.SFrameLookAndFeel.UIComponent.BUTTON_ACTIVE;
import static NG.GUIMenu.Frames.SFrameLookAndFeel.UIComponent.BUTTON_PRESSED;

/**
 * A button with a state that only changes upon clicking the button
 * @author Geert van Ieperen. Created on 22-9-2018.
 */
public class SToggleButton extends SComponent implements MouseRelativeClickListener {
    private final int minHeight;
    private final int minWidth;
    private String text;

    private boolean state;
    private List<Runnable> stateChangeListeners = new ArrayList<>();

    /**
     * Create a button with the given properties, starting disabled
     * @param text         the displayed text
     */
    public SToggleButton(String text) {
        this(text, SButton.BUTTON_MIN_WIDTH, SButton.BUTTON_MIN_HEIGHT);
    }

    /**
     * Create a button with the given properties
     * @param text         the displayed text
     * @param minWidth     the minimal width of this button, which {@link NG.GUIMenu.LayoutManagers.SLayoutManager}s
     *                     should respect
     * @param minHeight    the minimal height of this button.
     * @param initialState the initial state of the button. If true, the button will be enabled
     */
    public SToggleButton(String text, int minWidth, int minHeight, boolean initialState) {
        this.minHeight = minHeight;
        this.minWidth = minWidth;
        this.text = text;
        this.state = initialState;
    }

    /**
     * Create a button with the given properties, starting disabled
     * @param text      the displayed text
     * @param minWidth  the minimal width of this buttion, which {@link NG.GUIMenu.LayoutManagers.SLayoutManager}s
     *                  should respect
     * @param minHeight the minimal height of this button.
     */
    public SToggleButton(String text, int minWidth, int minHeight) {
        this(text, minWidth, minHeight, false);
    }

    /**
     * Create a button with the given properties, starting disabled and with the given listener
     * @param text                the displayed text
     * @param stateChangeListener upon change, this action is activated with the current state as argument
     */
    public SToggleButton(String text, Consumer<Boolean> stateChangeListener) {
        this(text, SButton.BUTTON_MIN_WIDTH, SButton.BUTTON_MIN_HEIGHT, stateChangeListener);
    }

    /**
     * Create a button with the given properties, starting disabled and with the given listener
     * @param text                the displayed text
     * @param minWidth            the minimal width of this buttion, which {@link NG.GUIMenu.LayoutManagers.SLayoutManager}s
     *                            should respect
     * @param minHeight           the minimal height of this button.
     * @param stateChangeListener upon change, this action is activated with the current state as argument
     */
    public SToggleButton(String text, int minWidth, int minHeight, Consumer<Boolean> stateChangeListener) {
        this(text, minWidth, minHeight);
        addStateChangeListener(() -> stateChangeListener.accept(state));
    }

    @Override
    public int minWidth() {
        return minWidth;
    }

    @Override
    public int minHeight() {
        return minHeight;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        if (dimensions.x == 0 || dimensions.y == 0) return;
        design.draw(state ? BUTTON_PRESSED : BUTTON_ACTIVE, screenPosition, dimensions);
        design.drawText(screenPosition, dimensions, text, NGFonts.TextType.REGULAR, SFrameLookAndFeel.Alignment.CENTER);
    }

    @Override
    public void onClick(int button, int xSc, int ySc) {
        setState(!state);
    }

    /**
     * @param action Upon change, this action is activated
     */
    public void addStateChangeListener(Runnable action) {
        stateChangeListeners.add(action);
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
        stateChangeListeners.forEach(Runnable::run);
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
}
