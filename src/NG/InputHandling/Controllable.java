package NG.InputHandling;

import NG.InputHandling.Controllers.Controller;

import java.util.function.Supplier;

/**
 * @author Geert van Ieperen created on 28-9-2019.
 */
public interface Controllable {
    void setController(Supplier<Controller> controller);
}
