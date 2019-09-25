package NG.Entities;

import NG.DataStructures.Vector3fx;
import NG.DataStructures.Vector3fxc;
import NG.Storable;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Geert van Ieperen created on 27-7-2019.
 */
public class FixedState implements State {
    private final Vector3fxc position;
    private final Quaternionf orientation;
    private float time;

    /**
     * create a state on the given position and orientation.
     * @param position    the position of this state
     * @param orientation the orientation of this state
     */
    public FixedState(Vector3fxc position, Quaternionf orientation) {
        this(position, orientation, 0);
    }

    /**
     * create a state on the given position and orientation, representing the given time.
     * @param position    the position of this state
     * @param orientation the orientation of this state
     * @param time        the time reporesented by this state. Only influences the result of {@link #update(float)}
     *                    and {@link State#interpolateFraction(State, float)}
     */
    public FixedState(Vector3fxc position, Quaternionf orientation, float time) {
        this.position = new Vector3fx(position);
        this.orientation = new Quaternionf(orientation);
        this.time = time;
    }

    /**
     * create a fixed copy of the given state
     * @param source
     */
    public FixedState(State source) {
        this(source.position(), source.orientation(), source.time());
    }

    public FixedState(Vector3fc position, Quaternionf orientation) {
        this(new Vector3fx(position), orientation);
    }

    @Override
    public State copy() {
        return new FixedState(position, orientation, time);
    }

    @Override
    public Vector3fxc position() {
        return position;
    }

    @Override
    public Vector3fc velocity() {
        return Vectors.O;
    }

    @Override
    public Quaternionf orientation() {
        return orientation;
    }

    @Override
    public float time() {
        return time;
    }

    /**
     * changes the time of this state. This is only effective when using {@link State#interpolateFraction(State, float)}
     * @param gameTime the new time of this state.
     */
    public void update(float gameTime) {
        this.time = gameTime;
    }

    @Override
    public State interpolateFraction(State other, float fraction) {
        float gameTime = Toolbox.interpolate(time, other.time(), fraction);
        Vector3fx pos = new Vector3fx(position).lerp(other.position(), fraction);
        Vector3f vel = new Vector3f(other.velocity()).mul(fraction);
        Quaternionf or = new Quaternionf(orientation).nlerp(other.orientation(), fraction);

        return new MutableState(gameTime, pos, vel, or);
    }

    @Override
    public void writeToDataStream(DataOutputStream out) throws IOException {
        Storable.writeVector3fx(out, position);
        Storable.writeQuaternionf(out, orientation);
        out.writeFloat(time);
    }

    public FixedState(DataInputStream in) throws IOException {
        position = Storable.readVector3fx(in);
        orientation = Storable.readQuaternionf(in);
        time = in.readFloat();
    }
}
