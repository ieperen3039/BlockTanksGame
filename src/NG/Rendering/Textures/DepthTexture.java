package NG.Rendering.Textures;

import NG.Tools.Logger;
import NG.Tools.Toolbox;
import de.matthiasmann.twl.utils.PNGDecoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.GL_R16;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

/**
 * @author Geert van Ieperen created on 25-9-2019.
 */
public class DepthTexture implements Texture2D {
    private static Map<File, DepthTexture> loadedMeshes = new HashMap<>();

    private final int id;

    private final int width;
    private final int height;

    private DepthTexture(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        PNGDecoder image = new PNGDecoder(in);

        this.width = image.getWidth();
        this.height = image.getHeight();

        // Load texture contents into a byte buffer
        int byteSize = 4;
        ByteBuffer buf = ByteBuffer.allocateDirect(byteSize * width * height);
        image.decode(buf, width * byteSize, PNGDecoder.Format.RGBA);
        buf.flip();

        // Create a new OpenGL texture
        this.id = glGenTextures();

        // Bind the texture
        glBindTexture(GL_TEXTURE_2D, id);

        // Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // Upload the texture data
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R16, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);

        // Generate Mip Map
        glGenerateMipmap(GL_TEXTURE_2D);

        Toolbox.checkGLError();
        Logger.DEBUG.printf("loaded depth texture %s: (%d, %d)", file.getName(), width, height);
    }

    @Override
    public void bind(int sampler) {
        glActiveTexture(sampler);
        glBindTexture(GL_TEXTURE_2D, id);
    }

    @Override
    public void cleanup() {
        glDeleteTextures(id);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setClamp(ClampMethod p){
        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, p.glValue);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, p.glValue);
    }
    public static DepthTexture get(File file) throws IOException {
        if (!file.exists()) throw new FileNotFoundException("Texture file does not exists: " + file);

        DepthTexture tex = loadedMeshes.get(file);

        if (tex == null) {
            tex = new DepthTexture(file);
            loadedMeshes.put(file, tex);
        }

        return tex;
    }

    public static Texture2D get(Path texturePath) throws IOException {
        return texturePath == null ? null : get(texturePath.toFile());
    }
}
