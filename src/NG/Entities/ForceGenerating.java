package NG.Entities;

import NG.DataStructures.Vector3fxc;
import org.joml.Vector3f;

/**
 * @author Geert van Ieperen created on 27-9-2019.
 */
public interface ForceGenerating {
    /**
     * calculates the force generated by this object, assuming it is on the given position
     * @return the force generated by this object in local space
     * @param position the position of this object
     */
    Vector3f getForce(Vector3fxc position);
}
