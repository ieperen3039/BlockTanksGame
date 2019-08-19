package NG.GameEvent;

import NG.Core.Game;
import NG.Core.GameTimer;
import NG.Tools.Logger;
import NG.Tools.Toolbox;

/**
 * a generic event that happens on a predetermined time. Triggering of the event should result in a stimulus being
 * broadcasted.
 */
public abstract class Event implements Comparable<Event>, Runnable {
    protected final float eventTime;

    /**
     * @param eventTime the time of activation in seconds
     */
    public Event(float eventTime) {
        this.eventTime = eventTime;
    }

    /**
     * @return the time of activation in seconds
     */
    public float getTime() {
        return eventTime;
    }

    @Override
    public int compareTo(Event other) {
        return Float.compare(eventTime, other.eventTime);
    }

    /**
     * an event that does nothing but triggering a debug statement. This may schedule itself repetitively
     */
    public static class DebugEvent extends Event {
        private final float recurrence;
        private Game game;
        private final int id;
        private static int nextID = 0;

        /**
         * an event of a debug statement
         * @param game       the game instance
         * @param eventTime  the time to schedule the event
         * @param recurrence the recurrence period, or -1 if no recurrence is wanted. The actual recurrence period is up
         *                   to 1 above the given value.
         */
        public DebugEvent(Game game, float eventTime, float recurrence) {
            super(eventTime);
            Logger.DEBUG.printFrom(2, "Scheduled debug timer at " + eventTime);
            this.game = game;
            this.recurrence = recurrence;
            this.id = nextID++;
        }

        @Override
        public void run() {
            Logger.DEBUG.print(this + " triggered at " + game.get(GameTimer.class).getGametime());
            if (recurrence < 0) return;

            float next = Toolbox.randomBetween(getTime() + recurrence, getTime() + recurrence + 1);
            DebugEvent nextEvent = new DebugEvent(game, next, recurrence);
            game.get(EventLoop.class).addEvent(nextEvent);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " " + id;
        }
    }
}
