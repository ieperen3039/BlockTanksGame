package NG.Tools;

import NG.Blocks.BlocksConstruction;
import NG.CollisionDetection.BoundingBox;
import NG.DataStructures.Vector3fx;
import NG.DataStructures.Vector3fxc;
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
        fluidDensity = 3f; // water
        fluidLevel = fluidHeight;
    }

    public void addPointVolume(Vector3fc center, float volume) {
        if (center.z() < fluidLevel) {
            if (volumeTotal == 0) {
                centerOfVolume.set(center);
                volumeTotal = volume;

            } else {
                // weighted sum
                centerOfVolume.mul(volumeTotal)
                        .add(new Vector3f(center).mul(volume));
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
        float torque = (floatForce * BlocksConstruction.UNIT_MASS_TO_GRAVITY) / distSq;
        vecMassToVolume.normalize().cross(Vectors.Z).mul(torque / mass); // probably wrong, but close enough
        Logger.WARN.print(vecMassToVolume, distSq, torque);
        return vecMassToVolume;
    }

    /**
     * @return the magnitude of the upward float force in units of 1-block weight
     */
    public float getFloatForce() {
        float pressure = fluidDensity * (fluidLevel - centerOfVolume.z);
        return volumeTotal * pressure;
    }

    public void addAABB(BoundingBox box, float volume) {
        Vector3f center = box.getMinimum().lerp(box.getMaximum(), 0.5f);
        if (box.maxZ < fluidLevel) {
            addPointVolume(center, volume);

        } else if (box.minZ < fluidLevel) {

            float submergedPart = fluidLevel - box.minZ;
            float submergedFraction = (box.maxZ - box.minZ) / (submergedPart);
            center.z = -submergedPart;
            addPointVolume(center, volume * submergedFraction);
        }
    }
}
