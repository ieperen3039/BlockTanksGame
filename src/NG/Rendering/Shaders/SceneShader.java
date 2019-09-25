package NG.Rendering.Shaders;

import NG.Camera.Camera;
import NG.Core.Game;
import NG.Rendering.GLFWWindow;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MatrixStack.SceneShaderGL;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

/**
 * An abstract shader that initializes a view-projection matrix, a model matrix, and a normal matrix. allows for setting
 * multiple unforms, and gives utility methods as {@link #createPointLightsUniform(String, int)}
 * @author Yoeri Poels
 * @author Geert van Ieperen
 */
public abstract class SceneShader implements ShaderProgram, MaterialShader, LightShader {

    private final Map<String, Integer> uniforms;

    private int programId;
    private int vertexShaderID;
    private int geometryShaderID;
    private int fragmentShaderID;

    /**
     * create a shader and manages the interaction of its uniforms. This initializer must be called on the main thread
     * @param vertexPath   the path to the vertex shader, or null for the standard implementation
     * @param geometryPath the path to the geometry shader, or null for the standard implementation
     * @param fragmentPath the path to the fragment shader, or null for the standard implementation
     * @throws ShaderException if a new shader could not be created for internal reasons
     * @throws IOException     if the defined files could not be found (the file is searched for in the shader folder
     *                         itself, and should exclude any first slash)
     */
    public SceneShader(Path vertexPath, Path geometryPath, Path fragmentPath) throws ShaderException, IOException {
        uniforms = new HashMap<>();

        programId = glCreateProgram();
        if (programId == 0) {
            throw new ShaderException("OpenGL error: Could not create Shader");
        }

        if (vertexPath != null) {
            final String shaderCode = ShaderProgram.loadText(vertexPath);
            vertexShaderID = ShaderProgram.createShader(programId, GL_VERTEX_SHADER, shaderCode);
        }

        if (geometryPath != null) {
            final String shaderCode = ShaderProgram.loadText(geometryPath);
            geometryShaderID = ShaderProgram.createShader(programId, GL_GEOMETRY_SHADER, shaderCode);
        }

        if (fragmentPath != null) {
            final String shaderCode = ShaderProgram.loadText(fragmentPath);
            fragmentShaderID = ShaderProgram.createShader(programId, GL_FRAGMENT_SHADER, shaderCode);
        }

        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new ShaderException("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        if (vertexShaderID != 0) {
            glDetachShader(programId, vertexShaderID);
        }

        if (geometryShaderID != 0) {
            glDetachShader(programId, geometryShaderID);
        }

        if (fragmentShaderID != 0) {
            glDetachShader(programId, fragmentShaderID);
        }

        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == GL_FALSE) {
            Logger.WARN.print("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        // Create uniforms for world and projection matrices
        createUniform("viewProjectionMatrix");
        createUniform("modelMatrix");
        createUniform("normalMatrix");

        Toolbox.checkGLError();
    }

    @Override
    public void bind() {
        glUseProgram(programId);
    }

    @Override
    public void unbind() {
        glUseProgram(0);
    }

    @Override
    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }

    /**
     * Create a new uniform and get its memory location.
     * @param uniformName The name of the uniform.
     * @throws ShaderException If an error occurs while fetching the memory location.
     */
    protected void createUniform(String uniformName) throws ShaderException {
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        if (uniformLocation < 0) {
            throw new ShaderException("Could not find uniform:" + uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
    }

    @Override
    public int unif(String uniformName) {
        try {
            return uniforms.get(uniformName);
        } catch (NullPointerException ex) {
            throw new ShaderException("Uniform '" + uniformName + "' does not exist");
        }
    }

    /**
     * Create an uniform for a point-light array.
     * @param name the name of the uniform in the shader
     * @param size The size of the array.
     * @throws ShaderException If an error occurs while fetching the memory location.
     */
    protected void createPointLightsUniform(String name, int size) throws ShaderException {
        for (int i = 0; i < size; i++) {
            try {
                createUniform((name + "[" + i + "]") + ".color");
                createUniform((name + "[" + i + "]") + ".mPosition");
                createUniform((name + "[" + i + "]") + ".intensity");

            } catch (ShaderException ex) {
                if (i == 0) {
                    throw ex;
                } else {
                    throw new IllegalArgumentException(
                            "Number of lights in shader is not equal to game value (" + (i - 1) + " instead of " + size + ")", ex);
                }
            }
        }
    }

    @Override
    public SGL getGL(Game game) {
        GLFWWindow window = game.get(GLFWWindow.class);
        Camera camera = game.get(Camera.class);
        int windowWidth = window.getWidth();
        int windowHeight = window.getHeight();
        return new SceneShaderGL(this, windowWidth, windowHeight, camera);
    }

    @Override
    public void setProjectionMatrix(Matrix4f viewProjectionMatrix) {
        setUniform("viewProjectionMatrix", viewProjectionMatrix);
    }

    @Override
    public void setModelMatrix(Matrix4f modelMatrix) {
        setUniform("modelMatrix", modelMatrix);
    }

    @Override
    public void setNormalMatrix(Matrix3f normalMatrix) {
        setUniform("normalMatrix", normalMatrix);
    }
}
