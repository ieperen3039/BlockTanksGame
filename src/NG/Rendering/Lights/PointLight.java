package NG.Rendering.Lights;

import NG.DataStructures.Generic.Color4f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 28-2-2019.
 */
public interface PointLight {
    Vector3fc getPosition();

    Color4f getColor();

    float getIntensity();
}
