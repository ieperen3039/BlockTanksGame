package NG.GUIMenu.Frames;

import NG.Core.Game;
import NG.GUIMenu.BaseLF;
import NG.GUIMenu.Components.SComponent;
import NG.GUIMenu.Components.SFrame;
import NG.GUIMenu.Components.SToolBar;
import NG.GUIMenu.GUIPainter;
import NG.GUIMenu.HUDManager;
import NG.InputHandling.KeyMouseCallbacks;
import NG.InputHandling.MouseScrollListener;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Tools.Logger;
import org.joml.Vector2ic;

import java.util.*;

/**
 * Objects of this class can manage an in-game window system that is behaviourally similar to classes in the {@link
 * javax.swing} package. New {@link SFrame} objects can be added using {@link #addFrame(SFrame)}.
 * @author Geert van Ieperen. Created on 20-9-2018.
 */
public class FrameManagerImpl implements FrameGUIManager {
    private Game game;
    /** the first element in this list has focus */
    private Deque<SFrame> frames;
    private SComponent modalComponent;

    private SFrameLookAndFeel lookAndFeel;
    private SToolBar toolBar = null;

    public FrameManagerImpl() {
        this.frames = new ArrayDeque<>();
        lookAndFeel = new BaseLF();
    }

    @Override
    public void init(Game game) throws Exception {
        if (this.game != null) return;
        this.game = game;
        lookAndFeel.init(game);
    }

    @Override
    public void draw(GUIPainter painter) {
        assert hasLookAndFeel();

        frames.removeIf(SFrame::isDisposed);
        lookAndFeel.setPainter(painter);

        Iterator<SFrame> itr = frames.descendingIterator();
        while (itr.hasNext()) {
            final SFrame f = itr.next();

            if (f.isVisible()) {
                f.validateLayout();
                f.draw(lookAndFeel, f.getPosition());
            }
        }

        if (modalComponent != null) {
            modalComponent.validateLayout();
            modalComponent.draw(lookAndFeel, modalComponent.getScreenPosition());
        }

        if (toolBar != null) {
            toolBar.draw(lookAndFeel, null);
        }
    }

    @Override
    public boolean removeElement(SComponent component) {
        if (component instanceof SFrame) {
            ((SFrame) component).dispose();
            return true;
        }

        Optional<SComponent> optParent = component.getParent();
        if (optParent.isPresent()) {
            SComponent parent = optParent.get();
            if (parent instanceof SFrame) {
                ((SFrame) parent).dispose();
                return true;
            }
        }
        return false;
    }

    @Override
    public void addFrame(SFrame frame) {
        frame.validateLayout();

        // TODO remove assumption that toolbar is on top
        int toolbarHeight = toolBar == null ? 0 : toolBar.getHeight();
        int x = 50;
        int y = 50 + toolbarHeight;

        // reposition frame not to overlap other frames (greedy)
        for (Iterator<SFrame> iterator = frames.descendingIterator(); iterator.hasNext(); ) {
            SFrame other = iterator.next();
            if (other.isDisposed() || !other.isVisible()) continue;

            Vector2ic otherPos = other.getScreenPosition();

            if (otherPos.x() == x && otherPos.y() == y) {
                x += 20;
                y += 20; // MS windows-style
            }
        }

        addFrame(frame, x, y);
    }

    @Override
    public void addFrame(SFrame frame, int x, int y) {
        // if the frame was already visible, still add it to make it focused.
        frames.remove(frame);

        boolean success = frames.offerFirst(frame);
        if (!success) {
            Logger.DEBUG.print("Too much subframes opened, removing the last one");
            frames.removeLast().dispose();
            frames.addFirst(frame);
        }

        frame.setPosition(x, y);
    }

    @Override
    public void focus(SFrame frame) {
        if (frame.isDisposed()) {
            throw new NoSuchElementException(frame + " is disposed");
        }

        // even if the frame was not opened, show it
        frame.setVisible(true);

        // no further action when already focused
        if (frames.peekFirst().equals(frame)) return;

        boolean success = frames.remove(frame);
        if (!success) {
            throw new NoSuchElementException(frame + " was not part of the window");
        }

        frames.addFirst(frame);
    }

    @Override
    public void setLookAndFeel(SFrameLookAndFeel lookAndFeel) {
        this.lookAndFeel = lookAndFeel;
    }

    @Override
    public boolean hasLookAndFeel() {
        return lookAndFeel != null;
    }

    @Override
    public void setModalListener(SComponent listener) {
        modalComponent = listener;
    }

    @Override
    public void setToolBar(SToolBar toolBar) {
        this.toolBar = toolBar;
    }

    @Override
    public SToolBar getToolBar() {
        return toolBar;
    }

    @Override
    public void cleanup() {
        game.get(KeyMouseCallbacks.class).removeListener(this);
        frames.forEach(SFrame::dispose);
        frames.clear();
    }

    @Override
    public boolean covers(int xSc, int ySc) {
        if (toolBar != null && toolBar.contains(xSc, ySc)) {
            return true;
        }

        if (modalComponent != null && modalComponent.contains(xSc, ySc)) {
            return true;
        }

        for (SFrame frame : frames) {
            if (frame.isVisible() && frame.contains(xSc, ySc)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean checkMouseClick(MouseTool tool, final int xSc, final int ySc) {
        SComponent component;

        // check modal dialogues
        if (modalComponent != null) {
            SComponent tgt = this.modalComponent;
            modalComponent = null;

            return HUDManager.applyOnComponent(tool, xSc, ySc, tgt);

        } else {
            component = getComponentAt(xSc, ySc);

            if (component != null) {
                tool.apply(component, xSc, ySc);
                return true;
            }
        }

        return false;
    }

    @Override
    public SComponent getComponentAt(int xSc, int ySc) {
        // check toolbar
        if (toolBar != null) {
            if (toolBar.contains(xSc, ySc)) {
                return toolBar;
            }
        }

        // check all frames, starting from the front-most frame
        for (SFrame frame : frames) {
            if (frame.isVisible() && frame.contains(xSc, ySc)) {
                focus(frame);
                int xr = xSc - frame.getX();
                int yr = ySc - frame.getY();
                return frame.getComponentAt(xr, yr);
            }
        }

        return null;
    }

    @Override
    public boolean checkMouseScroll(int xSc, int ySc, float value) {
        SComponent component = getComponentAt(xSc, ySc);

        if (component instanceof MouseScrollListener) {
            MouseScrollListener listener = (MouseScrollListener) component;
            listener.onScroll(value);
            return true;
        }

        return false;
    }
}
