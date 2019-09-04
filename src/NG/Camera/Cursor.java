package NG.Camera;

import NG.Entities.State;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import NG.Tools.Toolbox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * @author Geert van Ieperen created on 5-2-2019.
 */
public class Cursor extends DummyEntity {
    private final Supplier<State> positionSupplier;

    public Cursor(Supplier<State> positionSupplier) {
        this.positionSupplier = positionSupplier;
    }

    @Override
    public State getCurrentState() {
        return positionSupplier.get();
    }

    @Override
    public void draw(SGL gl, float renderTime) {
        gl.pushMatrix();
        gl.translate(positionSupplier.get().position().toVector3f());
        Toolbox.draw3DPointer(gl);
        gl.popMatrix();
    }

    @Override
    public void writeToDataStream(DataOutputStream out) throws IOException {
        Storable.write(out, getCurrentState());
    }

    Cursor(DataInputStream in) throws IOException, ClassNotFoundException {
        State state = Storable.read(in, State.class);
        positionSupplier = () -> state;
    };
}
