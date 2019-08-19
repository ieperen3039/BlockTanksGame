package NG.GUIMenu.Components;

import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.GUIMenu.HUDManager;
import NG.GUIMenu.NGFonts;
import NG.InputHandling.MouseRelativeClickListener;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static NG.GUIMenu.Frames.SFrameLookAndFeel.UIComponent.DROP_DOWN_HEAD_CLOSED;
import static NG.GUIMenu.Frames.SFrameLookAndFeel.UIComponent.DROP_DOWN_HEAD_OPEN;

/**
 * A menu item that may assume different options, where the player can choose from using a drop-down selection.
 * @author Geert van Ieperen. Created on 5-10-2018.
 */
public class SDropDown extends SComponent implements MouseRelativeClickListener {
    private final String[] values;
    private final DropDownOptions optionPane;
    private final HUDManager gui;
    private List<Consumer<Integer>> stateChangeListeners = new ArrayList<>();

    private int current;
    private boolean isOpened = false;
    private int minHeight;
    private int minWidth;

    private int dropOptionHeight = 50;

    /**
     * create a dropdown menu with the given possible values, with a minimum width of 150 and height of 50
     * @param gui     a reference to the gui in which this is displayed
     * @param initial the initial selected item, such that {@code values[initial]} is shown
     * @param values  a list of possible values for this dropdown menu
     */
    public SDropDown(HUDManager gui, int initial, String... values) {
        this(gui, 150, 50, initial, values);
    }

    /**
     * create a dropdown menu with the given possible values
     * @param gui       a reference to the gui in which this is displayed
     * @param minWidth  the minimum width of the selection bar
     * @param minHeight the minimum height of the selection bar
     * @param initial   the initial selected item, such that {@code values[initial]} is shown
     * @param values    a list of possible values for this dropdown menu
     */
    public SDropDown(HUDManager gui, int minWidth, int minHeight, int initial, String... values) {
        this.values = values;
        this.current = initial;
        this.minHeight = minHeight;
        this.minWidth = minWidth;
        this.optionPane = new DropDownOptions(values);
        this.gui = gui;

        setGrowthPolicy(true, false);
    }

    /**
     * create a dropdown menu with the given possible values, with a minimum width of 150 and height of 50. Initially
     * the first option is selected.
     * @param gui    a reference to the gui in which this is displayed
     * @param values a list of possible values for this dropdown menu
     */
    public SDropDown(HUDManager gui, List<String> values) {
        this(gui, 150, 50, values);
    }

    /**
     * create a dropdown menu with the given possible values
     * @param gui       a reference to the gui in which this is displayed
     * @param minWidth  the minimum width of the selection bar
     * @param minHeight the minimum height of the selection bar
     * @param values    a list of possible values for this dropdown menu
     */
    public SDropDown(HUDManager gui, int minWidth, int minHeight, List<String> values) {
        this(gui, minWidth, minHeight, 0, values.toArray(new String[0]));
    }

    /**
     * create a dropdown menu with the string representation of the given object array as values. To obtain the selected
     * values, one must retrieve the selected index with {@link #getSelectedIndex()} and access the original array.
     * @param gui       a reference to the gui in which this is displayed
     * @param minHeight the minimum height of the selection bar
     * @param minWidth  the minimum width of the selection bar
     * @param initial   the initial selected item, such that {@code values[initial]} is shown
     * @param values    a list of possible values for this dropdown menu
     */
    public <T> SDropDown(HUDManager gui, int minHeight, int minWidth, T initial, List<? extends T> values) {
        this.minHeight = minHeight;
        this.minWidth = minWidth;

        int candidate = 0;
        String[] arr = new String[values.size()];
        for (int i = 0; i < values.size(); i++) {
            T elt = values.get(i);
            arr[i] = elt.toString();

            if (elt.equals(initial)) {
                candidate = i;
            }
        }

        this.current = candidate;
        this.values = arr;
        this.optionPane = new DropDownOptions(arr);
        this.gui = gui;
        setGrowthPolicy(true, false);
    }

    /** @return the index of the currently selected item in the original array */
    public int getSelectedIndex() {
        return current;
    }

    /** @return the currently selected item */
    public String getSelected() {
        return values.length == 0 ? null : values[current];
    }

    /** hints the layout manager the minimum size of this component */
    public void setMinimumSize(int width, int height) {
        minWidth = width;
        minHeight = height;
    }

    /** sets the height of a single option in the drop-down section of the component. */
    public void setDropOptionHeight(int dropOptionHeight) {
        this.dropOptionHeight = dropOptionHeight;
    }

    public void addStateChangeListener(Consumer<Integer> action) {
        stateChangeListeners.add(action);
    }

    @Override
    public void setParent(SComponent parent) {
        super.setParent(parent);
        optionPane.setParent(parent);
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
        design.draw(isOpened ? DROP_DOWN_HEAD_OPEN : DROP_DOWN_HEAD_CLOSED, screenPosition, dimensions);
        design.drawText(screenPosition, dimensions, values[current], NGFonts.TextType.REGULAR, SFrameLookAndFeel.Alignment.LEFT);
        // modal dialogs are drawn separately
    }

    @Override
    public void onClick(int button, int xSc, int ySc) {
        if (isOpened) {
            close();

        } else {
            validateLayout();
            optionPane.setPosition(position.x, position.y + dimensions.y);
            optionPane.setSize(dimensions.x, 0);
            optionPane.setVisible(true);
            gui.setModalListener(optionPane);
        }
    }

    public void setCurrent(int index) {
        current = index;
        stateChangeListeners.forEach(c -> c.accept(current));
    }

    private void close() {
        optionPane.setVisible(false);
    }

    private class DropDownOptions extends SPanel implements MouseRelativeClickListener {

        private DropDownOptions(String[] values) {
            super(1, values.length);
            setVisible(false);

            for (int i = 0; i < values.length; i++) {
                final int index = i;
                SExtendedTextArea option = new SExtendedTextArea(
                        values[index], dropOptionHeight, minWidth, true, NGFonts.TextType.REGULAR, SFrameLookAndFeel.Alignment.LEFT
                );

                option.setClickListener((b, x, y) -> {
                    setCurrent(index);
                    close();
                });

                add(option, new Vector2i(0, i));
            }
        }

        @Override
        public void onClick(int button, int xRel, int yRel) {
            if (xRel < 0 || yRel < 0 || xRel > getWidth() || yRel > getHeight()) {
                close();
                return;
            }

            SComponent target = getComponentAt(xRel, yRel);
            if (target instanceof SExtendedTextArea) {
                SExtendedTextArea option = (SExtendedTextArea) target;
                option.onClick(button, 0, 0);
            }
        }
    }
}
