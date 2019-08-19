package NG.CollisionDetection;

import NG.Camera.Camera;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.DataStructures.Vector3fxc;
import NG.Entities.Entity;
import NG.Entities.MovingEntity;
import NG.InputHandling.ClickShader;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Rendering.GLFWWindow;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import NG.Tools.Vectors;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Geert van Ieperen created on 17-8-2019.
 */
public abstract class AbstractState implements GameState {
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

        entities().forEach(e -> {
            Vector3fxc position = e.getStateAt(rendertime).position();
            BoundingBox box = e.getBoundingBox().move(position);

            if (box.testFustrum(fic)) {
                e.draw(gl, rendertime);
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

            entity = getEntityByRay(origin, direction).left;
        }

        if (entity == null) return false;

        tool.apply(entity, xSc, ySc);
        return true;
    }

    @Override
    public void writeToDataStream(DataOutputStream out) throws IOException {
        Collection<Entity> entities = entities();
        List<Storable> box = new ArrayList<>(entities.size());

        for (Entity e : entities) {
            if (e instanceof Storable) {
                box.add((Storable) e);
            }
        }

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
