package NG.Tools;

import NG.Shapes.Shape;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 22-9-2019.
 */
public class BuoyancyComputation {
    public final float fluidLevel; // height in z-axis of the fluid
    public final float fluidDensity; // in units of 1x1x1 block masses

    public Vector3f centerOfVolume = new Vector3f();
    public float volumeTotal = 0;

    public BuoyancyComputation() {
        this(0);
    }

    public BuoyancyComputation(int fluidHeight) {
        fluidDensity = 2f; // water
        fluidLevel = fluidHeight;
    }

    public void add(Shape object) {
        throw new UnsupportedOperationException();// todo super advanced volume calculation
    }

    public void addPointVolume(Vector3fc center, float volume) {
        if (center.z() < fluidLevel) {
            if (volumeTotal == 0) {
                centerOfVolume.set(center);
                volumeTotal = volume;

            } else {
                centerOfVolume.add(
                        new Vector3f(center)
                                .sub(centerOfVolume) // vec (cov -> center)
                                .mul(volume / volumeTotal) // weighted displacement
                );
                volumeTotal += volume;
            }
        }
    }

    public Vector3f getRotationXYZ(Vector3fc centerOfMass, float mass) {
        Vector3f vecMassToVolume = new Vector3f(centerOfVolume).sub(centerOfMass);
        float dx = vecMassToVolume.x;
        float dy = vecMassToVolume.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy); // assuming z is up
        float torque = centerOfVolume.length() / dist;
        vecMassToVolume.normalize(torque / mass); // probably wrong, but close enough

        return vecMassToVolume;
    }

    /**
     * @return the magnitude of the upward float force in units of 1-block weight
     */
    public float getFloatForce() {
        float pressure = fluidDensity * (fluidLevel - centerOfVolume.z);
        return volumeTotal * pressure;
    }
}
