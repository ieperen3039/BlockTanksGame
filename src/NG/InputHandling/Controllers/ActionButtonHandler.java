package NG.InputHandling.Controllers;

import NG.Core.Game;
import NG.Core.GameAspect;
import NG.InputHandling.KeyPressListener;
import NG.InputHandling.MouseToolCallbacks;
import NG.Rendering.GLFWWindow;
import NG.Settings.KeyBinding;
import NG.Settings.Settings;
import NG.Tools.Directory;
import NG.Tools.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.lwjgl.opengl.GL11.GL_FRONT;

/**
 * @author Geert van Ieperen. Created on 7-7-2018.
 */
public class ActionButtonHandler implements KeyPressListener, GameAspect {
    private Game game;

    @Override
    public void keyPressed(int key) {
        switch (KeyBinding.get(key)) {
            case TOGGLE_FULLSCREEN:
                Logger.DEBUG.print("Switching fullscreen");
                game.get(GLFWWindow.class).toggleFullScreen();
                break;

            case PRINT_SCREEN:
                SimpleDateFormat ft = new SimpleDateFormat("mm_dd-hh_mm_ss");
                final String name = "Screenshot_" + ft.format(new Date());

                game.get(GLFWWindow.class).printScreen(Directory.screenshots, name, GL_FRONT);
                Logger.DEBUG.print("Saved screenshot as \"" + name + "\"");
                break;

            case DEBUG_SCREEN:
                Settings s = game.get(Settings.class);
                s.DEBUG_SCREEN = !s.DEBUG_SCREEN;
        }
    }

    @Override
    public void init(Game game) throws Exception {
        this.game = game;
        game.get(MouseToolCallbacks.class).addKeyPressListener(this);
    }

    @Override
    public void cleanup() {
        game.get(MouseToolCallbacks.class).removeListener(this);
    }
}
