package NG.GUIMenu.Components;

import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.GUIMenu.NGFonts;
import NG.InputHandling.MouseRelativeClickListener;
import NG.InputHandling.MouseReleaseListener;
import org.joml.Vector2ic;

import static NG.GUIMenu.Frames.SFrameLookAndFeel.UIComponent.BUTTON_ACTIVE;
import static NG.GUIMenu.Frames.SFrameLookAndFeel.UIComponent.BUTTON_PRESSED;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

/**
 * @author Geert van Ieperen. Created on 22-9-2018.
 */
public class SCloseButton extends SComponent implements MouseReleaseListener, MouseRelativeClickListener {
    private Runnable closeAction;
    private boolean state = false;

    public SCloseButton(SFrame frame) {
        this.closeAction = frame::dispose;
        setGrowthPolicy(false, false);
    }

    public SCloseButton(Runnable closeAction) {
        this.closeAction = closeAction;
        setGrowthPolicy(false, false);
    }

    public void setCloseAction(Runnable closeAction) {
        this.closeAction = closeAction;
    }

    @Override
    public int minWidth() {
        return SFrame.FRAME_TITLE_BAR_SIZE;
    }

    @Override
    public int minHeight() {
        return SFrame.FRAME_TITLE_BAR_SIZE;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic scPos) {
        design.draw(state ? BUTTON_PRESSED : BUTTON_ACTIVE, scPos, dimensions);
        design.drawText(scPos, dimensions, "X", NGFonts.TextType.ACCENT, SFrameLookAndFeel.Alignment.CENTER);

//        try {
//            design.drawIconButton(position, dimensions, null, state);
//        } catch (IOException e) {
//            Logger.WARN.print(e);
//        }
    }

    @Override
    public void onClick(int button, int x, int y) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) state = true;
    }

    @Override
    public void onRelease(int button, int x, int y) {
        if (state && button == GLFW_MOUSE_BUTTON_LEFT) {
            closeAction.run();
        }
    }

    @Override
    public String toString() {
        return "SCloseButton";
    }
}
