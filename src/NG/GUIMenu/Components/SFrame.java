package NG.GUIMenu.Components;

import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.GUIMenu.LayoutManagers.SingleElementLayout;
import NG.GUIMenu.NGFonts;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import static NG.GUIMenu.Frames.SFrameLookAndFeel.UIComponent.PANEL;

/**
 * A Frame object similar to {@link javax.swing.JFrame} objects. The {@link #setMainPanel(SComponent)} can be used to
 * control over the contents of the SFrame.
 * @author Geert van Ieperen. Created on 20-9-2018.
 */
public class SFrame extends SContainer {
    public static final int INNER_BORDER = 4;
    public static final int FRAME_TITLE_BAR_SIZE = 50;

    private final String title;
    private SContainer upperBar;
    private boolean isDisposed = false;
    private STextArea titleComponent;

    /**
     * Creates a SFrame with the given title, width and height
     * @param title the title of the new frame
     * @see SPanel
     */
    public SFrame(String title, int width, int height) {
        this(title, width, height, true);
    }

    /**
     * Creates a SFrame with the given title, width and height. Moving, minimizing and closing by the user is disabled
     * for this frame.
     * @param title the title of the new frame
     * @see SPanel
     */
    public SFrame(String title, int width, int height, boolean manipulable) {
        super(new SingleElementLayout());
        this.title = title;

        if (manipulable) {
            upperBar = makeUpperBar(title);
        } else {
            titleComponent = new STextArea(title, FRAME_TITLE_BAR_SIZE, 0, true, NGFonts.TextType.TITLE, SFrameLookAndFeel.Alignment.CENTER_TOP);
            upperBar = new SPanel(new SingleElementLayout(), true);
            upperBar.add(titleComponent, null);
        }
        upperBar.setParent(this);
        upperBar.validateLayout();

        setMainPanel(new SPanel());
        setSize(width, height);
        setGrowthPolicy(false, false);
    }

    /**
     * Creates a SFrame with the given title and minimum size
     * @param title the title of the new frame
     * @see SPanel
     */
    public SFrame(String title) {
        this(title, 0, 0);
    }

    public SFrame(String title, SComponent mainPanel) {
        this(title);
        setMainPanel(mainPanel);
        pack();
    }

    public void setTitle(String title) {
        titleComponent.setText(title);
    }

    private SPanel makeUpperBar(String frameTitle) {
        SExtendedTextArea title = new SExtendedTextArea(
                frameTitle, FRAME_TITLE_BAR_SIZE, 0, true,
                NGFonts.TextType.TITLE, SFrameLookAndFeel.Alignment.CENTER
        );
        titleComponent = title;
        title.setDragListener(this::addToPosition);

        SPanel bar = SPanel.row(
                title, new SCloseButton(this)
        );
        bar.setBorderVisible(true);

        return bar;
    }

    /**
     * sets the area below the title bar to contain the given component. The size of this component is determined by
     * this frame.
     * @param comp the new middle component
     * @return this
     */
    public SFrame setMainPanel(SComponent comp) {
        comp.setPosition(0, upperBar.getHeight());
        super.add(comp, null); // single element layout
        return this;
    }

    /**
     * This method should not be used. The main panel should be modified, which can be set with {@link
     * #setMainPanel(SComponent)}
     * @throws UnsupportedOperationException always
     * @deprecated this should not be used
     */
    @Deprecated
    @Override
    public void add(SComponent comp, Object prop) {
        throw new UnsupportedOperationException("Tried adding components to a SFrame, which is illegal");
    }

    /**
     * sets the size of this frame to the minimum as a call to {@code setSize(minWidth(), minHeight())}
     */
    public void pack() {
        setSize(minWidth(), minHeight());
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        if (!isVisible()) return;
        // take offset into account for consistency.
        design.draw(PANEL, screenPosition, dimensions);
        upperBar.draw(design, screenPosition);
        drawChildren(design, screenPosition);
    }

    @Override
    public int minWidth() {
        return Math.max(super.minWidth(), upperBar.minWidth());
    }

    @Override
    public int minHeight() {
        return super.minHeight() + upperBar.minHeight();
    }

    @Override
    public String toString() {
        return "SFrame (" + title + ")";
    }

    @Override
    public SComponent getComponentAt(int xRel, int yRel) {
        if (upperBar.contains(xRel, yRel)) {
            return upperBar.getComponentAt(xRel, yRel);
        }
        return super.getComponentAt(xRel, yRel);
    }

    @Override
    public Vector2i getScreenPosition() {
        return new Vector2i(position);
    }

    @Override
    public void doValidateLayout() {
        super.doValidateLayout();
        upperBar.setSize(getWidth(), FRAME_TITLE_BAR_SIZE);
        upperBar.validateLayout();
    }

    @Override
    protected ComponentBorder getLayoutBorder() {
        return new ComponentBorder(INNER_BORDER, INNER_BORDER, INNER_BORDER + upperBar.getHeight(), INNER_BORDER);
    }

    /**
     * removes this component and release any resources used
     */
    public void dispose() {
        this.setVisible(false);
        isDisposed = true;
    }

    /**
     * @return true if {@link #dispose()} has been successfully called
     */
    public boolean isDisposed() {
        return isDisposed;
    }
}
