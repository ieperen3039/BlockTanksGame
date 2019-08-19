package NG.GUIMenu;

import NG.DataStructures.Generic.Color4f;
import NG.GUIMenu.Components.SComponent;
import NG.GUIMenu.Frames.SFrameLookAndFeel;
import org.joml.Vector2i;
import org.joml.Vector2ic;

/**
 * @author Geert van Ieperen created on 8-8-2019.
 */
public class TankHUD extends SimpleHUD {

    public static final int RECTICLE_STROKE_WIDTH = 1;
    private static final int RECTICLE_SIZE = 10;

    public TankHUD() {
        super(new BaseLF(), new SComponent() {
            @Override
            public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
                GUIPainter g = design.getPainter();
                int hWidth = getWidth() / 2;
                int hHeight = getHeight() / 2;

                g.line(RECTICLE_STROKE_WIDTH, Color4f.GREEN,
                        new Vector2i(hWidth, hHeight - RECTICLE_SIZE),
                        new Vector2i(hWidth, hHeight + RECTICLE_SIZE)
                );
                g.line(RECTICLE_STROKE_WIDTH, Color4f.GREEN,
                        new Vector2i(hWidth - RECTICLE_SIZE, hHeight),
                        new Vector2i(hWidth + RECTICLE_SIZE, hHeight)
                );
            }
        });
    }
}
