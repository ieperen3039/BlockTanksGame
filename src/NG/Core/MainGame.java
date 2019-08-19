package NG.Core;

import NG.Blocks.BaseBlocks;
import NG.Blocks.BlockTypeCollection;
import NG.Blocks.FileBlockTypeCollection;
import NG.Camera.Camera;
import NG.Camera.PointCenteredCamera;
import NG.Camera.StrategyCamera;
import NG.CollisionDetection.GameState;
import NG.CollisionDetection.PhysicsEngine;
import NG.ConstructionMode.ConstructionMenu;
import NG.DataStructures.Generic.Color4f;
import NG.Entities.EntityList;
import NG.GUIMenu.Components.SButton;
import NG.GUIMenu.Components.SFiller;
import NG.GUIMenu.Components.SFrame;
import NG.GUIMenu.Components.SPanel;
import NG.GUIMenu.Frames.FrameGUIManager;
import NG.GUIMenu.Frames.FrameManagerImpl;
import NG.GUIMenu.HUDManager;
import NG.GUIMenu.TankHUD;
import NG.GameMap.EmptyMap;
import NG.GameMap.GameMap;
import NG.InputHandling.ClickShader;
import NG.InputHandling.MouseToolCallbacks;
import NG.Mods.JarModReader;
import NG.Mods.Mod;
import NG.Mods.ModLoader;
import NG.Particles.GameParticles;
import NG.Particles.ParticleShader;
import NG.Rendering.GLFWWindow;
import NG.Rendering.Lights.GameLights;
import NG.Rendering.Lights.SingleShadowMapLights;
import NG.Rendering.RenderBundle;
import NG.Rendering.RenderLoop;
import NG.Rendering.Shaders.PhongShader;
import NG.Settings.KeyBinding;
import NG.Settings.Settings;
import NG.Tools.*;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Geert van Ieperen created on 2-8-2019.
 */
public class MainGame implements ModLoader {
    private static final Version GAME_VERSION = new Version(0, 0);
    private Splash splashWindow;

    private final RenderLoop renderer;
    private final GLFWWindow window;

    private final Game.Proxy gameService;
    private final Game game;
    private final Game menu;
    private final Game construction;

    private List<Mod> allMods;
    private List<Mod> activeMods = Collections.emptyList();
    private final MouseToolCallbacks inputHandler;
    private SButton.BProps bProps = new SButton.BProps(300, 80, false, false);

    public MainGame() throws IOException {
        Logger.DEBUG.print("Showing splash...");
        splashWindow = new Splash();
        splashWindow.run();

        try {
            Logger.INFO.print("Starting up the game engine...");
            String mainThreadName = Thread.currentThread().getName();

            // these are not GameAspects, and thus the init() rule does not apply.
            Settings settings = new Settings();
            window = new GLFWWindow(Settings.GAME_NAME, new GLFWWindow.Settings(settings), true);
            renderer = new RenderLoop(settings.TARGET_FPS);
            GameTimer gameTimer = new GameTimer(settings.RENDER_DELAY);

            inputHandler = new MouseToolCallbacks();
            HUDManager menuHud = new FrameManagerImpl();

            menu = new GameService(GAME_VERSION, mainThreadName, renderer, window, inputHandler,
                    gameTimer, settings, menuHud
            );

            HUDManager hud = new TankHUD();
//            Camera camera = new StaticCamera(new Vector3f(20, 25, 10), Vectors.newZero(), Vectors.newZ());
            Camera camera = new StrategyCamera(Vectors.newZero(), 20, 10);
            GameLights lights = new SingleShadowMapLights();
            GameState state = new PhysicsEngine();
//            GameMap map = new MeshMap(Directory.maps.getPath("map.obj"));
            GameMap map = new EmptyMap();
            GameParticles particles = new GameParticles();

            game = new GameService(GAME_VERSION, mainThreadName, renderer,
                    lights, camera, gameTimer, settings, window, state, inputHandler, hud, map, particles
            );

            // world
            game.add(new RenderBundle(new PhongShader())
                    .add(gl -> game.get(GameLights.class).draw(gl))
                    .add(gl -> game.get(GameMap.class).draw(gl))
                    .add(gl -> game.get(GameState.class).drawEntities(gl))
            );

            // particles
            game.add(new RenderBundle(new ParticleShader())
                    .add(gl -> game.get(GameParticles.class).draw(gl)));

            BaseBlocks baseBlocks = new BaseBlocks();
            baseBlocks.generateDefaults();
            BlockTypeCollection fileBlocks = new FileBlockTypeCollection("Base");
            HUDManager constMenu = new ConstructionMenu(baseBlocks, fileBlocks);
            Camera constCamera = new PointCenteredCamera(new Vector3f(-10, 0, 20), new Vector3f());
            GameLights constLights = new SingleShadowMapLights();
            GameState constState = new EntityList();

            construction = new GameService(GAME_VERSION, mainThreadName, renderer, window, inputHandler,
                    constMenu, settings, constCamera, constLights, constState, gameTimer
            );

            construction.add(new RenderBundle(new PhongShader())
                    .add(gl -> construction.get(GameLights.class).draw(gl))
                    .add(gl -> construction.get(GameState.class).drawEntities(gl))
                    .add(Toolbox::drawAxisFrame)
            );

            gameService = new Game.Proxy(menu);

        } catch (Exception ex) {
            splashWindow.dispose();
            throw ex;
        }
    }

