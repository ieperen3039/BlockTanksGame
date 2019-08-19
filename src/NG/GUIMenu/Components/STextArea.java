package NG.GUIMenu.Components;

import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.GUIMenu.NGFonts;
import org.joml.Vector2ic;

/**
 * @author Geert van Ieperen. Created on 22-9-2018.
 */
public class STextArea extends SComponent {
    public static final int LETTER_WIDTH = 18;
    protected final NGFonts.TextType textType;
    protected final SFrameLookAndFeel.Alignment alignment;
    protected String text;

    private int height;
    private int specMinWidth;

    public STextArea(
            String text, int minHeight, int minWidth, boolean doGrowInWidth, NGFonts.TextType textType,
            SFrameLookAndFeel.Alignment alignment
    ) {
        this.text = text;
        this.height = minHeight;
        this.specMinWidth = minWidth;
        this.textType = textType;
        setGrowthPolicy(doGrowInWidth, false);
        this.alignment = alignment;
    }

    public STextArea(String text, int minHeight) {
        this(text, minHeight, 0, true, NGFonts.TextType.REGULAR, SFrameLookAndFeel.Alignment.LEFT);
    }

    @Override
    public int minWidth() {
        return Math.max(getText().length() * LETTER_WIDTH, specMinWidth);
    }

    @Override
    public int minHeight() {
        return height;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        design.drawText(screenPosition, dimensions, getText(), textType, alignment);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        String text = getText();
        String substring = text.length() > 30 ? text.substring(0, 20) + "..." : text;
        return this.getClass().getSimpleName() + " (" + substring + ")";
    }
}
