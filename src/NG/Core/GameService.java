package NG.Core;

import NG.DataStructures.Generic.PairList;
import NG.Rendering.RenderLoop;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A Service-Oriented-Architecture for games, with identifiers based on classes.
 * @author Geert van Ieperen created on 2-4-2019.
 */
public class GameService implements Game {
    private Map<Class, Object> lookAsideTable = new HashMap<>();
    private RenderLoop renderer;
    private final PairList<Object, Class> elements;
    private final String mainThreadName;
    private Version version;

    /**
     * @param version the version of the game engine
     * @param mainThreadName the name of the main thread
     * @param initial an array of elements that are initially in this game
     */
    public GameService(Version version, String mainThreadName, RenderLoop renderer, Object... initial) {
        this.version = version;
        this.renderer = renderer;
        this.elements = new PairList<>(initial.length);
        this.mainThreadName = mainThreadName;

        for (Object elt : initial) {
            add(elt);
        }
    }

    @Override
    public <T> T get(Class<T> target) {
        Object found = lookAsideTable.get(target);
        if (found != null) {
            //noinspection unchecked
            return (T) found;
        }

        for (int i = 0; i < elements.size(); i++) {
            if (target.isAssignableFrom(elements.right(i))) {
                Object elt = elements.left(i);

                lookAsideTable.put(target, elt);
                //noinspection unchecked
                return (T) elt;
            }
        }

        List<String> elts = elements.stream().map(p -> p.right).map(Class::getSimpleName).collect(Collectors.toList());
        throw new NoSuchElementException(String.format("No element of %s :\n%s", target.toString(), elts));
    }

    @Override
    public <T> List<T> getAll(Class<T> target) {
        List<T> results = new ArrayList<>();

        for (int i = 0; i < elements.size(); i++) {
            if (target.isAssignableFrom(elements.right(i))) {
                Object elt = elements.left(i);

                lookAsideTable.put(target, elt);
                //noinspection unchecked
                results.add((T) elements.left(i));
            }
        }

        return results;
    }

    @Override
    public void add(Object newElement) {
        elements.add(newElement, newElement.getClass());
    }

    @Override
    public boolean remove(Object original) {
        for (int i = 0; i < elements.size(); i++) {
            if (elements.left(i) == original) { // pointer equality
                elements.remove(i);
                lookAsideTable.clear(); // we can't remove on value
                return true;
            }
        }

        return false;
    }

    @Override
    public void executeOnRenderThread(Runnable action) {
        boolean thisIsMainThread = Thread.currentThread().getName().equals(mainThreadName);

        if (thisIsMainThread) {
            action.run();

        } else {
            renderer.defer(action);
        }
    }

    @Override
    public void cleanup() {
        for (AbstractGameLoop loop : getAll(AbstractGameLoop.class)) {
            loop.stopLoop();
        }

        for (GameAspect aspect : getAll(GameAspect.class)) {
            aspect.cleanup();
        }

        elements.clear();
    }

    @Override
    public boolean has(Class<?> target) {
        if (lookAsideTable.containsKey(target)) {
            return true;
        }

        for (int i = 0; i < elements.size(); i++) {
            if (target.isAssignableFrom(elements.right(i))) {
                Object elt = elements.left(i);
                lookAsideTable.put(target, elt);

                return true;
            }
        }

        return false;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public Iterator<Object> iterator() {
        return new Iterator<>() {
            int i = 0;
            int originalSize = elements.size();

            @Override
            public boolean hasNext() {
                return i < elements.size();
            }

            @Override
            public Object next() {
                if (originalSize != elements.size()) {
                    throw new ConcurrentModificationException();
                }
                return elements.left(i++);
            }

            @Override
            public void remove() {
                elements.remove(--i);
                lookAsideTable.clear(); // we can't remove on value
                originalSize--;
            }
        };
    }
}
