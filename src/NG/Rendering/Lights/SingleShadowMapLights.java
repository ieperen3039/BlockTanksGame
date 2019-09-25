package NG.Rendering.Lights;

import NG.Camera.Camera;
import NG.CollisionDetection.GameState;
import NG.Core.Game;
import NG.DataStructures.Generic.Color4f;
import NG.GameMap.GameMap;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.DepthShader;
import NG.Rendering.Shaders.LightShader;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Storable;
import NG.Tools.Toolbox;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Geert van Ieperen created on 3-2-2019.
 */
public class SingleShadowMapLights implements GameLights {
    private static final int UPDATE_MARGIN = 10;

    private final Lock pointLightEditLock;
    private final Lock pointLightReadLock;

    private final List<PointLight> lights;
    private DirectionalLight sunLight;

    private Game game;
    private DepthShader shadowShader;

    private float lightDist = 1;
    private boolean staticMapIsDirty = false;

    public SingleShadowMapLights(int staticResolution, int dynamicResolution) {
        ReadWriteLock rwl = new ReentrantReadWriteLock(false);
        this.pointLightEditLock = rwl.writeLock();
        this.pointLightReadLock = rwl.readLock();

        this.lights = new ArrayList<>();
        this.sunLight = new DirectionalLight(Color4f.WHITE, new Vector3f(1, -1, 1), 0.5f, staticResolution, dynamicResolution);
    }

    @Override
    public void init(Game game) throws Exception {
        this.game = game;
        Future<DepthShader> shader = game.computeOnRenderThread(DepthShader::new);

        this.sunLight.init(game);

        this.shadowShader = shader.get();
    }

    @Override
    public void addPointLight(PointLight light) {
        pointLightEditLock.lock();
        try {
            lights.add(light);
        } finally {
            pointLightEditLock.unlock();
        }
    }

    @Override
    public void addDirectionalLight(Vector3fc origin, Color4f color, float intensity) {
        sunLight.setDirection(origin);
        sunLight.setColor(color);
        sunLight.setIntensity(intensity);
    }

    @Override
    public void renderShadowMaps() {
        Camera camera = game.get(Camera.class);
        Vector3fc playerFocus = camera.getFocus();
        float viewDist = camera.vectorToFocus().length();
        Vector3fc lightFocus = sunLight.getLightFocus();

        if (playerFocus.distanceSquared(lightFocus) > UPDATE_MARGIN * UPDATE_MARGIN) {
            sunLight.setLightFocus(playerFocus);

            staticMapIsDirty = true;

        } else if (Math.abs(viewDist - lightDist) > UPDATE_MARGIN) {
            float lightCubeSize = 10 + 8 * viewDist + UPDATE_MARGIN;
            sunLight.setLightSize(lightCubeSize);

            staticMapIsDirty = true;
            lightDist = viewDist;
        }

        if (sunLight.doStaticShadows() || sunLight.doDynamicShadows()) {
            // shadow render
            shadowShader.bind();
            {
                shadowShader.initialize(game);

                if (staticMapIsDirty && sunLight.doStaticShadows()) {
                    shadowShader.setDynamic(false);
                    DepthShader.DepthGL gl = shadowShader.getGL(game);
                    shadowShader.setDirectionalLight(sunLight);
                    game.get(GameMap.class).draw(gl);

                    gl.cleanup();
                }

                if (sunLight.doDynamicShadows()) {
                    glCullFace(GL_FRONT);
                    shadowShader.setDynamic(true);
                    DepthShader.DepthGL gl = shadowShader.getGL(game);
                    shadowShader.setDirectionalLight(sunLight);
                    game.get(GameState.class).drawEntities(gl);
                    glCullFace(GL_BACK);

                    gl.cleanup();
                }
            }
            shadowShader.unbind();

            Toolbox.checkGLError();
        }
    }

    @Override
    public void draw(SGL gl) {
        ShaderProgram shader = gl.getShader();
        if (shader instanceof LightShader) {
            LightShader lightShader = (LightShader) shader;

            pointLightReadLock.lock();
            try {
                for (PointLight light : lights) {
                    Vector3fc mPosition = gl.getPosition(light.getPosition());
                    lightShader.setPointLight(mPosition, light.getColor(), light.getIntensity());
                }
            } finally {
                pointLightReadLock.unlock();
            }

            lightShader.setDirectionalLight(sunLight);
        }
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void writeToDataStream(DataOutputStream out) throws IOException {
        pointLightReadLock.lock();
        try {
            out.writeInt(sunLight.getStaticShadowMap().getResolution());
            out.writeInt(sunLight.getDynamicShadowMap().getResolution());

            out.writeInt(lights.size());
            for (PointLight light : lights) {
                Storable.writeVector3f(out, light.getPosition());
                Storable.writeVector4f(out, light.getColor().toVector4f());
                out.writeFloat(light.getIntensity());
            }

        } finally {
            pointLightReadLock.unlock();
        }
    }

    public SingleShadowMapLights(DataInputStream in) throws IOException {
        this(in.readInt(), in.readInt());

        try {
            int lightsSize = in.readInt();
            for (int i = 0; i < lightsSize; i++) {
                Vector3f pos = Storable.readVector3f(in);
                Vector4f col = Storable.readVector4f(in);
                float intensity = in.readFloat();

                PointLight light = new FixedPointLight(pos, new Color4f(col), intensity);
                lights.add(light);
            }

        } catch (Exception e) {
            lights.clear();
            throw e;
        }
    }
}
