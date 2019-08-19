package NG.Rendering.Shaders;

import NG.Camera.Camera;
import NG.Core.Game;
import NG.DataStructures.Generic.Color4f;
import NG.Rendering.Lights.DirectionalLight;
import NG.Rendering.Textures.GenericTextures;
import NG.Rendering.Textures.Texture;
import NG.Settings.Settings;
import NG.Tools.Directory;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.IOException;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.*;

/**
 * A shader that uses a shadow-map and a Blinn-Phong model for lighting
 * @author Geert van Ieperen
 */
@SuppressWarnings("Duplicates")
public class BlinnPhongShader extends SceneShader implements TextureShader {
    private static final Path VERTEX_PATH = Directory.shaders.getPath("BlinnPhong", "blinnphong.vert");
    private static final Path FRAGMENT_PATH = Directory.shaders.getPath("BlinnPhong", "blinnphong.frag");
    private static final Path GEOMETRY_PATH = Directory.shaders.getPath("BlinnPhong", "blinnphong.geom");
    private static final int MAX_POINT_LIGHTS = 10;
    private static final float SPECULAR_POWER = 10f;

    private int nextLightIndex = 0;

    /**
     * @throws ShaderException if a new shader could not be created by some opengl reason
     * @throws IOException     if the defined files could not be found (the file is searched for in the shader folder
     *                         itself, and should exclude any first slash)
     */
    public BlinnPhongShader() throws ShaderException, IOException {
        super(VERTEX_PATH, GEOMETRY_PATH, FRAGMENT_PATH);

        createUniform("material.diffuse");
        createUniform("material.specular");
        createUniform("material.reflectance");

        createUniform("directionalLight.color");
        createUniform("directionalLight.direction");
        createUniform("directionalLight.intensity");
        createUniform("directionalLight.lightSpaceMatrix");
        createUniform("directionalLight.shadowEnable");

        createPointLightsUniform("pointLights", MAX_POINT_LIGHTS);

        createUniform("texture_sampler");
        createUniform("staticShadowMap");
        createUniform("dynamicShadowMap");

        createUniform("ambientLight");
        createUniform("specularPower");
        createUniform("cameraPosition");

        createUniform("hasTexture");
        createUniform("hasColor");

        createUniform("lineColor");
    }

    @Override
    public void initialize(Game game) {
        Settings settings = game.get(Settings.class);
        // Base variables
        Vector3fc eye = game.get(Camera.class).getEye();
        setUniform("ambientLight", Settings.AMBIENT_LIGHT.toVector3f());
        setUniform("cameraPosition", eye);
        setUniform("specularPower", SPECULAR_POWER);
        setUniform("directionalLight.shadowEnable", settings.STATIC_SHADOW_RESOLUTION > 0);

        setUniform("hasTexture", false);
        setUniform("hasColor", false);

        setUniform("lineColor", Color4f.WHITE);

        // Texture for the model
        setUniform("texture_sampler", 0);
        setUniform("staticShadowMap", 1);
        setUniform("dynamicShadowMap", 2);

        GenericTextures.CHECKER.bind(GL_TEXTURE0);
        GenericTextures.CHECKER.bind(GL_TEXTURE1);
        GenericTextures.CHECKER.bind(GL_TEXTURE2);

        nextLightIndex = 0;
        discardRemainingLights();
        nextLightIndex = 0;
    }

    @Override
    public void setPointLight(Vector3fc position, Color4f color, float intensity) {
        int lightNumber = nextLightIndex++;
        setUniform(("pointLights[" + lightNumber + "]") + ".color", color.rawVector3f());
        setUniform(("pointLights[" + lightNumber + "]") + ".mPosition", position);
        setUniform(("pointLights[" + lightNumber + "]") + ".intensity", color.alpha * intensity);
    }

    @Override
    public void setDirectionalLight(DirectionalLight light) {
        Color4f color = light.getColor();
        setUniform("directionalLight.color", color.rawVector3f());
        setUniform("directionalLight.direction", light.getDirection());
        setUniform("directionalLight.intensity", color.alpha * light.getIntensity());
        setUniform("directionalLight.lightSpaceMatrix", light.getLightSpaceMatrix());

        // Static Shadows
        if (light.doStaticShadows()) {
            ShadowMap staticShadowMap = light.getStaticShadowMap();
            staticShadowMap.bind(GL_TEXTURE1);
        }

        // Dynamic Shadows
        if (light.doDynamicShadows()) {
            ShadowMap dynamicShadowMap = light.getDynamicShadowMap();
            dynamicShadowMap.bind(GL_TEXTURE2);
        }
    }

    @Override
    public void setMaterial(Color4f diffuse, Color4f specular, float reflectance) {
        setUniform("material.diffuse", diffuse);
        setUniform("material.specular", specular);
        setUniform("material.reflectance", reflectance);
    }

    @Override
    public void setTexture(Texture tex) {
        if (tex != null) {
            setUniform("hasTexture", true);
            tex.bind(GL_TEXTURE0);

        } else {
            unsetTexture();
        }
    }

    @Override
    public void unsetTexture() {
        glBindTexture(GL_TEXTURE_2D, 0);
        setUniform("hasTexture", false);
    }

    @Override
    public void discardRemainingLights() {
        while (nextLightIndex < MAX_POINT_LIGHTS) {
            setPointLight(new Vector3f(), Color4f.INVISIBLE, 0);
        }
    }
}
