package NG.InputHandling.Controllers;

/**
 * @author Geert van Ieperen created on 25-9-2019.
 */
public interface BoatControls extends Controller {

    /**
     * the amount of throttle requested by this controller.
     * Values out of the range [-1, 1] do not occur (should be taken care of in implementation).
     * @return the desired throttle as fraction [-1, 1].
     *          If (return < 0) the controller wants to brake,
     *          if (return = 0) the controller wants to hold speed,
     *          if (return > 0) the controller wants to accelerate.
     */
    float throttle();

    /**
     * the steering/rudder requested by this controller.
     * Values out of the range [-1, 1] do not occur (should be taken care of in implementation).
     * @return the desired pitch as fraction [-1, 1].
     *          If (return < 0) the controller wants to turn left,
     *          if (return = 0) the controller wants to hold rotation,
     *          if (return > 0) the controller wants to turn right.
     */
    float steering();

    /**
     * @return the FOV multiplier as requested by the controller, as fraction of the default FOV.
     */
    float viewAngle();

    /**
     * @return true if the controller requests firing of the selected guns
     */
    boolean fire();
}
