package NG.Rendering.Textures;

import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL14.GL_MIRRORED_REPEAT;
import static org.lwjgl.opengl.GL44.GL_MIRROR_CLAMP_TO_EDGE;

/**
 * @author Geert van Ieperen created on 1-2-2019.
 */
public interface Texture2D {
    /**
     * activate this texture to be applied on the next model
     * @param sampler the texture slot to bind to
     */
    void bind(int sampler);

    /** destroy the resources claimed by the texture */
    void cleanup();

    int getWidth();

    int getHeight();

    enum ClampMethod {
        BORDER(GL_CLAMP_TO_BORDER),
        EDGE(GL_CLAMP_TO_EDGE),
        MIRROR(GL_MIRROR_CLAMP_TO_EDGE),
        REPEAT(GL_REPEAT),
        MIRROR_REPEAT(GL_MIRRORED_REPEAT)
        ;

        public final int glValue;

        ClampMethod(int glValue) {
            this.glValue = glValue;
        }
    }

    void setClamp(ClampMethod p);
}
