package NG.Camera;

import NG.Core.Game;
import NG.InputHandling.KeyPressListener;
import NG.InputHandling.KeyReleaseListener;
import NG.InputHandling.MousePositionListener;
import NG.InputHandling.MouseToolCallbacks;
import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.glfw.GLFW;

/**
 * @author Geert van Ieperen
 *         created on 5-11-2017.
 * The standard camera that rotates using dragging
 * some of the code originates from the RobotRace sample code of the TU/e
 */
public class PointCenteredCamera implements Camera, MousePositionListener, KeyPressListener, KeyReleaseListener {
    private static final boolean INVERT_CAMERA_ROTATION = false;

    private static final float ZOOM_SPEED = -0.1f;
    private static final float THETA_MIN = 0.01f;
    private static final float THETA_MAX = ((float) Math.PI) - 0.01f;
    private static final float PHI_MAX = (float) (2 * Math.PI);
    // Ratio of distance in pixels dragged and radial change of camera.
    private static final float DRAG_PIXEL_TO_RADIAN = -0.025f;

    /**
     * The point to which the camera is looking.
     */
    public final Vector3f focus;

    /** we follow the ISO convention. Phi gives rotation, theta the height */
    private float theta;
    private float phi;
    private float vDist = 10f;

    /** cached eye position */
    private Vector3f eye;
    private MouseToolCallbacks callbacks;
    private int xPos;
    private int yPos;
    private boolean isHeld = false;

    public PointCenteredCamera() {
        this(Vectors.newZero(), 0, 0);
    }

    public PointCenteredCamera(Vector3f eye, Vector3f focus){
        Vector3f focToEye = new Vector3f(eye).sub(focus);

        vDist = focToEye.length();
        phi = getPhi(focToEye);
        theta = getTheta(focToEye, vDist);

        this.focus = focus;
        this.eye = eye;
    }

    @Override
    public void onScroll(float value) {
        vDist *= (ZOOM_SPEED * value) + 1f;
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == GLFW.GLFW_MOUSE_BUTTON_LEFT){
            isHeld = true;
        }
    }

    @Override
    public void keyReleased(int keyCode) {
        if (keyCode == GLFW.GLFW_MOUSE_BUTTON_LEFT){
            isHeld = false;
        }
    }

    /**
     * @param eye normalized vector to eye
     * @return phi
     */
    private static float getPhi(Vector3fc eye) {
        return (float) Math.atan2(eye.y(), eye.x());
    }

    /**
     * @param eye normalized vector to eye
     * @param vDist distance to origin
     * @return theta
     */
    private static float getTheta(Vector3fc eye, float vDist) {
        return (float) Math.acos(eye.z() / vDist);
    }

    public PointCenteredCamera(Vector3f focus, float theta, float phi) {
        this.focus = focus;
        this.theta = theta;
        this.phi = phi;
    }

    @Override
    public void init(Game game) throws Exception {
        callbacks = game.get(MouseToolCallbacks.class);
        callbacks.addMousePositionListener(this);
        callbacks.addKeyPressListener(this);
        callbacks.addKeyReleaseListener(this);
    }

    @Override
    public void cleanup() {
        callbacks.removeListener(this);
    }

    private Vector3f getEyePosition() {

        double eyeX = vDist * Math.sin(theta) * Math.cos(phi);
        double eyeY = vDist * Math.sin(theta) * Math.sin(phi);
        double eyeZ = vDist * Math.cos(theta);

        final Vector3f eye = new Vector3f((float) eyeX, (float) eyeY, (float) eyeZ);
        return eye.add(focus, eye);
    }

    public void mouseMoved(int newX, int newY) {
        int deltaX = newX - xPos;
        int deltaY = newY - yPos;

        this.xPos = newX;
        this.yPos = newY;

        if (!isHeld) return;
        int s = INVERT_CAMERA_ROTATION ? -1 : 1;

        theta += deltaY * DRAG_PIXEL_TO_RADIAN * s;
        phi += deltaX * DRAG_PIXEL_TO_RADIAN * s;

        theta = Math.max(THETA_MIN, Math.min(THETA_MAX, theta));
        phi = phi % PHI_MAX;
    }

    @Override
    public Vector3fc vectorToFocus(){
        return new Vector3f(focus).sub(eye);
    }

    @Override
    public void updatePosition(float deltaTime, float renderTime) {
        eye = getEyePosition();
    }

    @Override
    public Vector3fc getEye() {
        return eye;
    }

    @Override
    public Vector3fc getFocus() {
        return focus;
    }

    @Override
    public Vector3fc getUpVector() {
        return Vectors.Z;
    }

    @Override
    public void set(Vector3fc focus, Vector3fc eye) {

    }

    @Override
    public boolean isIsometric() {
        return false;
    }
}
