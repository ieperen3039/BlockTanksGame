package NG.Sound;

import NG.Tools.Logger;
import NG.Tools.Toolbox;
import org.lwjgl.openal.AL10;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author Geert van Ieperen created on 6-2-2018.
 */
public class AudioFile {
    private static Queue<AudioFile> registeredSoundfiles = new ArrayDeque<>();
    public static AudioFile emptyFile = new AudioFile();

    public static final int DATA_NOT_LOADED = -1;
    public static final int DATA_COULD_NOT_BE_LOADED = -2;

    private int dataID = DATA_NOT_LOADED;
    private final File audioData;
    private final FileType type;

    /**
     * create an unloaded audio file handle
     * @param file
     */
    public AudioFile(File file) {
        audioData = file;
        String fileName = file.getName();

        if (fileName.endsWith(".ogg")) {
            type = FileType.ogg;
        } else if (fileName.endsWith(".wav")) {
            type = FileType.wave;

        } else if (!fileName.contains(".")) {
            Logger.ERROR.print("File " + fileName + " lacks an extension");
            type = null;
        } else {
            Logger.ERROR.print("Filetype of '" + fileName + "' is not supported.");
            type = null;
        }
    }

    /** empty audio file */
    private AudioFile() {
        audioData = null;
        type = null;
        dataID = 0;
    }

    /**
     * load this file to memory. Inverses an action of {@link #dispose()}
     */
    public void load() {
        // only load if this is not done yet
        if (dataID != -1) return;
        Toolbox.checkALError();

        this.dataID = AL10.alGenBuffers();

        boolean success = false;
        switch (type) {
            case wave:
                success = loadWaveData(dataID, audioData);
                break;
            case ogg:
                success = loadOggData(dataID, audioData);
                break;
        }

        // register for cleanup, unless it failed to load
        if (success) {
            registeredSoundfiles.add(this);
        } else {
            dataID = DATA_COULD_NOT_BE_LOADED;
        }

        Toolbox.checkALError();
    }
    /**
     * load the data as if it is a .wav file
     *
     * @param dataID
     * @param audioData
     * @return true iff it was loaded properly
     */
    public static boolean loadWaveData(int dataID, File audioData) {
        // load soundfile to audiostream
        WaveData waveFile;
        try {
            waveFile = WaveData.create(audioData);
            Toolbox.checkALError();

        } catch (IOException | UnsupportedAudioFileException e) {
            Logger.WARN.print("Could not load wave file '" + audioData + "'. Continuing without this sound");
            Logger.DEBUG.print(e);
            return false;
        }

        // load audio into soundcard
        AL10.alBufferData(dataID, waveFile.format, waveFile.data, waveFile.samplerate);
        waveFile.dispose();

        Toolbox.checkALError();
        return true;
    }

    public static boolean loadOggData(int dataID, File audioData) {
        Toolbox.checkALError();

        OggData oggFile;
        try {
            oggFile = OggData.create(audioData.getPath());
            Toolbox.checkALError();

        } catch (IOException e) {
            Logger.WARN.print("Could not load ogg file '" + audioData + "'. Continuing without this sound");
            Logger.DEBUG.print(e);
            return false;
        }

        AL10.alBufferData(dataID, oggFile.format, oggFile.data, oggFile.samplerate);

        Toolbox.checkALError();
        return true;
    }

    public int getID() {
        return dataID;
    }

    public boolean isLoaded() {
        return (dataID != DATA_NOT_LOADED) && (dataID != DATA_COULD_NOT_BE_LOADED);
    }

    /**
     * remove this soundfile from memory
     * @see #load()
     */
    public void dispose() {
        if (dataID > 0) {
            AL10.alDeleteBuffers(dataID);
            dataID = DATA_NOT_LOADED;
        }
        registeredSoundfiles.remove(this);
    }

    /**
     * all sounds that have been written to the soundcard will be removed. This will result in warnings if any of the
     * files are still used by sound sources.
     * @see #load()
     */
    @SuppressWarnings("ConstantConditions")
    public static void cleanAll() {
        while (!registeredSoundfiles.isEmpty()) {
            registeredSoundfiles.peek().dispose();
            Toolbox.checkALError();
        }
    }

    @Override
    public String toString() {
        return String.format("Audio(%d: %s)", dataID, audioData.getName());
    }

    private enum FileType {
        wave, ogg
    }
}
