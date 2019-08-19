package NG.Sound;

import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Vector3fc;
import org.lwjgl.openal.AL10;
import org.lwjgl.system.MemoryUtil;

import java.util.Collection;

import static NG.Sound.AudioFile.DATA_COULD_NOT_BE_LOADED;
import static NG.Sound.AudioFile.DATA_NOT_LOADED;
import static org.lwjgl.openal.AL10.*;

/**
 * @author Geert van Ieperen
 * created on 5-2-2018.
 */
public class AudioSource {
    protected int sourceID;
    private boolean isDisposed = false;

    protected AudioSource(AudioFile data) {
        Toolbox.checkALError();
        int dataID = data.getID();
        if (dataID == DATA_NOT_LOADED) throw new NullPointerException("Audio file " + data + " has not been loaded");
        if (dataID == DATA_COULD_NOT_BE_LOADED) return; // data could not be loaded, and this has already been reported

        sourceID = AL10.alGenSources();
        if (sourceID == MemoryUtil.NULL)
            throw new RuntimeException("Could not generate buffer for sound"); // TODO replace all runtimeExceptions with others
        alSourcei(sourceID, AL10.AL_BUFFER, dataID);
        Toolbox.checkALError();
    }

    /** creates and plays the specified sound on the specified place */
    public AudioSource(Sounds data, Vector3fc sourcePos, float pitch, float gain) {
        this(data, sourcePos, Vectors.O, pitch, gain, false);
    }

    /** creates and plays the specified sound on the specified place */
    public AudioSource(Sounds data, Vector3fc sourcePos, Vector3fc velocity, float pitch, float gain, boolean doLoop) {
        this(data.get());
        alSourcef(sourceID, AL10.AL_PITCH, pitch);
        set(AL10.AL_POSITION, sourcePos);
        set(AL10.AL_VELOCITY, velocity);
        if (doLoop) alSourcei(sourceID, AL_LOOPING, AL_TRUE);
        setGain(gain);

        AL10.alSourcePlay(sourceID);
    }

    /** plays the given soundfile as background music */
    public AudioSource(AudioFile file, float gain, boolean doLoop) {
        this(file);

        alSourcei(sourceID, AL_SOURCE_RELATIVE, AL_TRUE);
        alSourcef(sourceID, AL_ROLLOFF_FACTOR, 0.0f);
        if (doLoop) alSourcei(sourceID, AL_LOOPING, AL_TRUE);
        setGain(gain);

        AL10.alSourcePlay(sourceID);
        Toolbox.checkALError();
    }

    private AudioSource() {
        sourceID = 0;
        isDisposed = true;
    }

    public void pause() {
        AL10.alSourcePause(sourceID);
    }

    public void play() {
        AL10.alSourcePlay(sourceID);
        Toolbox.checkALError();
    }

    public void interrupt(){
        AL10.alSourceStop(sourceID);
    }

    /**
     * remove this source off the soundcard
     */
    public void dispose(){
        if (isDisposed) {
            return;
        }
        AL10.alDeleteSources(sourceID);
        isDisposed = true;
    }

    public static void disposeAll(Collection<AudioSource> sources) {
        int[] names = new int[sources.size()];
        int i = 0;
        for (AudioSource s : sources) {
            names[i++] = s.sourceID;
            s.isDisposed = true;
        }
        AL10.alDeleteSources(names);
    }

    public boolean isDisposed() {
        return isDisposed;
    }

    public void set(int mode, Vector3fc p) {
        AL10.alSource3f(sourceID, mode, p.x(), p.y(), p.z());
    }

    public void setGain(float newValue){
        alSourcef(sourceID, AL10.AL_GAIN, newValue);
        Toolbox.checkALError("gain: " + newValue);
    }

    public void update(float renderTime) {
        if (AL10.alGetSourcei(sourceID, AL_SOURCE_STATE) == AL_STOPPED) {
            dispose();
        }
    }

    public void setPitch(float value) {
        alSourcef(sourceID, AL10.AL_PITCH, value);
        Toolbox.checkALError("pitch: " + value);
    }

    public static AudioSource empty = new AudioSource();
}
