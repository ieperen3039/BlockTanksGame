package NG.Rendering;

import NG.Core.Game;
import NG.Core.GameAspect;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.Shaders.ShaderProgram;
import NG.Tools.Vectors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A collection of drawing calls executed on a specific shader. The drawing calls are executed in the order in which
 * they are added to this bundle.
 */
public class RenderBundle implements GameAspect {
    private ShaderProgram shader;
    private List<Consumer<SGL>> targets;
    private Game game;

    public RenderBundle(ShaderProgram shader) {
        this.shader = shader;
        this.targets = new ArrayList<>();
    }

    @Override
    public void init(Game game) throws Exception {
        this.game = game;
    }

    /**
     * appends the given consumer to the end of the render sequence
     * @return this
     */
    public RenderBundle add(Consumer<SGL> drawable) {
        targets.add(drawable);
        return this;
    }

    /**
     * executes the given drawables in order
     */
    public void draw() {
        shader.bind();
        {
            shader.initialize(game);

            // GL object
            SGL gl = shader.getGL(game);

            for (int i = 0; i < targets.size(); i++) {
                targets.get(i).accept(gl);

                // check that the given call has not modified the SGL state
                assert gl.getPosition(Vectors.Scaling.UNIFORM).equals(Vectors.Scaling.UNIFORM) :
                        "drawing call " + i + " did not properly restore the SGL object";

            }
        }
        shader.unbind();
    }

    @Override
    public void cleanup() {
        shader.cleanup();
        targets.clear();
    }
}
