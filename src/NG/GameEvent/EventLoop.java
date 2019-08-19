package NG.GameEvent;

import NG.Core.GameAspect;

/**
 * @author Geert van Ieperen created on 18-4-2019.
 */
public interface EventLoop extends Runnable, GameAspect {
    void addEvent(Event e);
}
