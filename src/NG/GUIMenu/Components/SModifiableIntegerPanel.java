package NG.GUIMenu.Components;

import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.GUIMenu.NGFonts;
import org.joml.Vector2i;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen created on 7-2-2019.
 */
public class SModifiableIntegerPanel extends SPanel {
    private static final int ADD_BUTTON_HEIGHT = 50;
    private static final int ADD_BUTTON_WIDTH = 80;
    private static final int VALUE_SIZE = 150;
    private final Consumer<Integer> onUpdate;
    private final STextArea valueDisplay;
    private final int upperBound;
    private final int lowerBound;

    private int value;

    public SModifiableIntegerPanel(Consumer<Integer> onUpdate, String name, int initialValue) {
        this(onUpdate, name, Integer.MIN_VALUE, Integer.MAX_VALUE, initialValue);
    }

    public SModifiableIntegerPanel(
            Consumer<Integer> onUpdate, String name, int lowerBound, int upperBound, int initialValue
    ) {
        super(8, 1);
        this.onUpdate = onUpdate;
        this.value = initialValue;
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;

        this.valueDisplay = new STextArea(String.valueOf(initialValue), ADD_BUTTON_HEIGHT, VALUE_SIZE, false, NGFonts.TextType.REGULAR, SFrameLookAndFeel.Alignment.LEFT);

        if (!name.isEmpty()) add(new STextArea(name, 0), new Vector2i(0, 0));

        add(new SButton("-100", () -> addToValue(-100), ADD_BUTTON_WIDTH, ADD_BUTTON_HEIGHT), new Vector2i(1, 0));
        add(new SButton("-10", () -> addToValue(-10), ADD_BUTTON_WIDTH, ADD_BUTTON_HEIGHT), new Vector2i(2, 0));
        add(new SButton("-1", () -> addToValue(-1), ADD_BUTTON_WIDTH, ADD_BUTTON_HEIGHT), new Vector2i(3, 0));
        add(valueDisplay, new Vector2i(4, 0));
        add(new SButton("+1", () -> addToValue(1), ADD_BUTTON_WIDTH, ADD_BUTTON_HEIGHT), new Vector2i(5, 0));
        add(new SButton("+10", () -> addToValue(10), ADD_BUTTON_WIDTH, ADD_BUTTON_HEIGHT), new Vector2i(6, 0));
        add(new SButton("+100", () -> addToValue(100), ADD_BUTTON_WIDTH, ADD_BUTTON_HEIGHT), new Vector2i(7, 0));
    }

    private void addToValue(Integer i) {
        value = Math.min(Math.max(value + i, lowerBound), upperBound);
        valueDisplay.setText(String.valueOf(value));
        onUpdate.accept(value);
    }
}
