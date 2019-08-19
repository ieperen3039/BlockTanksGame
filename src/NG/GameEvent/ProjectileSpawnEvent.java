package NG.GameEvent;

import NG.CollisionDetection.GameState;
import NG.Core.Game;
import NG.DataStructures.Vector3fx;
import NG.Entities.FixedState;
import NG.Entities.Projectiles.Projectile;
import NG.Entities.State;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Supplier;

/**
 * @author Geert van Ieperen created on 19-4-2019.
 */
public class ProjectileSpawnEvent extends Event {
    private final Game game;
    private Projectile elt;
    private State spawnPosition;
    private final Supplier<Boolean> validity;

    /**
     * @param elt           the projectile to be spawned
     * @param eventTime     the time of spawning in seconds
     * @param validity      a function that checks whether this spawn is still valid on the current game time
     * @param position
     */
    public ProjectileSpawnEvent(
            Game game, Projectile elt, float eventTime,
            Supplier<Boolean> validity, Vector3fx position
    ) {
        super(eventTime);
        this.game = game;
        this.elt = elt;
        this.spawnPosition = new FixedState(position, new Quaternionf(), eventTime);
        this.validity = validity;
    }

    @Override
    public void run() {
        if (validity.get()) {
            elt.setState(spawnPosition);
            game.get(GameState.class).addEntity(elt);
        }
    }

    /**
     * creates and schedules the spawning of a projectile on the given spawn time
     * @see #ProjectileSpawnEvent(Game, Projectile, Vector3fc, float, Supplier)
     */
    public static void create(
            Game game, Projectile projectile, float spawnTime, Vector3f spawnPosition
    ) {
        Event e = new ProjectileSpawnEvent(game, projectile, spawnTime, () -> true, new Vector3fx(spawnPosition));
        game.get(EventLoop.class).addEvent(e);
    }
}
