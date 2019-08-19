package NG.Camera;

import NG.Core.GameAspect;
import NG.InputHandling.MouseScrollListener;
import NG.Settings.Settings;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

/**
 * A camera class manages movement and position of the camera. The actual implementation of creating the perspective
 * matrix is done based on the values of {@link #getEye()}, {@link #getFocus()} and {@link #getUpVector()}
 * @author Geert van Ieperen created on 29-10-2017.
 * @see StrategyCamera
 */
public interface Camera extends GameAspect, MouseScrollListener {
    /**
     * the direction vector of the eye of the camera to the focus of the camera.
     * @return {@link #getEye()}.to({@link #getFocus()}) The length of this vector may differ by implementation
     */
    Vector3fc vectorToFocus();

    /**
     * updates the state of this camera according to the given passed time.
     * @param deltaTime the number of seconds passed since last update. This may be real-time or in-game time
     * @param renderTime
     */
    void updatePosition(float deltaTime, float renderTime);

    /** @return the position of the camera itself */
    Vector3fc getEye();

    /** @return the point in space where the camera looks to */
    Vector3fc getFocus();

    /** @return the direction of up. the length of this vector is undetermined. */
    Vector3fc getUpVector();

    /** sets the camera to the given position and focus. The state of the up-vector depends on the implementation */
    void set(Vector3fc focus, Vector3fc eye);

    /**
     * Calculates a projection matrix based on a camera position and the given parameters of the viewport
     * @param aspectRatio the ratio between width and height of the projection. For screen this is width / height, both
     *                    in pixels.
     * @return a projection matrix, such that modelspace vectors multiplied with this matrix will be transformed to
     * viewspace.
     */
    default Matrix4f getViewProjection(float aspectRatio) {
        Matrix4f vpMatrix = new Matrix4f();

        if (isIsometric()) {
            float visionSize = vectorToFocus().length() - Settings.Z_NEAR;
            vpMatrix.setOrthoSymmetric(aspectRatio * visionSize, visionSize, Settings.Z_NEAR, Settings.Z_FAR);
        } else {
            vpMatrix.setPerspective(Settings.FOV, aspectRatio, Settings.Z_NEAR, Settings.Z_FAR);
        }

        // set the view
        vpMatrix.lookAt(
                getEye(),
                getFocus(),
                getUpVector()
        );

        return vpMatrix;
    }

    boolean isIsometric();
}
