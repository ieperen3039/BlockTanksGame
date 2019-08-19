package NG.GameEvent;

import NG.Core.Game;
import NG.Core.GameTimer;
import NG.Tools.Logger;

import java.util.PriorityQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * a game queue that only allows adding events as result of events or when the queue is empty. Externally added events
 * are allowed only after the next event to be executed. Otherwise, the new event is delayed until after the next event.
 * Very efficient when used correctly.
 * @author Geert van Ieperen created on 13-2-2019.
 */
public class GameEventQueue extends Thread implements EventLoop {
    private final PriorityQueue<Event> eventQueue;
    private final Lock lockNewEvent;

    private boolean shouldStop = false;
    private Game game;

    public GameEventQueue() {
        super("Game Event Loop");
        eventQueue = new PriorityQueue<>();
        lockNewEvent = new ReentrantLock();
    }

    @Override
    public void init(Game game) throws Exception {
        this.game = game;
    }

    @Override
    public void run() {
        try {
            while (!eventQueue.isEmpty() && !Thread.interrupted()) {
                lockNewEvent.lock();
                Event nextEvent = eventQueue.remove();
                lockNewEvent.unlock();

                GameTimer timer = game.get(GameTimer.class);
                timer.updateGameTime();
                float gametime = timer.getGametime();
                float remainingSeconds = nextEvent.getTime() - gametime;
                long millis = (long) (remainingSeconds * 1000);
                Thread.sleep(millis);

                if (shouldStop) break;

                // do stuff
                nextEvent.run();
            }

        } catch (Exception ex) {
            Logger.ERROR.print(this + " has Crashed! " + ex);
            exceptionHandler(ex);

        } finally {
            cleanup();
        }

    }

    private void exceptionHandler(Exception ex) {
        ex.printStackTrace();
    }

    public void stopLoop() {
        shouldStop = false;
    }

    public void addEvent(Event e) {
        lockNewEvent.lock();
        try {
            eventQueue.add(e);

        } finally {
            lockNewEvent.unlock();
        }
    }

    @Override
    public void cleanup() {

    }
}
