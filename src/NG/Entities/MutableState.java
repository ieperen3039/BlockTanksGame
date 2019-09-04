package NG.Entities;

import NG.DataStructures.Vector3fx;
import NG.DataStructures.Vector3fxc;
import NG.Storable;
import NG.Tools.Toolbox;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Describes the state of an entity. Simply put, this describes the position and velocity at a given time
 * @author Geert van Ieperen created on 26-7-2019.
 */
public class MutableState implements State {
    private float time;
    private Vector3fx position;
    private Vector3f velocity;
    private Quaternionf orientation;
    private Quaternionf rotationSpeed;

    public MutableState(float time, Vector3fxc position, Vector3fc velocity, Quaternionf orientation) {
        this(time, position, velocity, orientation, new Quaternionf());
    }

    public MutableState(
            float time, Vector3fxc position, Vector3fc velocity, Quaternionf orientation, Quaternionf rotationSpeed
    ) {
        this.time = time;
        this.position = new Vector3fx(position);
        this.orientation = new Quaternionf(orientation);
        this.velocity = new Vector3f(velocity);
        this.rotationSpeed = rotationSpeed;
    }

    public MutableState(float time, Vector3fxc position) {
        this.time = time;
        this.position = new Vector3fx(position);
        this.velocity = new Vector3f();
        this.orientation = new Quaternionf();
        this.rotationSpeed = new Quaternionf();
    }

    /**
     * copies the given state
     * @param source another state
     */
    public MutableState(State source) {
        this(source.time(), source.position(), source.velocity(), source.orientation(), new Quaternionf());
    }

    @Override
    public MutableState copy() {
        return new MutableState(this.time, this.position, this.velocity, this.orientation, this.rotationSpeed);
    }

    @Override
    public Vector3fxc position() {
        return position;
    }

    @Override
    public Vector3fc velocity() {
        return velocity;
    }

    @Override
    public Quaternionf orientation() {
        return orientation;
    }

    @Override
    public float time() {
        return time;
    }

    @Override
    public MutableState update(float gameTime) {
        float deltaTime = gameTime - time;

        Vector3f movement = new Vector3f(velocity).mul(deltaTime);
        position.add(movement);

        Quaternionf rotation = new Quaternionf(rotationSpeed).scale(deltaTime);
        orientation.add(rotation);

        return this;
    }

    public void set(State source) {
        this.time = source.time();
        position.set(source.position());
        velocity.set(source.velocity());
        orientation.set(source.orientation());

        if (source instanceof MutableState) {
            MutableState ms = (MutableState) source;
            rotationSpeed = ms.rotationSpeed;
        } else {
            rotationSpeed = new Quaternionf();
        }
    }

    /**
     * Adds the given velocity and rotational velocity to this state. During a physics update, the correct order of
     * updating is {@code this.add(velocityChange, rotationSpeedChange)}{@link #update(float) .update(gameTime)}.
     *
     * @param velocityChange      the change in velocity
     * @param rotationSpeedChange the change in rotation speed
     * @return this
     */
    public MutableState add(Vector3fc velocityChange, Quaternionfc rotationSpeedChange) {
        velocity.add(velocityChange);
        rotationSpeed.add(rotationSpeedChange);

        return this;
    }

    /**
     * sets the new velocity
     * @param newVelocity      the change in velocity
     * @return this
     */
    public MutableState setVelocity(Vector3fc newVelocity) {
        velocity.set(newVelocity);
        return this;
    }

    @Override
    public MutableState interpolateFraction(State other, float fraction) {
        float gameTime = Toolbox.interpolate(time, other.time(), fraction);
        Vector3fx p = new Vector3fx(position).lerp(other.position(), fraction);
        Vector3f v = new Vector3f(velocity).lerp(other.velocity(), fraction);

        return new MutableState(gameTime, p, v, orientation, rotationSpeed);
    }

    @Override
    public void writeToDataStream(DataOutputStream out) throws IOException {
        Storable.writeVector3fx(out, position);
        Storable.writeVector3f(out, velocity);
        Storable.writeQuaternionf(out, orientation);
        Storable.writeQuaternionf(out, rotationSpeed);
        out.writeFloat(time);
    }

    public MutableState(DataInputStream in) throws IOException {
        position = Storable.readVector3fx(in);
        velocity = Storable.readVector3f(in);
        orientation = Storable.readQuaternionf(in);
        rotationSpeed = Storable.readQuaternionf(in);
        time = in.readFloat();
    }
}
