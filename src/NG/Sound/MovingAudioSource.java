package NG.Sound;

import NG.Entities.MovingEntity;
import NG.Entities.State;
import org.joml.Vector3f;
import org.lwjgl.openal.AL10;

/**
 * @author Geert van Ieperen created on 7-2-2018.
 */
public class MovingAudioSource extends AudioSource {
    private final MovingEntity source;

    public MovingAudioSource(Sounds data, MovingEntity source, float pitch, float gain, boolean repeat) {
        super(data, new Vector3f(), new Vector3f(), pitch, gain, repeat);
        this.source = source;
    }

    public MovingAudioSource(Sounds data, MovingEntity source, float gain) {
        this(data, source, 1.0f, gain, false);
    }

    @Override
    public void update(float renderTime) {
        State entityState = source.getCurrentState();
        set(AL10.AL_POSITION, entityState.position().toVector3f());
        set(AL10.AL_VELOCITY, entityState.velocity());

        if (source.isDisposed()) {
            dispose();
        } else {
            super.update(renderTime);
        }
    }
}
