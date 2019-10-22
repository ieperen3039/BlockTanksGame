package NG.Entities.Projectiles;

import NG.DataStructures.Vector3fx;
import NG.Entities.MutableState;
import NG.Entities.State;
import NG.Rendering.MatrixStack.SGL;
import NG.Shapes.GenericShapes;
import NG.Storable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DeathIcosahedron extends Projectile {
    public DeathIcosahedron(MutableState spawnState) {
        super(spawnState);
    }

    @Override
    protected void drawProjectile(SGL gl, float renderTime) {
        gl.render(GenericShapes.ICOSAHEDRON, this);
    }

    @Override
    public float getMass() {
        return 10; // I dunno
    }

    @Override
    protected Vector3fx getCenterOfMass() {
        return new Vector3fx(state.position());
    }

    @Override
    public void writeToDataStream(DataOutputStream out) throws IOException {
        state.writeToDataStream(out);
    }

    public DeathIcosahedron(DataInputStream in) throws IOException, ClassNotFoundException {
        super(Storable.read(in, MutableState.class));
    }
}
