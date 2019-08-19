package NG.Rendering.Lights;

import NG.Core.GameAspect;
import NG.DataStructures.Generic.Color4f;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 3-2-2019.
 */
public interface GameLights extends GameAspect, Storable {
    /**
     * adds a point-light to the game.
     * @param light the new light
     */
    void addPointLight(PointLight light);

    /**
     * initializes the lights on the scene.
     * @param gl the current gl object
     */
    void draw(SGL gl);

    /**
     * set the parameters of the one infinitely-far light source of the scene.
     * @param origin    a vector TO the light source
     * @param color     the color of the light source
     * @param intensity the light intensity
     */
    void addDirectionalLight(Vector3fc origin, Color4f color, float intensity);

    /**
     * update the light behaviour and shadows
     */
    void renderShadowMaps();
}
