package NG.Sound;

import NG.Tools.Directory;

import static NG.Tools.Directory.music;
import static NG.Tools.Directory.soundEffects;

/**
 * @author Geert van Ieperen
 * created on 6-2-2018.
 */
public enum Sounds {
    button(soundEffects, "toggle_button.ogg"),
    pulsePower(music, "Pulse Power.ogg"),
    cosmicBlack(music, "Cosmic Black.ogg"),

    powerupOne(soundEffects, "powerup_1.wav"),
    powerupTwo(soundEffects, "powerup_2.wav"),

    jet_fire(soundEffects, "qubodupFireLoop.ogg"),
    booster(soundEffects, "loop_2.wav"),
    fizzle(soundEffects, "rocket_booster.ogg"),
    shield(soundEffects, "powerfield.ogg"),
    windOff(soundEffects, "qubodupFireLoop.ogg"),

    explosionMC(soundEffects, "explosion.ogg"),
    explosion2(soundEffects, "explosion_2.ogg"),
    seekerPop(soundEffects, "seeker_pop.ogg"),
    shieldPop(soundEffects, "fizzle.ogg"),
    deathWarning(soundEffects, "explosion.ogg"),

    testSound(soundEffects, "fizzle.ogg");

    private AudioFile audioFile;

    Sounds(Directory dir, String name) {
        audioFile = new AudioFile(dir.getFile(name));
    }

    public AudioFile get() {
        if (!audioFile.isLoaded()) audioFile.load();
        return audioFile;
    }
}
