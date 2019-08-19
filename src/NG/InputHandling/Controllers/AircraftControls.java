package NG.InputHandling.Controllers;

/**
 * A controller for plane controls.
 * The controller
 *
 * @author Geert van Ieperen
 *         created on 31-10-2017.
 */
public interface AircraftControls {
    AircraftControls EMPTY = new EmptyController();

    /**
     * updates the state of the controller.
     * <p>
     * May only be called in the main thread.
     */
    void update();

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
     * the amount of pitch requested by this controller.
     * a positive pitch makes the nose move up
     * Values out of the range [-1, 1] do not occur (should be taken care of in implementation).
     * @return the desired roll as fraction [-1, 1].
     *          If (return < 0) the controller wants to pitch up,
     *          if (return = 0) the controller wants to hold direction,
     *          if (return > 0) the controller wants to pitch down.
     */
    float pitch();

    /**
     * the amount of yaw requested by this controller.
     * Values out of the range [-1, 1] do not occur (should be taken care of in implementation).
     * @return the desired pitch as fraction [-1, 1].
     *          If (return < 0) the controller wants to turn left,
     *          if (return = 0) the controller wants to hold rotation,
     *          if (return > 0) the controller wants to turn right.
     */
    float yaw();

    /**
     * the amount of clockwise roll requested by this controller.
     * Values out of the range [-1, 1] do not occur (should be taken care of in implementation).
     * @return the desired yaw as fraction [-1, 1].
     *          If (return < 0) the controller wants to roll clockwise,
     *          if (return = 0) the controller wants to hold rotation,
     *          if (return > 0) the controller wants to roll counterclockwise.
     */
    float roll();

    /**
     * @return whether primary fire should be activated
     */
    boolean primaryFire();

    /**
     * @return whether secondary fire should be activated
     */
    boolean secondaryFire();

    /** return any resources associated with this controller */
    default void cleanUp() {
    }

    /**
     * a controller that does nothing
     */
    class EmptyController implements AircraftControls {
        @Override
        public void update() {
        }

        @Override
        public float throttle() {
            return 0;
        }

        @Override
        public float pitch() {
            return 0;
        }

        @Override
        public float yaw() {
            return 0;
        }

        @Override
        public float roll() {
            return 0;
        }

        @Override
        public boolean primaryFire() {
            return false;
        }

        @Override
        public boolean secondaryFire() {
            return false;
        }
    }
}
