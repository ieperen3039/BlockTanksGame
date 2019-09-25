package NG.Rendering.Shaders;

import NG.Core.Game;
import NG.DataStructures.Generic.Color4f;
import NG.Rendering.MatrixStack.SGL;
import org.joml.*;
import org.lwjgl.system.MemoryStack;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;

import static org.lwjgl.opengl.GL20.*;

/**
 * @author Geert van Ieperen created on 7-1-2018.
 */
public interface ShaderProgram {

    /** shaders and meshes must use these shader locations */
    int VERTEX_LOCATION = 0;
    int NORMAL_LOCATION = 1;
    int COLOR_LOCATION = 2;
    int TEXTURE_LOCATION = 3;

    /**
     * Bind the renderer to the current rendering state
     */
    void bind();

    /**
     * Unbind the renderer from the current rendering state
     */
    void unbind();

    /**
     * Cleanup the renderer to a state of disposal
     */
    void cleanup();

    /**
     * initialize the uniforms for this shader. Must be called before rendering.
     * @param game the source of information
     */
    void initialize(Game game);

    /**
     * Set the value of a 4x4 matrix shader uniform.
     * @param uniformName The name of the uniform.
     * @param value       The new value of the uniform.
     */
    default void setUniform(String uniformName, Matrix4f value) {
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
    default void setUniform(String uniformName, Matrix3f value) {
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
    default void setUniform(String uniformName, int value) {
        glUniform1i(unif(uniformName), value);
    }

    /**
     * Set the value of a certain float shader uniform
     * @param uniformName The name of the uniform.
     * @param value       The new value of the uniform.
     */
    default void setUniform(String uniformName, float value) {
        glUniform1f(unif(uniformName), value);
    }

    /**
     * Set the value of a certain 3D Vector shader uniform
     * @param uniformName The name of the uniform.
     * @param value       The new value of the uniform.
     */
    default void setUniform(String uniformName, Vector3fc value) {
        glUniform3f(unif(uniformName), value.x(), value.y(), value.z());
    }

    /**
     * Set the value of a certain 2D Vector shader uniform
     * @param uniformName The name of the uniform.
     * @param value       The new value of the uniform.
     */
    default void setUniform(String uniformName, Vector2fc value) {
        glUniform2f(unif(uniformName), value.x(), value.y());
    }

    int unif(String uniformName);

    default void setUniform(String uniformName, float[] value) {
        glUniform4f(unif(uniformName), value[0], value[1], value[2], value[3]);
    }

    /**
     * Set the value of a certain 4D Vector shader uniform
     * @param uniformName The name of the uniform.
     * @param value       The new value of the uniform.
     */
    default void setUniform(String uniformName, Vector4fc value) {
        glUniform4f(unif(uniformName), value.x(), value.y(), value.z(), value.w());
    }

    default void setUniform(String uniformName, boolean value) {
        setUniform(uniformName, value ? 1 : 0);
    }

    default void setUniform(String uniformName, Color4f color) {
        glUniform4f(unif(uniformName), color.red, color.green, color.blue, color.alpha);
    }

    void setProjectionMatrix(Matrix4f viewProjectionMatrix);

    void setModelMatrix(Matrix4f modelMatrix);

    void setNormalMatrix(Matrix3f normalMatrix);

    SGL getGL(Game game);

    /**
     * Create a new shader and return the id of the newly created shader.
     * @param programId
     * @param shaderType The type of shader, e.g. GL_VERTEX_SHADER.
     * @param shaderCode The shaderCode as a String.
     * @return The id of the newly created shader.
     * @throws ShaderException If an error occurs during the creation of a shader.
     */
    static int createShader(int programId, int shaderType, String shaderCode) throws ShaderException {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new ShaderException("Error creating shader. Type: " + shaderType);
        }

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new ShaderException("Error compiling Shader code:\n" + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(programId, shaderId);

        return shaderId;
    }

    /**
     * loads a textfile and returns the text as a string
     * @param path a path to an UTF-8 text file.
     * @return a string representation of the requested resource
     * @throws IOException if the path does not point to a valid, readable file
     */
    static String loadText(Path path) throws IOException {
        String result;
        try (
                InputStream in = new FileInputStream(path.toFile());
                Scanner scanner = new Scanner(in, StandardCharsets.UTF_8)
        ) {
            result = scanner.useDelimiter("\\A").next();

        } catch (FileNotFoundException e) {
            throw new IOException("Resource not found: " + path);
        }
        return result;
    }
}
