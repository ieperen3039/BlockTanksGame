package NG.Particles;

import NG.DataStructures.Generic.Color4f;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Tools.Toolbox;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * a group of particles, each rendered around the same time
 * @author Geert van Ieperen created on 16-5-2018.
 */
public class ParticleCloud implements Mesh {

    private static final float PARTICLECLOUD_MIN_TIME = 0.5f;
    private static final int PARTICLECLOUD_SPLIT_SIZE = 1000;
    private int vaoId;
    private int posMidVboID;
    private int moveVboID;
    private int colorVboID;
    private int ttlVboID;
    private int randVboID;

    private ArrayList<Particle> bulk = new ArrayList<>();
    private float maxTTL = 0;
    private float minTTL = Float.MAX_VALUE;
    private int nrOfParticles;
    private float startTime = -1;

    /**
     * @param position     position of the middle of the particle
     * @param color        color of this particle
     * @param movement     movement of this particle in one second
     * @param maxTTL   maximum duration of this particle
     */
    public void addParticle(
            Vector3fc position, Vector3fc movement, Color4f color, float maxTTL
    ) {

        final float randFloat = Toolbox.random.nextFloat();
        float timeToLive = randFloat * randFloat * maxTTL;

        addParticle(new Particle(position, movement, color, timeToLive));
    }

    private void addParticle(Particle p) {
        bulk.add(p);
        maxTTL = Math.max(maxTTL, p.timeToLive);
        minTTL = Math.min(minTTL, p.timeToLive);
    }

    /**
     * @param currentTime initial time in seconds
     */
    public void writeToGL(float currentTime) {
        int numElts = bulk.size();
        maxTTL += currentTime;
        minTTL += currentTime;

        nrOfParticles = numElts;
        startTime = currentTime;

        FloatBuffer positionBuffer = MemoryUtil.memAllocFloat(3 * numElts);
        FloatBuffer moveBuffer = MemoryUtil.memAllocFloat(3 * numElts);
        FloatBuffer colorBuffer = MemoryUtil.memAllocFloat(4 * numElts);
        FloatBuffer ttlBuffer = MemoryUtil.memAllocFloat(2 * numElts);

        IntBuffer randomBuffer = MemoryUtil.memAllocInt(numElts);
        randomBuffer.put(new Random(Toolbox.random.nextInt())
                .ints(numElts)
                .toArray());

        for (int i = 0; i < bulk.size(); i++) {
            Particle p = bulk.get(i);

            p.position.get(i * 3, positionBuffer);
            p.movement.get(i * 3, moveBuffer);
            p.color.put(colorBuffer);
            ttlBuffer.put(currentTime);
            ttlBuffer.put(currentTime + p.timeToLive);
        }

        colorBuffer.flip();
        ttlBuffer.flip();
        randomBuffer.flip();

        try {
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            posMidVboID = loadToGL(positionBuffer, 0, 3); // Position of triangle middle VBO
            moveVboID = loadToGL(moveBuffer, 1, 3); // Movement VBO
            colorVboID = loadToGL(colorBuffer, 2, 4); // Color VBO
            ttlVboID = loadToGL(ttlBuffer, 3, 2); // beginTime-maxTTL VBO

            randVboID = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, randVboID);
            glBufferData(GL_ARRAY_BUFFER, randomBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(4, 1, GL_INT, false, 0, 0);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);

        } finally {
            MemoryUtil.memFree(positionBuffer);
            MemoryUtil.memFree(moveBuffer);
            MemoryUtil.memFree(colorBuffer);
            MemoryUtil.memFree(ttlBuffer);
            MemoryUtil.memFree(randomBuffer);
        }

        Toolbox.checkGLError();
        bulk = null;
    }

    /** a rough estimate of the number of visible particles */
    public int estParticlesAt(float currentTime) {
        if (bulk != null) return 0;

        float fraction = 1 - ((currentTime - startTime) / (maxTTL - minTTL));
        if (fraction < 0) return 0;
        if (fraction > 1) fraction = 1;

        return (int) (nrOfParticles * fraction * fraction); // assuming quadratic despawn
    }

    public Stream<ParticleCloud> granulate() {
        float despawnPeriod = maxTTL - minTTL;

        if ((despawnPeriod > PARTICLECLOUD_MIN_TIME) && (bulk.size() > PARTICLECLOUD_SPLIT_SIZE)) {
            float mid = minTTL + (despawnPeriod / 2);

            ParticleCloud newCloud = splitOff(mid);
            return Stream.concat(this.granulate(), newCloud.granulate());
        }

        return Stream.of(this);
    }

    /**
     * splits off all particles with timeToLive more than the given TTL. The new particles are written to GL
     * @param newTTL the new maximum ttl of these particles
     */
    public ParticleCloud splitOff(float newTTL) {
        ParticleCloud newCloud = new ParticleCloud();
        ArrayList<Particle> newBulk = new ArrayList<>();

        for (Particle p : bulk) {
            if (p.timeToLive > newTTL) {
                newCloud.addParticle(p);
            } else {
                newBulk.add(p);
            }
        }

        maxTTL = newTTL;
        bulk = newBulk;

        return newCloud;
    }

    private static int loadToGL(FloatBuffer buffer, int index, int itemSize) {
        int vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        glVertexAttribPointer(index, itemSize, GL_FLOAT, false, 0, 0);
        return vboID;
    }

    /**
     * renders all particles. The particle-shader must be linked first, and writeToGl must be called
     */
    @Override
    public void render(SGL.Painter lock) {
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0); // Position of triangle middle VBO
        glEnableVertexAttribArray(1); // Movement VBO
        glEnableVertexAttribArray(2); // Color VBO
        glEnableVertexAttribArray(3); // TTL VBO
        glEnableVertexAttribArray(4); // Rand VBO

        glDrawArrays(GL_POINTS, 0, nrOfParticles);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);
        glDisableVertexAttribArray(4);
        glBindVertexArray(0);
    }

    public boolean hasFaded(float currentTime) {
        return currentTime > startTime + maxTTL;
    }

    public boolean disposeIfFaded(float currentTime) {
        if (hasFaded(currentTime)) {
            dispose();
            return true;
        }
        return false;
    }

    public void dispose() {
        glDisableVertexAttribArray(0);

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(posMidVboID);
        glDeleteBuffers(moveVboID);
        glDeleteBuffers(colorVboID);
        glDeleteBuffers(ttlVboID);
        glDeleteBuffers(randVboID);

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    /**
     * merges the other queued particles into this particle Note! this only works before {@link #writeToGL(float)} is
     * called
     * @param other another particle cloud. The other will not be modified
     * @throws NullPointerException if this or the other has called {@link #writeToGL(float)} previously
     */
    public void addAll(ParticleCloud other) {
        this.bulk.addAll(other.bulk);
        this.maxTTL = Math.max(this.maxTTL, other.maxTTL);
        this.minTTL = Math.min(this.minTTL, other.minTTL);
    }

    /**
     * @return true if particles will be loaded to the GPU after a call to writeToGl() This returns false if there are
     * no particles loaded, or writeToGl has already been called
     */
    public boolean readyToLoad() {
        return (bulk != null) && !bulk.isEmpty();
    }

    /**
     * record class for particles
     */
    private class Particle {
        public final Vector3fc position;
        public final Vector3fc movement;
        public final Color4f color;
        public final float timeToLive;

        public Particle(Vector3fc position, Vector3fc movement, Color4f color, float timeToLive) {
            this.position = position;
            this.movement = movement;
            this.color = color;
            this.timeToLive = timeToLive;
        }
    }
}
