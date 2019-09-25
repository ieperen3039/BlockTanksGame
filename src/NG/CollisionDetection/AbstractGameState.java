package NG.CollisionDetection;

import NG.Camera.Camera;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.Entities.Entity;
import NG.Entities.MovingEntity;
import NG.InputHandling.ClickShader;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Rendering.GLFWWindow;
import NG.Rendering.MatrixStack.SGL;
import NG.Settings.Settings;
import NG.Storable;
import NG.Tools.Vectors;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * @author Geert van Ieperen created on 17-8-2019.
 */
public abstract class AbstractGameState implements GameState {
    private Game game;

    @Override
    public void init(Game game) throws Exception {
        this.game = game;
    }

    @Override
    public void drawEntities(SGL gl) {
        float rendertime = game.get(GameTimer.class).getRendertime();
        GLFWWindow window = game.get(GLFWWindow.class);
        Matrix4f viewProjection = game.get(Camera.class)
                .getViewProjection((float) window.getWidth() / window.getHeight());
        FrustumIntersection fic = new FrustumIntersection().set(viewProjection, false);

        entities().forEach(ety -> {
            if (game.get(Settings.class).DEBUG || ety.getHitbox(rendertime).testFustrum(fic)) {
                ety.draw(gl, rendertime);
            }
        });
    }

    @Override
    public boolean checkMouseClick(MouseTool tool, int xSc, int ySc) {
        Entity entity;

        if (game.has(ClickShader.class)) {
            entity = game.get(ClickShader.class).getEntity(game, xSc, ySc);

        } else {
            Vector3f origin = new Vector3f();
            Vector3f direction = new Vector3f();
            Vectors.windowCoordToRay(game, xSc, ySc, origin, direction);

            float rendertime = game.get(GameTimer.class).getRendertime();
            entity = getEntityByRay(origin, direction, rendertime).left;
        }

        if (entity == null) return false;

        tool.apply(entity, xSc, ySc);
        return true;
    }

    @Override
    public void writeToDataStream(DataOutputStream out) throws IOException {
        Collection<? extends Storable> box = entities();

        out.writeInt(box.size());
        for (Storable s : box) {
            Storable.writeSafe(out, s);
        }
    }

    /**
     * reverts {@link #writeToDataStream(DataOutputStream)}
     * @param in the input stream
     */
    public void readFromDataStream(DataInputStream in) throws IOException {
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            Entity entity = Storable.readSafe(in, MovingEntity.class);
            if (entity == null) continue;
            addEntity(entity);
        }
    }
}
