package NG.Rendering;

import NG.Camera.Camera;
import NG.Camera.StaticCamera;
import NG.Core.AbstractGameLoop;
import NG.Core.Game;
import NG.Core.GameAspect;
import NG.Core.GameTimeControl;
import NG.DataStructures.Generic.Color4f;
import NG.GUIMenu.HUDManager;
import NG.GUIMenu.NVGOverlay;
import NG.InputHandling.KeyMouseCallbacks;
import NG.Rendering.Lights.GameLights;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MatrixStack.SceneShaderGL;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shaders.PhongShader;
import NG.Rendering.Shaders.SceneShader;
import NG.Rendering.Shaders.TextureShader;
import NG.Rendering.Textures.Texture2D;
import NG.Settings.Settings;
import NG.Shapes.GenericShapes;
import NG.Tools.*;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/**
 * Repeatedly renders a frame of the main camera of the game given by {@link #init(Game)}
 * @author Geert van Ieperen. Created on 13-9-2018.
 */
public class RenderLoop extends AbstractGameLoop implements GameAspect {
    private final NVGOverlay overlay;
    private Game game;
    private SceneShader uiShader;

    private TimeObserver timeObserver;

    /**
     * creates a new, paused gameloop
     * @param targetFPS the target frames per second
     */
    public RenderLoop(int targetFPS) {
        super("Renderloop", targetFPS);
        overlay = new NVGOverlay();
    }

    public void init(Game game) throws IOException {
        if (this.game != null) return;
        this.game = game;
        Settings settings = game.get(Settings.class);

        overlay.init(settings.ANTIALIAS_LEVEL);

        overlay.addHudItem((painter) ->
                game.getAll(HUDManager.class)
                        .forEach(h -> h.draw(painter))
        );
        overlay.addHudItem((painter) -> {
            if (settings.DEBUG_SCREEN) {
                Logger.putOnlinePrint(painter::printRoll);
            }
        });

        uiShader = new PhongShader();

        KeyMouseCallbacks input = game.get(KeyMouseCallbacks.class);
        input.addKeyPressListener(k -> {
            if (k == GLFW.GLFW_KEY_PERIOD) {
                Logger.DEBUG.print("\n" + timeObserver.resultsTable());
            }
        });

        if (settings.DEBUG) {
            timeObserver = new TimeObserverImpl((getTPS() / 4) + 1, true){
                @Override
                protected void endTiming() {
                    glFinish();
                    super.endTiming();
                }
            };

        } else {
            timeObserver = new TimeObserver.EmptyObserver();
        }
    }

    @Override
    protected void update(float deltaTime) {
        timeObserver.startNewLoop();

        GLFWWindow window = game.get(GLFWWindow.class);
        glViewport(0, 0, window.getWidth(), window.getHeight());
        glEnable(GL_LINE_SMOOTH);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glFinish();

        // current time
        float rendertime;
        if (game.has(GameTimeControl.class)){
            GameTimeControl gameTimer = game.get(GameTimeControl.class);
            gameTimer.updateRenderTime();
            rendertime = gameTimer.getRendertime();

        } else {
            rendertime = 0;
        }

        // camera
        game.ifAvailable(Camera.class, c -> c.updatePosition(deltaTime, rendertime)); // real-time deltatime

        if (game.has(GameLights.class)) {
            timeObserver.startTiming("ShadowMaps");
            for (GameLights lights : game.getAll(GameLights.class)) {
                lights.renderShadowMaps();
            }
            timeObserver.endTiming("ShadowMaps");
        }

        List<RenderBundle> renders = game.getAll(RenderBundle.class);
        for (RenderBundle renderBundle : renders) {
            String identifier = renderBundle.toString();
            timeObserver.startTiming(identifier);
            renderBundle.draw();
            timeObserver.endTiming(identifier);
        }

        int windowWidth = window.getWidth();
        int windowHeight = window.getHeight();

        timeObserver.startTiming("GUI");
        overlay.draw(windowWidth, windowHeight, 10, 10, 16);
        timeObserver.endTiming("GUI");

        // update window
        timeObserver.startTiming("Window Refresh");
        window.update();
        timeObserver.endTiming("Window Refresh");

        // loop clean
        Toolbox.checkGLError();
        if (window.shouldClose()) stopLoop();

        timeObserver.startTiming("Loop Overhead");
    }

    @Override
    public void cleanup() {
        uiShader.cleanup();
        overlay.cleanup();
    }

    private void dumpTexture(Texture2D texture, String fileName) {
        assert (uiShader instanceof TextureShader);
        GLFWWindow window = game.get(GLFWWindow.class);

        uiShader.bind();
        {
            uiShader.initialize(game);
            Camera viewpoint = new StaticCamera(new Vector3f(0, 0, 3), Vectors.newZero(), Vectors.newX());

            SGL tgl = new SceneShaderGL(uiShader, texture.getWidth(), texture.getHeight(), viewpoint);

            uiShader.setPointLight(Vectors.Z, Color4f.WHITE, 0.8f);
            ((TextureShader) uiShader).setTexture(texture);
            tgl.render(GenericShapes.TEXTURED_QUAD, null);
            ((TextureShader) uiShader).unsetTexture();

        }
        uiShader.unbind();
        window.printScreen(Directory.screenshots, fileName, GL_BACK);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public SceneShader getUIShader() {
        return uiShader;
    }

    private class Pointer {
        private static final float SIZE = 2;
        private Vector3f midSquare;
        private Vector3f exact;
        private Vector3f exactNegate;
        private boolean isVisible;

        private Pointer() {
            this.midSquare = new Vector3f();
            this.exact = new Vector3f();
            this.exactNegate = new Vector3f();
        }

        public void draw(SGL gl) {
            if (!isVisible) return;
            gl.pushMatrix();
            {
                gl.translate(exact);
                Toolbox.draw3DPointer(gl);
                gl.translate(exactNegate);

                gl.translate(midSquare);
                gl.translate(0, 0, 2 + SIZE);
                gl.scale(SIZE, SIZE, -SIZE);

                if (gl.getShader() instanceof MaterialShader) {
                    MaterialShader mShader = (MaterialShader) gl.getShader();
                    mShader.setMaterial(Material.ROUGH, Color4f.WHITE);
                }

                gl.render(GenericShapes.ARROW, null);
            }
            gl.popMatrix();
        }

        public void setPosition(Vector3fc position, Vector3fc midSquare) {
            this.midSquare.set(midSquare);
            this.exact.set(position);
            this.exactNegate = exact.negate(exactNegate);
        }
    }

}
