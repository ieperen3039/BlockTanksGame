package NG.Core;


import NG.DataStructures.Tracked.TrackedFloat;

/**
 * A combination of a gameloop timer and a render timer. The timers are updated on calls to {@link #updateGameTime()}
 * and {@link #updateRenderTime()}
 */
@SuppressWarnings("WeakerAccess")
public class GameTimer {

    /** game-seconds since creating this gametimer */
    protected float currentInGameTime;
    /** last record of system time */
    private long lastMark;
    /** multiplication factor to multiply system time units to game-seconds */
    private static final float MUL_TO_SECONDS = 1E-9f;

    protected final TrackedFloat gameTime;
    protected final TrackedFloat renderTime;
    protected boolean isPaused = false;
    private final float renderDelay;

    public GameTimer(float renderDelay) {
        this(0f, renderDelay);
    }

    public GameTimer(float startTime, float renderDelay) {
        this.currentInGameTime = startTime;
        this.gameTime = new TrackedFloat(startTime);
        this.renderDelay = renderDelay;
        this.renderTime = new TrackedFloat(startTime - renderDelay);
        this.lastMark = System.nanoTime();
    }

    public void updateGameTime() {
        updateTimer();
        gameTime.update(currentInGameTime);
    }

    public void updateRenderTime() {
        updateTimer();
        renderTime.update(currentInGameTime - renderDelay);
    }

    public float getGametime() {
        return gameTime.current();
    }

    public float getGametimeDifference() {
        return gameTime.difference();
    }

    public float getRendertime() {
        return renderTime.current();
    }

    public float getRendertimeDifference() {
        return renderTime.difference();
    }

    /** may be called anytime */
    protected void updateTimer() {
        long currentTime = System.nanoTime();
        float deltaTime = (currentTime - lastMark) * MUL_TO_SECONDS;
        lastMark = currentTime;

        if (!isPaused) currentInGameTime += deltaTime;
    }

    /** stops the in-game time */
    public void pause() {
        updateTimer();
        isPaused = true;
    }

    /** lets the in-game time proceed, without jumping */
    public void unPause() {
        updateTimer();
        isPaused = false;
    }

    /**
     * @param offset the ingame time is offset by the given time
     */
    public void addOffset(float offset) {
        currentInGameTime += offset;
    }

    /** sets the ingame time to the given time */
    public void set(float time) {
        updateTimer();
        currentInGameTime = time;

        updateGameTime();
        updateRenderTime();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " @" + currentInGameTime + (isPaused ? "(paused)" : "");
    }

    public boolean isPaused() {
        return isPaused;
    }
}
