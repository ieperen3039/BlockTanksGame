package NG.Core;

import NG.Tools.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

/**
 * A collection of references to any major element of the game.
 * @author Geert van Ieperen. Created on 16-9-2018.
 */
public interface Game extends Iterable<Object> {

    /**
     * returns an element of the given class. If there are multiple elements, the first element is returned.
     * @param target the class that is sought
     * @param <T>    the type of this class
     * @return an element of the given target class, or null if no such element is found
     */
    <T> T get(Class<T> target);

    /**
     * returns all elements of the given class.
     * @param target the class that is sought
     * @param <T>    the type of this class
     * @return a list of all elements of the given target class, or an empty list if no such element is found.
     */
    <T> List<T> getAll(Class<T> target);

    /**
     * returns whether this Game object has at least one instance of the given class.
     * @param target the class that is sought
     * @return true iff {@link #get(Class)} would return a class
     */
    boolean has(Class<?> target);

    /**
     * adds an element to this server. Added elements can be retrieved using {@link #get(Class)}
     * @param newElement the new element
     */
    void add(Object newElement);

    /** @see #add(Object) */
    default void addAll(Object... elements) {
        for (Object elt : elements) {
            add(elt);
        }
    }

    /**
     * @param original the object to remove
     * @return true iff the element was found and removed
     */
    boolean remove(Object original);

    /**
     * if the given target class is part of this game, execute it and return true. if no such class is found, nothing is
     * executed and false is returned.
     * @param target a class that is part of this game
     * @param action an action that uses an instance of such object
     * @return {@link #has(Class) this.has(target)}
     */
    default <T> boolean ifAvailable(Class<T> target, Consumer<T> action) {
        if (has(target)) {
            action.accept(get(target));
            return true;
        }
        return false;
    }

    /**
     * @return the version of the game engine
     */
    Version getVersion();

    /**
     * Schedules the specified action to be executed in the OpenGL context. The action is guaranteed to be executed
     * before two frames have been rendered.
     * @param action the action to execute
     */
    void executeOnRenderThread(Runnable action);

    /**
     * Schedules the specified action to be executed in the OpenGL context. The action is guaranteed to be executed
     * before two frames have been rendered.
     * @param action the action to execute
     * @param <V>    the return type of action
     * @return a reference to obtain the result of the execution, or null if it threw an exception
     */
    default <V> Future<V> computeOnRenderThread(Callable<V> action) {
        FutureTask<V> task = new FutureTask<>(() -> {
            try {
                return action.call();

            } catch (Exception | AssertionError ex) {
                Logger.ERROR.print(ex);
                return null;
            }
        });

        executeOnRenderThread(task);
        return task;
    }

    /**
     * empty all elements, call the cleanup method of all gameAspect elements
     */
    void cleanup();

    default void init() throws Exception {
        for (GameAspect aspect : getAll(GameAspect.class)) {
            aspect.init(this);
        }
    }

    /**
     * a class that allows run-time switching between game instances
     */
    class Proxy implements Game {
        private Game current;

        public Proxy(Game initial) {
            current = initial;
        }

        public void select(Game target) {
            current = target;
        }

        /**
         * returns an element of the given class. If there are multiple elements, the first element is returned.
         * @param target the class that is sought
         * @param <T>    the type of this class
         * @return an element of the given target class, or null if no such element is found
         */
        public <T> T get(Class<T> target) {
            return current.get(target);
        }

        /**
         * returns all elements of the given class.
         * @param target the class that is sought
         * @param <T>    the type of this class
         * @return a list of all elements of the given target class, or an empty list if no such element is found.
         */
        public <T> List<T> getAll(Class<T> target) {
            return current.getAll(target);
        }

        /**
         * adds an element to this server. Added elements can be retrieved using {@link #get(Class)}
         * @param newElement the new element
         */
        public void add(Object newElement) {
            current.add(newElement);
        }

        /**
         * @param original the object to remove
         * @return true iff the element was found and removed
         */
        public boolean remove(Object original) {
            return current.remove(original);
        }

        @Override
        public Version getVersion() {
            return current.getVersion();
        }

        @Override
        public void executeOnRenderThread(Runnable action) {
            current.executeOnRenderThread(action);
        }

        @Override
        public void cleanup() {
            current.cleanup();
        }

        @Override
        public boolean has(Class<?> target) {
            return current.has(target);
        }

        @Override
        public <V> Future<V> computeOnRenderThread(Callable<V> action) {
            return current.computeOnRenderThread(action);
        }

        @Override
        public Iterator<Object> iterator() {
            return current.iterator();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " " + current;
        }
    }
}
