package NG.Sound;

import NG.Settings.Settings;
import NG.Tools.*;
import org.joml.Vector3fc;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.*;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static NG.Tools.Toolbox.checkALError;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.EXTEfx.ALC_MAX_AUXILIARY_SENDS;

/**
 * This software is using the J-Ogg library available from http://www.j-ogg.de and copyrighted by Tor-Einar Jarnbjo.
 * @author Geert van Ieperen
 * created on 5-2-2018.
 */
public class SoundEngine {

    // default device
    private final long device;

    /**
     * set up openAL environment
     */
    public SoundEngine(Settings settings) {
        // Create a handle for the device capabilities
        device = ALC10.alcOpenDevice((ByteBuffer) null);
        if (device == MemoryUtil.NULL) throw new RuntimeException("Could not find default device");

        ALCCapabilities deviceCaps = ALC.createCapabilities(device);

        IntBuffer contextAttribList = BufferUtils.createIntBuffer(16);
        // default attributes
        int[] attributes = new int[]{
                ALC_REFRESH,                60,
                ALC_SYNC,                   ALC_FALSE,
                ALC_MAX_AUXILIARY_SENDS,    2,
                0
        };

        contextAttribList.put(attributes);

        // create the context with the provided attributes
        long newContext = ALC10.alcCreateContext(device, contextAttribList);

        if(!ALC10.alcMakeContextCurrent(newContext)) {
            throw new RuntimeException("Failed to make OpenAL context current");
        }

        final ALCapabilities alCaps = AL.createCapabilities(deviceCaps);
        checkALError();

            if (!alCaps.OpenAL10) Logger.DEBUG.print("Sound system does not support Open AL 10");
            if (!deviceCaps.OpenALC10) Logger.DEBUG.print("Sound system does not support Open ALC 10");
//            if (!alCaps.) System.err.println("Warning: Sound system does not support ...");

//        AL10.alDistanceModel(AL_LINEAR_DISTANCE);
        AL10.alListenerf(AL10.AL_GAIN, settings.SOUND_MASTER_GAIN);
        AL10.alDopplerFactor(0.2f);

        setListenerPosition(Vectors.O,Vectors.O);
        setListenerOrientation(Vectors.X, Vectors.Y);

        checkALError();
    }

    /**
     * set the position and velocity of the listener
     * @param pos position
     * @param vel velocity, does not influence position
     */
    public void setListenerPosition(Vector3fc pos, Vector3fc vel) {
        AL10.alListener3f(AL10.AL_POSITION, pos.x(), pos.y(), pos.z());
        AL10.alListener3f(AL10.AL_VELOCITY, vel.x(), vel.y(), vel.z());
    }

    public void setListenerOrientation(Vector3fc forward, Vector3fc up) {
        float[] asArray = {forward.x(), forward.y(), forward.z(), up.x(), up.y(), up.z()};
        AL10.alListenerfv(AL10.AL_ORIENTATION, asArray);
    }

    public void closeDevices() {
        boolean success = ALC10.alcCloseDevice(device);
        if (!success) Logger.WARN.print("Could not close device");
    }

    public static void main(String[] args) {
        SoundEngine soundEngine = new SoundEngine(new Settings());
        checkALError();
        Timer timer = new Timer();

        final float PITCH = 2f;
        final File FILE = Directory.soundEffects.getFile("powerfield.ogg");

        try {
            AudioFile audioData = new AudioFile(FILE);
            audioData.load();
            AudioSource src = new AudioSource(audioData, 1.0f, true);
            src.setPitch(PITCH);
            src.play();

            Logger.DEBUG.print("Playing sound... Do you hear it?");
            checkALError();
            while (!src.isDisposed()) {
                timer.updateLoopTime();
                src.update(timer.getTime());
                Toolbox.sleep(100);
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            checkALError();
            soundEngine.closeDevices();
        }
    }

}
