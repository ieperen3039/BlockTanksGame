package NG.Particles;

import NG.Core.Game;
import NG.Core.GameAspect;
import NG.Core.GameTimer;
import NG.Rendering.MatrixStack.SGL;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ordinary container for particles
 * @author Geert van Ieperen created on 3-4-2019.
 */
public class GameParticles implements GameAspect {
    private final List<ParticleCloud> particles;
    private final List<ParticleCloud> newParticles;
    private final Lock newLock;
    private Game game;


    public GameParticles() {
        this.particles = new ArrayList<>();
        newParticles = new ArrayList<>();

        newLock = new ReentrantLock();
    }

    @Override
    public void init(Game game) throws Exception {
        this.game = game;
    }

    public void add(ParticleCloud cloud) {
        newLock.lock();
        try {
            newParticles.add(cloud);
        } finally {
            newLock.unlock();
        }
    }

    public void draw(SGL gl) {
        float now = game.get(GameTimer.class).getRendertime();

        particles.removeIf(cloud -> cloud.disposeIfFaded(now));

        newLock.lock();
        try {
            newParticles.stream()
                    .flatMap(ParticleCloud::granulate)
                    .peek(c -> c.writeToGL(now))
                    .forEach(particles::add);

            newParticles.clear();
        } finally {
            newLock.unlock();
        }

        for (ParticleCloud cloud : particles) {
            gl.render(cloud, null);
        }
    }

    @Override
    public void cleanup() {
        newLock.lock();
        particles.clear();
        newParticles.clear();
        newLock.unlock();
    }
}
