package NG.Rendering.Shaders;

import NG.Core.Game;
import NG.Core.GameTimer;
import NG.DataStructures.Generic.Color4f;
import NG.Rendering.Lights.DirectionalLight;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.MeshLoading.MeshFile;
import NG.Rendering.Textures.DepthTexture;
import NG.Rendering.Textures.Texture2D;
import NG.Tools.Directory;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

/**
 * @author Geert van Ieperen created on 22-9-2019.
 */
public class WaterShader extends SceneShader {
    private static final Path VERTEX_PATH = Directory.shaders.getPath("Water", "water.vert");
    private static final Path FRAGMENT_PATH = Directory.shaders.getPath("Water", "water.frag");

    // WATER_MESH is 20x20x0 centered on (0, 0, 0)
    private static final Mesh WATER_MESH =
            MeshFile.loadFileRequired(Directory.meshes.getPath("general", "sea_25.ply")).getMesh();
    private final Texture2D WATER_TEXTURE;

    private static final float WATER_LEVEL = 0;
    private static final int OCEAN_LAYERS = 5;
    private static final float BASE_WATER_SCALING = 2f;

    private static final int MAX_POINT_LIGHTS = 16;

    private int nextLightIndex = 0;

    public WaterShader() throws ShaderException, IOException {
        super(VERTEX_PATH, null, FRAGMENT_PATH);
        createUniform("modelMatrix");
        createUniform("currentTime");

        createPointLightsUniform("lights", MAX_POINT_LIGHTS);

        // water properties
        createUniform("material.diffuse");
        createUniform("material.specular");
        createUniform("material.reflectance");

        createUniform("waterHeightMap");
        WATER_TEXTURE = DepthTexture.get(Paths.get("res", "check.png"));
        WATER_TEXTURE.setClamp(Texture2D.ClampMethod.MIRROR_REPEAT);
    }

    @Override
    public void initialize(Game game) {
        Material water = Material.WATER;
        setUniform("material.diffuse", water.baseColor);
        setUniform("material.specular", water.specular);
        setUniform("material.reflectance", water.reflectance);
        // Texture for the model
        setUniform("waterHeightMap", 0);
        float time = game.get(GameTimer.class).getRendertime();
        setUniform("currentTime", time + 10);

        WATER_TEXTURE.bind(GL_TEXTURE0);
        nextLightIndex = 0;
    }

    @Override
    public void setPointLight(Vector3fc position, Color4f color, float intensity) {
        Vector4fc mPosition = new Vector4f(position, 1.0f);
        setLight(color, mPosition, color.alpha * intensity);
    }

    @Override
    public void setDirectionalLight(DirectionalLight light) {
        Vector4fc mPosition = new Vector4f(light.getDirection(), 0.0f);
        Color4f color = light.getColor();
        setLight(color, mPosition, light.getIntensity());
    }

    private void setLight(Color4f color, Vector4fc mPosition, float intensity) {
        int lightNumber = nextLightIndex++;
        assert lightNumber < MAX_POINT_LIGHTS;
        setUniform(("lights[" + lightNumber + "]") + ".color", color.rawVector3f());
        setUniform(("lights[" + lightNumber + "]") + ".mPosition", mPosition);
        setUniform(("lights[" + lightNumber + "]") + ".intensity", intensity);
    }

    /**
     * sets possible unused point-light slots to 'off'. No more point lights can be added after a call to this method.
     */
    @Override
    public void discardRemainingLights() {
        while (nextLightIndex < MAX_POINT_LIGHTS) {
            setPointLight(new Vector3f(), Color4f.INVISIBLE, 0);
        }
    }

    @Override
    public void setMaterial(Color4f diffuse, Color4f specular, float reflectance) {
        // ignore
    }

    public static void drawOcean(SGL gl, Vector3fc playerPosition) {
        gl.pushMatrix();
        {
            gl.translate(playerPosition.x(), playerPosition.y(), WATER_LEVEL);
            gl.scale(BASE_WATER_SCALING);

            gl.render(WATER_MESH, null);
            gl.pushMatrix();
            for (int i = 1; i < OCEAN_LAYERS; i++) {
                oceanLayer(gl);
                gl.scale(3, 3, 1);
            }
            gl.popMatrix();
        }
        gl.popMatrix();
    }

    private static void oceanLayer(SGL gl) {
        for (int xm = -1; xm <= 1; xm++) {
            for (int ym = -1; ym <= 1; ym++) {
                if (xm == 0 && ym == 0) continue;

                int x = 20 * xm;
                int y = 20 * ym;
                gl.translate(x, y, 0);
                gl.render(WATER_MESH, null);
                gl.translate(-x, -y, 0);
            }
        }
    }
}
