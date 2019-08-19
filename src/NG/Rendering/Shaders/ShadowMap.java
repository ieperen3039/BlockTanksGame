package NG.Rendering.Shaders;

import NG.Rendering.Textures.Texture;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.*;

/**
 * @author Dungeons-and-Drawings group
 */
@SuppressWarnings("Duplicates")
public class ShadowMap implements Texture {
    private final int resolution;
    private final int depthMapFBO;
    private final int depthMap;

    public ShadowMap(int resolution) {
        this.resolution = resolution;
        // Allocate Texture and FBO
        depthMapFBO = glGenFramebuffers();
        depthMap = glGenTextures();
    }

    public void init() throws ShaderException {
        // Create depth map texture
        glBindTexture(GL_TEXTURE_2D, depthMap);
        glTexImage2D(
                GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F,
                resolution, resolution,
                0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null
        );

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        // Create FBO
        glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthMap, 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        // Error Check
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new ShaderException("ShadowMap could not create FrameBuffer");
        }

        // Unbind Depth Map and FBO
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void bind(int sampler) {
        glActiveTexture(sampler);
        glBindTexture(GL_TEXTURE_2D, depthMap);
    }

    @Override
    public void cleanup() {
        // Delete resources
        glDeleteFramebuffers(depthMapFBO);
        glDeleteTextures(depthMap);
    }

    @Override
    public int getWidth() {
        return resolution;
    }

    @Override
    public int getHeight() {
        return resolution;
    }

    public int getResolution() {
        return resolution;
    }

    public void bindFrameBuffer() {
        glViewport(0, 0, resolution, resolution);
        glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO);
        glClear(GL_DEPTH_BUFFER_BIT);
    }
}
