package NG.GUIMenu;

import NG.Core.Game;
import NG.GUIMenu.Components.SComponent;
import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Rendering.GLFWWindow;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geert van Ieperen created on 18-5-2019.
 */
public abstract class SimpleHUD implements HUDManager {
    private final List<SComponent> freeFloatingElements = new ArrayList<>();
    protected Game game;

    protected SFrameLookAndFeel lookAndFeel;
    private SComponent modalComponent = null;
    private SComponent mainPanel;

    /**
     * create a HUD manager with the given look-and-feel and layout manager
     * @param lookAndFeel an uninitialized look-and-feel for this manager
     */
    public SimpleHUD(SFrameLookAndFeel lookAndFeel) {
        this(lookAndFeel, null);
    }

    /**
     * create a HUD manager with the given look-and-feel and layout manager
     * @param lookAndFeel an uninitialized look-and-feel for this manager
     */
    public SimpleHUD(SFrameLookAndFeel lookAndFeel, SComponent mainPanel) {
        this.lookAndFeel = lookAndFeel;
        this.mainPanel = mainPanel;
    }

    /**
     * sets the given container to cover the entire screen
     * @param container
     */
    public void display(SComponent container) {
        this.mainPanel = container;

        if (game != null) {
            GLFWWindow window = game.get(GLFWWindow.class);
            container.setSize(window.getWidth(), window.getHeight());
        }
    }

    @Override
    public void init(Game game) throws Exception {
        if (this.game != null) return;
        this.game = game;
        lookAndFeel.init(game);

        if (mainPanel != null) {
            GLFWWindow window = game.get(GLFWWindow.class);
            mainPanel.setSize(window.getWidth(), window.getHeight());
        }
    }

    @Override
    public void setModalListener(SComponent listener) {
        modalComponent = listener;
    }

    @Override
    public void draw(GUIPainter painter) {
        if (mainPanel == null) return;
        assert hasLookAndFeel();

        GLFWWindow window = game.get(GLFWWindow.class);
        if (window.getWidth() != mainPanel.getWidth() || window.getHeight() != mainPanel.getHeight()){
            mainPanel.setSize(window.getWidth(), window.getHeight());
        }

        lookAndFeel.setPainter(painter);
        freeFloatingElements.forEach(SComponent::validateLayout);
        mainPanel.validateLayout();

        freeFloatingElements.forEach(e -> e.draw(lookAndFeel, e.getPosition()));
        mainPanel.draw(lookAndFeel, new Vector2i(0, 0));
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
    public void addElement(SComponent component) {
        freeFloatingElements.add(component);
    }

    @Override
    public boolean removeElement(SComponent component) {
        return freeFloatingElements.remove(component);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public boolean checkMouseClick(MouseTool tool, final int xSc, final int ySc) {
        SComponent component;

        // check modal dialogues
        if (modalComponent != null) {
            SComponent tgt = this.modalComponent;
            modalComponent = null;

            HUDManager.applyOnComponent(tool, xSc, ySc, tgt);
            return true; // did do something; disabling the modal component
        }

        component = getComponentAt(xSc, ySc);

        if (component != null) {
            tool.apply(component, xSc, ySc);
            return true;
        }

        return false;
    }

    @Override // also fixes checkMouseClick
    public SComponent getComponentAt(int xSc, int ySc) {
        for (SComponent elt : freeFloatingElements) {
            if (elt.isVisible() && elt.contains(xSc, ySc)) {
                int xr = xSc - elt.getX();
                int yr = ySc - elt.getY();
                return elt.getComponentAt(xr, yr);
            }
        }

        return mainPanel.getComponentAt(xSc, ySc);
    }

    @Override
    public boolean covers(int xSc, int ySc) {
        for (SComponent elt : freeFloatingElements) {
            if (elt.isVisible() && elt.contains(xSc, ySc)) return true;
        }

        // TODO more efficient implementation
        return getComponentAt(xSc, ySc) != null;
    }

    @Override
    public void cleanup() {
        freeFloatingElements.clear();
    }
}
