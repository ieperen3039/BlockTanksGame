package NG.Rendering.Lights;

import NG.DataStructures.Generic.Color4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * a light object, which can be either a point-light or an infinitely far light
 * @author Geert van Ieperen. Created on 18-11-2018.
 */
public class FixedPointLight implements PointLight {
    private final Vector3fc position;
    private final Color4f color;
    private final float intensity;

    public FixedPointLight(float x, float y, float z, Color4f color, float intensity) {
        this(new Vector3f(x, y, z), color, intensity);
    }

    public FixedPointLight(Vector3fc position, Color4f color, float intensity) {
        this.position = new Vector3f(position);
        this.color = color; // TODO fix brightness
        this.intensity = intensity;
    }

    @Override
    public Vector3fc getPosition() {
        return position;
    }

    @Override
    public Color4f getColor() {
        return color;
    }

    @Override
    public float getIntensity() {
        return intensity;
    }
}
