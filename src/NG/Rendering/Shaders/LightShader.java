package NG.Rendering.Shaders;

import NG.DataStructures.Generic.Color4f;
import NG.Rendering.Lights.DirectionalLight;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 1-2-2019.
 */
public interface LightShader {
    /**
     * pass a pointlight to the shader
     * @param mPosition the position in model-space (worldspace)
     * @param color     the color of the light, with alpha as intensity
     * @param intensity the light intensity of the light
     */
    void setPointLight(Vector3fc mPosition, Color4f color, float intensity);

    /**
     * pass an infinitely far away light to the shader
     * @param light a light with parameters
     */
    void setDirectionalLight(DirectionalLight light);

    /**
     * sets possible unused point-light slots to 'off'. No more point lights can be added after a call to this method.
     */
    void discardRemainingLights();
}
