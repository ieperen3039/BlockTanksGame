package NG.Rendering.Shaders;

import NG.Camera.Camera;
import NG.Core.Game;
import NG.DataStructures.Generic.Color4f;
import NG.Rendering.GLFWWindow;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MatrixStack.SceneShaderGL;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import org.joml.*;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.FloatBuffer;
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

    /**
     * Set the value of a 4x4 matrix shader uniform.
     * @param uniformName The name of the uniform.
     * @param value       The new value of the uniform.
     */
    protected void setUniform(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Dump the matrix into a float buffer
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(unif(uniformName), false, fb);
        }
    }

    /**
     * Set the value of a 3x3 matrix shader uniform.
     * @param uniformName The name of the uniform.
     * @param value       The new value of the uniform.
     */
    protected void setUniform(String uniformName, Matrix3f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Dump the matrix into a float buffer
            FloatBuffer fb = stack.mallocFloat(9);
            value.get(fb);
            glUniformMatrix3fv(unif(uniformName), false, fb);
        }
    }

    /**
     * Set the value of a certain integer shader uniform
     * @param uniformName The name of the uniform.
     * @param value       The new value of the uniform.
     */
    protected void setUniform(String uniformName, int value) {
        glUniform1i(unif(uniformName), value);
    }

    /**
     * Set the value of a certain float shader uniform
     * @param uniformName The name of the uniform.
     * @param value       The new value of the uniform.
     */
    protected void setUniform(String uniformName, float value) {
        glUniform1f(unif(uniformName), value);
    }

    /**
     * Set the value of a certain 3D Vector shader uniform
     * @param uniformName The name of the uniform.
     * @param value       The new value of the uniform.
     */
    public void setUniform(String uniformName, Vector3fc value) {
        glUniform3f(unif(uniformName), value.x(), value.y(), value.z());
    }

    /**
     * Set the value of a certain 2D Vector shader uniform
     * @param uniformName The name of the uniform.
     * @param value       The new value of the uniform.
     */
    protected void setUniform(String uniformName, Vector2fc value) {
        glUniform2f(unif(uniformName), value.x(), value.y());
    }

    private int unif(String uniformName) {
        try {
            return uniforms.get(uniformName);
        } catch (NullPointerException ex) {
            throw new ShaderException("Uniform '" + uniformName + "' does not exist");
        }
    }

    protected void setUniform(String uniformName, float[] value) {
        glUniform4f(unif(uniformName), value[0], value[1], value[2], value[3]);
    }

    /**
     * Set the value of a certain 4D Vector shader uniform
     * @param uniformName The name of the uniform.
     * @param value       The new value of the uniform.
     */
    protected void setUniform(String uniformName, Vector4fc value) {
        glUniform4f(unif(uniformName), value.x(), value.y(), value.z(), value.w());
    }

    protected void setUniform(String uniformName, boolean value) {
        setUniform(uniformName, value ? 1 : 0);
    }

    protected void setUniform(String uniformName, Color4f color) {
        glUniform4f(unif(uniformName), color.red, color.green, color.blue, color.alpha);
    }

    public void setProjectionMatrix(Matrix4f viewProjectionMatrix) {
        setUniform("viewProjectionMatrix", viewProjectionMatrix);
    }

    public void setModelMatrix(Matrix4f modelMatrix) {
        setUniform("modelMatrix", modelMatrix);
    }

    public void setNormalMatrix(Matrix3f normalMatrix) {
        setUniform("normalMatrix", normalMatrix);
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
}