    public void root() throws Exception {
        init();
        run();
        cleanup();
    }

    private void init() throws Exception {
        Logger.DEBUG.print("Initializing...");
        renderer.init(gameService);
        inputHandler.init(gameService);
        menu.init();
        construction.init();
        game.init();

        GLFWWindow window = menu.get(GLFWWindow.class);
        SFrame mainMenu = getMainMenu(350, 500);
        mainMenu.validateLayout();
        int x = window.getWidth() / 2 - mainMenu.getWidth() / 2;
        int y = window.getHeight() / 2 - mainMenu.getHeight() / 2;
        menu.get(FrameGUIManager.class).addFrame(mainMenu, x, y);

        inputHandler.addKeyPressListener(k -> {
            if (KeyBinding.get(k) == KeyBinding.EXIT_GAME) {
                renderer.stopLoop();
            }
        });

        renderer.defer(() -> {
            try {
                ClickShader clickShader = new ClickShader();
                construction.add(clickShader);
                game.add(clickShader);
                Logger.INFO.print("Installed shader-based click detection");

            } catch (Exception ex) {
                Logger.WARN.print("Could not install click shader: " + ex);
            }
        });

        construction.get(GameLights.class).
                addDirectionalLight(new Vector3f(-1, -1, 2), Color4f.WHITE, 0.5f);
        construction.get(GameLights.class).
                addDirectionalLight(new Vector3f(1, 1, 0.5f), Color4f.WHITE, 0.1f);

        game.get(GameLights.class).
                addDirectionalLight(new Vector3f(2, 1.5f, 0.5f), Color4f.WHITE, 0.5f);

        Logger.DEBUG.print("Loading mods...");

        allMods = JarModReader.loadMods(Directory.mods);

        Logger.INFO.print("Finished initialisation");
    }

    @Override
    public void initMods(List<Mod> mods) {
        for (Iterator<Mod> it = mods.iterator(); it.hasNext(); ) {
            Mod mod = it.next();
            try {
                mod.init(game);

            } catch (Exception ex) {
                Logger.ERROR.print("Error while loading " + mod.getModName(), ex);
                it.remove();
            }
        }
    }

    @Override
    public List<Mod> allMods() {
        return Collections.unmodifiableList(allMods);
    }

    @Override
    public Mod getModByName(String name) {
        for (Mod mod : allMods) {
            if (mod.getModName().equals(name)) {
                return mod;
            }
        }
        return null;
    }

    @Override
    public void cleanMods() {
        activeMods.forEach(Mod::cleanup);
        activeMods.clear();
    }

    private void cleanup() {
        game.cleanup();
        menu.cleanup();
        splashWindow.dispose();

        window.cleanup();
        renderer.cleanup();
    }

    private void run() {
        splashWindow.dispose();

        window.open();
        renderer.run();
        window.close();
    }

    private SFrame getMainMenu(int width, int height) {
        SFrame frame = new SFrame("Main menu", width, height, false);
        frame.setMainPanel(
                SPanel.row(
                        new SFiller(),
                        SPanel.column(
                                new SFiller(),
                                new SButton("New Construction", () -> gameService.select(construction), bProps),
                                new SButton("Exit", renderer::stopLoop, bProps),
                                new SFiller()
                        ),
                        new SFiller()
                ));
        return frame;
    }
}
