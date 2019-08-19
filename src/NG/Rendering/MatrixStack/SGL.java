package NG.Rendering.MatrixStack;

import NG.Entities.Entity;
import NG.Rendering.MeshLoading.Mesh;
import NG.Rendering.Shaders.ShaderProgram;
import org.joml.Matrix4fc;

/**
 * This resembles the {@link org.lwjgl.opengl.GL} object.
 * @author Geert van Ieperen created on 15-11-2017.
 */
public interface SGL extends MatrixStack {

    /**
     * instructs the graphical card to render the specified mesh
     * @param object       A Mesh that has not been disposed.
     * @param sourceEntity the entity that is currently drawn
     */
    void render(Mesh object, Entity sourceEntity);

    /** @return the shader that is used for rendering. */
    ShaderProgram getShader();

    /** @return the view projection matrix used to render the current scene */
    Matrix4fc getViewProjectionMatrix();

    /**
     * Objects should use GPU calls only in their render method. To prevent invalid uses of the {@link
     * Mesh#render(Painter)} object, a Painter object is required to call that render method.
     */
    class Painter {
        /**
         * Objects should call GPU calls only in their render method. This render method may only be called by a GL2
         * object, to prevent drawing calls while the GPU is not initialized. For this reason, the Painter constructor
         * is protected.
         */
        protected Painter() {
        }
    }

}
