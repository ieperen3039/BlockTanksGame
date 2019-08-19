package NG.GUIMenu.Components;

import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.GUIMenu.LayoutManagers.GridLayoutManager;
import NG.GUIMenu.LayoutManagers.SLayoutManager;
import NG.GUIMenu.LayoutManagers.SingleElementLayout;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import static NG.GUIMenu.Frames.SFrameLookAndFeel.UIComponent.PANEL;

/**
 * @author Geert van Ieperen. Created on 20-9-2018.
 */
public class SPanel extends SContainer {

    /** when using the default constructor, you can use these values to denote the positions */
    public static final Object NORTH = new Vector2i(1, 0);
    public static final Object EAST = new Vector2i(2, 1);
    public static final Object SOUTH = new Vector2i(1, 2);
    public static final Object WEST = new Vector2i(0, 1);
    public static final Object NORTHEAST = new Vector2i(2, 0);
    public static final Object SOUTHEAST = new Vector2i(2, 2);
    public static final Object MORTHWEST = new Vector2i(0, 0);
    public static final Object SOUTHWEST = new Vector2i(0, 2);
    public static final Object MIDDLE = new Vector2i(1, 1);
    private final int minimumWidth;
    private final int minimumHeight;

    private boolean border = true;

    /**
     * creates a panel with the given layout manager
     * @param minimumWidth   minimum width of the panel in pixels
     * @param minimumHeight  minimum height of the panel in pixels
     * @param layoutManager  a new layout manager for this component
     * @param growHorizontal if true, the panel tries to grow in its width
     * @param growVertical   if true, the panel tries to grow in its height
     * @param drawBorder     if true, this panel itself is drawn
     */
    public SPanel(
            int minimumWidth, int minimumHeight, SLayoutManager layoutManager, boolean growHorizontal,
            boolean growVertical,
            boolean drawBorder
    ) {
        super(layoutManager);
        this.minimumWidth = minimumWidth;
        this.minimumHeight = minimumHeight;
        this.border = drawBorder;

        setGrowthPolicy(growHorizontal, growVertical);
    }

    /**
     * creates a panel that uses a grid layout with the given number of rows and columns
     * @param minimumWidth   minimum width of the panel in pixels
     * @param minimumHeight  minimum height of the panel in pixels
     * @param rows           number of grid rows in this panel
     * @param cols           number of columns in this panel
     * @param growHorizontal if true, the panel tries to grow in its width
     * @param growVertical   if true, the panel tries to grow in its height
     */
    public SPanel(
            int minimumWidth, int minimumHeight, int cols, int rows, boolean growHorizontal, boolean growVertical
    ) {
        this(
                minimumWidth, minimumHeight,
                (cols * rows > 0 ? new GridLayoutManager(cols, rows) : new SingleElementLayout()),
                growHorizontal, growVertical, true);
    }

    /**
     * creates a panel with the given layout manager, minimum size of (0, 0)
     * @param layoutManager a new layout manager for this component
     * @param growPolicy    if true, try to grow
     */
    public SPanel(SLayoutManager layoutManager, boolean growPolicy) {
        this(0, 0, layoutManager, growPolicy, growPolicy, true);
    }

    /**
     * creates a panel that uses a default GridLayout of 3x3 and with minimum size of (0, 0) and a no-growth policy.
     * Objects should be located using one of
     * {@link #NORTH},
     * {@link #EAST},
     * {@link #SOUTH},
     * {@link #WEST},
     * {@link #NORTHEAST},
     * {@link #SOUTHEAST},
     * {@link #MORTHWEST},
     * {@link #SOUTHWEST},
     * {@link #MIDDLE}
     */
    public SPanel() {
        super(new GridLayoutManager(3, 3));
        this.minimumWidth = 0;
        this.minimumHeight = 0;
    }

    /**
     * creates a panel that uses no layout and with given minimum size
     * @param minimumWidth  minimum width of the panel in pixels
     * @param minimumHeight minimum height of the panel in pixels
     * @param growPolicy    if true, this panel tries to grow in each direction
     */
    public SPanel(int minimumWidth, int minimumHeight, boolean growPolicy) {
        super(new GridLayoutManager(1, 1));
        this.minimumWidth = minimumWidth;
        this.minimumHeight = minimumHeight;
        setGrowthPolicy(growPolicy, growPolicy);
    }

    /**
     * the basic panel for holding components. Growth policy is false, and minimum size is (0, 0).
     * Use {@link Vector2i} for positioning elements. Note that indices are 0-indexed.
     * @param cols number of components
     * @param rows number of rows
     */
    public SPanel(int cols, int rows) {
        this(0, 0, cols, rows, false, false);
    }

    /** creates a new panel with the given components on a row
     * @see #column(boolean, boolean, SComponent...) */
    public static SPanel column(SComponent... components) {
        SPanel newPanel = new SPanel(1, components.length);
        newPanel.border = false;

        for (int i = 0; i < components.length; i++) {
            newPanel.add(components[i], new Vector2i(0, i));
        }

        return newPanel;
    }

    /**
     * create a new panel with the given components in a column
     * @param hzGrow whether this panel wants horizontal growth
     * @param vtGrow whether this panel wants vertical growth
     * @param components the components to put in a column, top to bottom
     * @return the new panel
     */
    public static SPanel column(boolean hzGrow, boolean vtGrow, SComponent... components) {
        SPanel column = column(components);
        column.setGrowthPolicy(hzGrow, vtGrow);
        return column;
    }

    /** creates a new panel with the given components in a row
     * @see #row(boolean, boolean, SComponent...) */
    public static SPanel row(SComponent... components) {
        SPanel newPanel = new SPanel(components.length, 1);
        newPanel.border = false;

        for (int i = 0; i < components.length; i++) {
            newPanel.add(components[i], new Vector2i(i, 0));
        }

        return newPanel;
    }

    /**
     * create a new panel with the given components in a row
     * @param hzGrow whether this panel wants horizontal growth
     * @param vtGrow whether this panel wants vertical growth
     * @param components the components to put in a row, top to bottom
     * @return the new panel
     */
    public static SPanel row(boolean hzGrow, boolean vtGrow, SComponent... components) {
        SPanel row = row(components);
        row.setGrowthPolicy(hzGrow, vtGrow);
        return row;
    }

    public void setBorderVisible(boolean doVisible) {
        this.border = doVisible;
    }

    @Override
    public int minWidth() {
        return Math.max(minimumWidth, super.minWidth());
    }

    @Override
    public int minHeight() {
        return Math.max(minimumHeight, super.minHeight());
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        assert getWidth() > 0 && getHeight() > 0 :
                String.format("Non-positive dimensions of %s: width = %d, height = %d", this, getWidth(), getHeight());

        if (border) design.draw(PANEL, screenPosition, dimensions);
        drawChildren(design, screenPosition);
    }

    @Override
    protected ComponentBorder getLayoutBorder() {
        return border ? super.getLayoutBorder() : new ComponentBorder();
    }
}
