package NG.Camera;

import NG.DataStructures.Generic.Color4f;
import NG.Rendering.Lights.PointLight;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 28-2-2019.
 */
public class CameraLight implements PointLight {
    private final Camera target;
    private final Color4f color;
    private final float intensity;

    public CameraLight(Camera target, Color4f color, float intensity) {
        this.target = target;
        this.color = color;
        this.intensity = intensity;
    }

    @Override
    public Vector3fc getPosition() {
        return target.getEye();
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
