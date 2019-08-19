package NG.GUIMenu.Components;

import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.GUIMenu.LayoutManagers.LimitedVisibilityLayout;
import org.joml.Vector2i;
import org.joml.Vector2ic;

/**
 * @author Geert van Ieperen created on 13-5-2019.
 */
public class STileBrowser extends SContainer {
    private static final int SCROLL_BUTTON_WIDTH = 50;
    private final SButton buttonFurther;
    private final SButton buttonBack;
    private LimitedVisibilityLayout layoutManager;

    public STileBrowser(boolean growPolicy, int minWidth, int eltWidth, SComponent... elements) {
        this(eltWidth, growPolicy, minWidth - 2 * SCROLL_BUTTON_WIDTH, elements);
    }

    private STileBrowser(int eltWidth, boolean growPolicy, int layoutWidth, SComponent... elements) {
        this(growPolicy, new LimitedVisibilityLayout(layoutWidth / eltWidth, layoutWidth, false), elements);
    }

    private STileBrowser(boolean growPolicy, LimitedVisibilityLayout layoutManager, SComponent... elements) {
        super(layoutManager);
        this.layoutManager = layoutManager;

        buttonBack = new SButton("<", () -> inc(-1), SCROLL_BUTTON_WIDTH, 0);
        buttonFurther = new SButton(">", () -> inc(1), SCROLL_BUTTON_WIDTH, 0);

        for (SComponent elt : elements) {
            layoutManager.add(elt, null);
        }

        setGrowthPolicy(growPolicy, growPolicy);
    }

    private void inc(int v) {
        this.layoutManager.shiftVisible(v);
        invalidateLayout();
    }

    /**
     * appends the given element to the end of the browser
     * @param elt the element to add
     */
    public void add(SComponent elt) {
        super.add(elt, null);
    }

    @Override
    public SComponent getComponentAt(int xRel, int yRel) {
        if (buttonBack.contains(xRel, yRel)) {
            Vector2ic position = buttonBack.getPosition();
            return buttonBack.getComponentAt(xRel - position.x(), yRel - position.y());

        } else if (buttonFurther.contains(xRel, yRel)) {
            Vector2ic position = buttonFurther.getPosition();
            return buttonFurther.getComponentAt(xRel - position.x(), yRel - position.y());

        } else {
            return super.getComponentAt(xRel, yRel);
        }
    }

    @Override
    public void doValidateLayout() {
        super.doValidateLayout();

        // use original layout border for side buttons instead
        ComponentBorder supBorder = super.getLayoutBorder();
        int height = getHeight() - supBorder.top - supBorder.bottom;

        buttonFurther.setSize(0, height);
        buttonFurther.setPosition(getWidth() - supBorder.right - buttonFurther.getWidth(), supBorder.top);
        buttonFurther.validateLayout();
        buttonBack.setSize(0, height);
        buttonBack.setPosition(supBorder.left, supBorder.top);
        buttonBack.validateLayout();
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        buttonBack.draw(design, new Vector2i(screenPosition).add(buttonBack.position));
        buttonFurther.draw(design, new Vector2i(screenPosition).add(buttonFurther.position));
        drawChildren(design, screenPosition);
    }

    @Override
    protected ComponentBorder getLayoutBorder() {
        return super.getLayoutBorder().add(buttonBack.minWidth(), buttonFurther.minWidth(), 0, 0);
    }
}
