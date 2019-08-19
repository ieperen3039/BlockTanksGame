package NG.GUIMenu;

import NG.Core.GameAspect;
import NG.GUIMenu.Components.SComponent;
import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.InputHandling.MouseTools.MouseTool;
import NG.InputHandling.MouseTools.MouseToolListener;
import org.joml.Vector2ic;

/**
 * @author Geert van Ieperen created on 18-5-2019.
 */
public interface HUDManager extends GameAspect, MouseToolListener {

    /**
     * draws the elements of this HUD
     * @param painter
     */
    void draw(GUIPainter painter);

    /**
     * adds a component to the hud.
     * The position of the component may be changed as a result of this call.
     * @param component any new component
     */
    void addElement(SComponent component);

    /**
     * removes a component from the hud
     * @param component a component previously added
     * @return
     */
    boolean removeElement(SComponent component);

    /**
     * sets the appearance of the frames on the next drawing cycles to the given object. This overrides any previous
     * setting.
     * @param lookAndFeel any look-and-feel provider.
     */
    void setLookAndFeel(SFrameLookAndFeel lookAndFeel);

    /**
     * @return false iff no call to {@link #setLookAndFeel(SFrameLookAndFeel)} has occurred.
     */
    boolean hasLookAndFeel();

    SComponent getComponentAt(int xSc, int ySc);

    /**
     * @param xSc screen x coordinate in pixels from left
     * @param ySc screen y coordinate in pixels from top
     * @return the SFrame covering the given coordinate
     */
    boolean covers(int xSc, int ySc);

    /**
     * The next click action is redirected to the given listener instead of being processed by the frames. This is reset
     * after such click occurs.
     * @param listener a listener that receives the button and screen positions of the next click exactly once.
     */
    void setModalListener(SComponent listener);

    /**
     * applies the given mouse tool to the modalComponent, iff the given xSc and ySc coordinates are on this component
     * @param tool a mouse tool
     * @param xSc x screen position in pixels from left
     * @param ySc y screen position in pixels from top
     * @param modalComponent a fully visible component
     * @return true iff the tool was applied on the component
     */
    static boolean applyOnComponent(MouseTool tool, int xSc, int ySc, SComponent modalComponent) {
        Vector2ic mPos = modalComponent.getScreenPosition();

        if (xSc >= mPos.x() && ySc >= mPos.y()) {
            if (xSc <= mPos.x() + modalComponent.getWidth()) {
                if (ySc <= mPos.y() + modalComponent.getHeight()) {
                    tool.apply(modalComponent, xSc, ySc);
                    return true;
                }
            }
        }
        return false;
    }
}
