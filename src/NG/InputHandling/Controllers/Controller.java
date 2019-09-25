package NG.InputHandling.Controllers;

/**
 * @author Geert van Ieperen created on 25-9-2019.
 */
public interface Controller {
    /**
     * updates the state of the controller.
     * @param gameTime the current game time
     */
    void update(float gameTime);

    /** return any resources associated with this controller */
    default void cleanUp(){};
}
