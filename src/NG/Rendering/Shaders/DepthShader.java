package NG.Rendering.Shaders;

import NG.Core.Game;
import NG.DataStructures.Generic.Color4f;
import NG.Entities.Entity;
import NG.Rendering.Lights.DirectionalLight;
import NG.Rendering.MatrixStack.AbstractSGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Tools.Directory;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static NG.Rendering.Shaders.ShaderProgram.createShader;
import static NG.Rendering.Shaders.ShaderProgram.loadText;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

/**
 * @author Geert van Ieperen created on 7-1-2019.
 */
@SuppressWarnings("Duplicates")
public class DepthShader implements ShaderProgram, LightShader {
    private static final Path VERTEX_PATH = Directory.shaders.getPath("BlinnPhong", "depth_vertex.vert");
    private static final Path FRAGMENT_PATH = Directory.shaders.getPath("BlinnPhong", "depth_fragment.frag");
    private final Map<String, Integer> uniforms;

    private int programId;
    private int vertexShaderID;
    private int fragmentShaderID;

    private boolean isDynamic;
    private DirectionalLight directionalLight;

    public DepthShader() throws ShaderException, IOException {
        this.uniforms = new HashMap<>();

        this.programId = glCreateProgram();
        if (this.programId == 0) {
            throw new ShaderException("OpenGL error: Could not create Shader");
        }

        final String vertexCode = loadText(VERTEX_PATH);
        vertexShaderID = createShader(programId, GL_VERTEX_SHADER, vertexCode);

        final String fragmentCode = loadText(FRAGMENT_PATH);
        fragmentShaderID = createShader(programId, GL_FRAGMENT_SHADER, fragmentCode);

        link();
        createUniform("lightSpaceMatrix");
        createUniform("modelMatrix");
    }

    @Override
    public void initialize(Game game) {

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

    public void link() throws ShaderException {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new ShaderException("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        glDetachShader(programId, vertexShaderID);
        glDetachShader(programId, fragmentShaderID);

        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }
    }

    /**
     * Create a new uniform and get its memory location.
     * @param uniformName The name of the uniform.
     * @throws ShaderException If an error occurs while fetching the memory location.
     */
    private void createUniform(String uniformName) throws ShaderException {
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        if (uniformLocation < 0) {
            throw new ShaderException("Could not find uniform:" + uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
    }

    public void setModelMatrix(Matrix4f modelMatrix) {
        setUniform("modelMatrix", modelMatrix);
    }

    /**
     * Set the value of a 4x4 matrix shader uniform.
     * @param uniformName The name of the uniform.
     * @param value       The new value of the uniform.
     */
    private void setUniform(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Dump the matrix into a float buffer
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
        }
    }

    public void setLightSpaceMatrix(Matrix4f lightSpaceMatrix) {
        setUniform("lightSpaceMatrix", lightSpaceMatrix);
    }

    @Override
    public void setPointLight(Vector3fc mPosition, Color4f color, float intensity) {
        // ignore
    }

    /**
     * @param dynamic if true, the dynamic map of the light will be used. If false, the static map of the light will be
     *                used
     */
    public void setDynamic(boolean dynamic) {
        isDynamic = dynamic;
    }

    @Override
    public void setDirectionalLight(DirectionalLight light) {
        directionalLight = light;
        ShadowMap shadowMap = isDynamic ? light.getDynamicShadowMap() : light.getStaticShadowMap();
        shadowMap.bindFrameBuffer();
    }

    @Override
    public void discardRemainingLights() {
        // ignore
    }

    /**
     * create a GL object that allows rendering the depth map of a scene
     * @return a GL object that renders a depth map in the frame buffer of the first light that is rendered.
     */
    public DepthGL getGL(Game game) {
        return new DepthGL();
    }

    /**
     * @author Geert van Ieperen created on 30-1-2019.
     */
    public class DepthGL extends AbstractSGL {
        @Override
        public void render(Mesh object, Entity sourceEntity) {
            setLightSpaceMatrix(directionalLight.getLightSpaceMatrix());
            setModelMatrix(getModelMatrix());

            object.render(LOCK);
        }

        @Override
        public ShaderProgram getShader() {
            return DepthShader.this;
        }

        @Override
        public Matrix4fc getViewProjectionMatrix() {
            return directionalLight.getLightSpaceMatrix();
        }

        public void cleanup() {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }
}
