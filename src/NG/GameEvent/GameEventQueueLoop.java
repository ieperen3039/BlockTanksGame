package NG.GameEvent;

import NG.CollisionDetection.GameState;
import NG.Core.AbstractGameLoop;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.Storable;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An object similar to {@link GameEventQueue}, but now the queue is evaluated once every game tick. This allows for
 * external updates to the queue, as with user interaction.
 * @author Geert van Ieperen created on 14-2-2019.
 */
public class GameEventQueueLoop extends AbstractGameLoop implements Storable, EventLoop {
    private final PriorityQueue<Event> eventQueue; // requires explicit synchronisation
    private final Lock lockQueueRead;
    private final Lock lockQueueEdit;
    private Game game;
    private float updateTime;

    /**
     * creates a new, paused event loop
     * @param name
     * @param targetTps the target number of executions of {@link #update(float)} per second
     */
    public GameEventQueueLoop(String name, int targetTps) {
        super(name, targetTps);
        eventQueue = new PriorityQueue<>();
        ReadWriteLock rwl = new ReentrantReadWriteLock(false);
        lockQueueRead = rwl.readLock();
        lockQueueEdit = rwl.writeLock();
    }

    @Override
    public void init(Game game) throws Exception {
        this.game = game;
        updateTime = game.get(GameTimer.class).getGametime();
    }

    @Override
    protected void update(float deltaTime) throws Exception {
        GameTimer timer = game.get(GameTimer.class);
        GameState state = game.get(GameState.class);

        timer.updateGameTime();
        float gameTime = timer.getGametime();

        if (eventQueue.isEmpty()) {
            state.update(gameTime);
            return;
        }

        lockQueueRead.lock();
        Event next = eventQueue.element();
        lockQueueRead.unlock();

        float eventTime = next.getTime();

        while (eventTime < gameTime) {
            if (eventTime > updateTime) {
                state.update(eventTime);
                updateTime = eventTime;

                lockQueueRead.lock();
                next = eventQueue.element();
                lockQueueRead.unlock();
            }

            /* if state had to be updated, and a new event was generated,
             * then the state has already been updated past this new event.
             */
            next.run();

            lockQueueEdit.lock();
            eventQueue.remove();

            if (eventQueue.isEmpty()) {
                lockQueueEdit.unlock();
                break;
            }

            next = eventQueue.element();
            lockQueueEdit.unlock();

            eventTime = next.getTime();
        }

        state.update(gameTime);
    }

    @Override
    public void addEvent(Event e) {
        assert e.getTime() >= updateTime;
        lockQueueEdit.lock();
        try {
            eventQueue.add(e);

        } finally {
            lockQueueEdit.unlock();
        }
    }

    @Override
    public void cleanup() {
        boolean b = lockQueueEdit.tryLock();
        eventQueue.clear();
        if (b) lockQueueEdit.unlock();
    }

    @Override
    public void writeToDataStream(DataOutputStream out) throws IOException {

    }
}
