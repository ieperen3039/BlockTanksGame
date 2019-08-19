package NG.Rendering.Textures;

import NG.Tools.Directory;
import NG.Tools.Logger;

import java.io.File;
import java.io.IOException;

/**
 * @author Geert van Ieperen created on 1-2-2019.
 */
public enum GenericTextures implements Texture {
    CHECKER("..", "check.png"),
    ;

    private Texture tex;

    GenericTextures(String... path) {
        File file = Directory.meshes.getFile(path);

        try {
            tex = FileTexture.get(file);
        } catch (IOException ex) {
            Logger.ERROR.print(ex);
            tex = null;
        }
    }

    @Override
    public void bind(int sampler) {
        tex.bind(sampler);
    }

    @Override
    public void cleanup() {
        tex.cleanup();
    }

    @Override
    public int getWidth() {
        return tex.getWidth();
    }

    @Override
    public int getHeight() {
        return tex.getHeight();
    }
}
