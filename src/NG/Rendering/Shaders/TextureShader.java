package NG.Rendering.Shaders;

import NG.Rendering.Textures.Texture;

/**
 * @author Geert van Ieperen created on 1-2-2019.
 */
public interface TextureShader {

    /**
     * sets the texture of the following object
     * @param tex the texture to use, or null to unbind
     */
    void setTexture(Texture tex);

    /**
     * unbinds the previously set texture
     */
    default void unsetTexture() {
        setTexture(null);
    }
}
