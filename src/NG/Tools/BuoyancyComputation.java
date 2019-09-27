package NG.Tools;

import NG.CollisionDetection.BoundingBox;
import NG.DataStructures.Vector3fx;
import NG.DataStructures.Vector3fxc;
import org.joml.Vector3f;

import static NG.Settings.Settings.GRAVITY_CONSTANT;

/**
 * Assuming Z is up
 * @author Geert van Ieperen created on 22-9-2019.
 */
public class BuoyancyComputation {
    public static final float FLUID_LEVEL = 0; // height in z-axis of the fluid
    public final float fluidDensity; // in kg / m3

    public Vector3fx centerOfVolume = new Vector3fx();
    public float volumeTotal = 0;

    public BuoyancyComputation() {
        fluidDensity = 997f; // water
//        fluidDensity = 500f; // a bit heavier than plastic
    }

    public void addPointVolume(Vector3fxc center, float volume) {
        if (center.z() < FLUID_LEVEL) {
            if (volumeTotal == 0) {
                centerOfVolume.set(center);
                volumeTotal = volume;

            } else {
                // weighted sum
                centerOfVolume.mul(volumeTotal)
                        .add(new Vector3fx(center).mul(volume));
                volumeTotal += volume;
                centerOfVolume.div(volumeTotal);
            }
        }
    }

    public Vector3f getRotationXYZ(Vector3fxc centerOfMass, float mass) {
        return getRotationXYZ(centerOfMass, mass, getFloatForce());
    }

    public Vector3f getRotationXYZ(Vector3fxc centerOfMass, float mass, float floatForce) {
        Vector3f vecMassToVolume = new Vector3fx(centerOfVolume).sub(centerOfMass).toVector3f();
        float dx = vecMassToVolume.x;
        float dy = vecMassToVolume.y;

        if (dx == 0f && dy == 0f) {
            vecMassToVolume.z = 0;
            return vecMassToVolume;
        }

        float distSq = dx * dx + dy * dy; // assuming z is up
        float torque = floatForce * distSq;
        vecMassToVolume.normalize().cross(Vectors.Z).mul(torque / mass); // probably wrong, but close enough
        return vecMassToVolume;
    }

    /**
     * @return the magnitude of the upward float force in Newtons
     */
    public float getFloatForce() {
        return fluidDensity * GRAVITY_CONSTANT * volumeTotal;
    }

    public void addAABB(BoundingBox box, float volume) {
        Vector3fx center = new Vector3fx(box.getMinimum().lerp(box.getMaximum(), 0.5f));
        if (box.maxZ < FLUID_LEVEL) {
            addPointVolume(center, volume);

        } else if (box.minZ < FLUID_LEVEL) {
            center.mul(1, 1, 0).add(0, 0, -(FLUID_LEVEL - box.minZ)); // set z to -submerged
            float submergedFraction = getSubmergedFraction(box);
            addPointVolume(center, volume * submergedFraction);
        }
    }

    public float getSubmergedFraction(BoundingBox box) {
        return (FLUID_LEVEL - box.minZ) / (box.maxZ - box.minZ);
    }
}
