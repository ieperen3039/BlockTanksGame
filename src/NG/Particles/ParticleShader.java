package NG.Particles;

import NG.Camera.Camera;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.Entities.Entity;
import NG.Rendering.GLFWWindow;
import NG.Rendering.MatrixStack.AbstractSGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.Shaders.ShaderException;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Settings.Settings;
import NG.Tools.Directory;
import NG.Tools.Logger;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Path;

import static NG.Rendering.Shaders.ShaderProgram.createShader;
import static NG.Rendering.Shaders.ShaderProgram.loadText;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

/**
 * @author Geert van Ieperen created on 17-5-2018.
 */
public class ParticleShader implements ShaderProgram {
    private static final Path VERTEX_PATH = Directory.shaders.getPath("Particle", "vertex.vert");
    private static final Path FRAGMENT_PATH = Directory.shaders.getPath("Particle", "fragment.frag");
    private static final Path GEOMETRY_PATH = Directory.shaders.getPath("Particle", "geometry.glsl");

    private final int programId;
    private final int vertexShaderId;
    private final int fragmentShaderId;

    private final int timeUniform;
    private final int projectionUniform;
    private final int geometryShaderId;
    private final int sizeUniform;

    public ParticleShader() throws IOException {
        programId = glCreateProgram();

        final String vertexCode = loadText(VERTEX_PATH);
        vertexShaderId = createShader(programId, GL_VERTEX_SHADER, vertexCode);

        final String fragmentCode = loadText(FRAGMENT_PATH);
        fragmentShaderId = createShader(programId, GL_FRAGMENT_SHADER, fragmentCode);

        final String geometryCode = loadText(GEOMETRY_PATH);
        geometryShaderId = createShader(programId, GL_GEOMETRY_SHADER, geometryCode);

        link();

        timeUniform = glGetUniformLocation(programId, "currentTime");
        projectionUniform = glGetUniformLocation(programId, "viewProjectionMatrix");
        sizeUniform = glGetUniformLocation(programId, "particleSize");
    }

    @Override
    public void initialize(Game game) {
        GameTimer timer = game.get(GameTimer.class);
        Settings settings = game.get(Settings.class);
        glUniform1f(timeUniform, timer.getRendertime());
        glUniform1f(sizeUniform, settings.PARTICLE_SIZE);
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }

    private void link() throws ShaderException {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new ShaderException("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        glDetachShader(programId, vertexShaderId);
        glDetachShader(programId, geometryShaderId);
        glDetachShader(programId, fragmentShaderId);

        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            Logger.ERROR.print("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }
    }

    /**
     * sets the projection matrix uniform
     */
    public void setProjection(Matrix4fc matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Dump the matrix into a float buffer
            FloatBuffer fb = stack.mallocFloat(16);
            matrix.get(fb);
            glUniformMatrix4fv(projectionUniform, false, fb);
        }
    }

    public ParticleGL getGL(Game game) {
        GLFWWindow window = game.get(GLFWWindow.class);
        Camera camera = game.get(Camera.class);
        Settings settings = game.get(Settings.class);
        int windowWidth = window.getWidth();
        int windowHeight = window.getHeight();

        return new ParticleGL(camera, windowWidth, windowHeight, settings.ISOMETRIC_VIEW);
    }

    public class ParticleGL extends AbstractSGL {
        private final Matrix4f viewProjectionMatrix;

        public ParticleGL(
                Camera camera, float windowWidth, float windowHeight, boolean iso
        ) {
            viewProjectionMatrix = camera.getViewProjection((float) windowWidth / windowHeight);
        }

        @Override
        public void render(Mesh object, Entity sourceEntity) {
            setProjection(viewProjectionMatrix);
            object.render(LOCK);
        }

        @Override
        public ShaderProgram getShader() {
            return ParticleShader.this;
        }

        @Override
        public Matrix4fc getViewProjectionMatrix() {
            return viewProjectionMatrix;
        }
    }
}
