package NG.Settings;

import NG.DataStructures.Generic.Color4f;

/**
 * A class that collects a number of settings. It is the only class whose fields are always initialized upon creation.
 * @author Geert van Ieperen. Created on 13-9-2018.
 */
public class Settings {
    public static final String GAME_NAME = "Block Tanks"; // laaaaame

    // debug settings
    public boolean DEBUG = true;
    public boolean DEBUG_SCREEN = DEBUG;
    public boolean RENDER_HITBOXES = DEBUG;

    // game engine settings
    public int TARGET_TPS = 30;
    public static final int CHUNK_SIZE = 16; // number of tiles in a chunk
    public static final float TILE_SIZE = 2f;
    public static final float TILE_SIZE_Z = 0.5f;
    public static final float GRAVITY_CONSTANT = 5f; // 9,81
    public static final float MIN_COLLISION_CHECK_SQ = 1e-6f; // squared

    // video settings
    public static final float FOV = (float) Math.toRadians(30);
    public static final float Z_NEAR = 0.01f;
    public static final float Z_FAR = 1000;
    public int TARGET_FPS = 30;
    public boolean V_SYNC = false;
    public int DEFAULT_WINDOW_WIDTH = 1600;
    public int DEFAULT_WINDOW_HEIGHT = 800;
    public int ANTIALIAS_LEVEL = 1;
    public boolean ISOMETRIC_VIEW = false;
    public float RENDER_DELAY = 1f / TARGET_TPS;
    public float CAMERA_ZOOM_SPEED = 0.1f;
    public int MAX_CAMERA_DIST = (int) Z_FAR;
    public float MIN_CAMERA_DIST = 0.5f;
    public int STATIC_SHADOW_RESOLUTION = 0;
    public int DYNAMIC_SHADOW_RESOLUTION = 0;
    public float PARTICLE_SIZE = 0.2f;
    public float PARTICLE_MODIFIER = 1f;

    // audio settings
    public float SOUND_MASTER_GAIN;

    // UI settings
    public static int TOOL_BAR_HEIGHT = 70;
    public boolean HIDE_POINTER_ON_MAP = false;

    // ambiance settings
    public static final Color4f AMBIENT_LIGHT = new Color4f(1, 1, 1, 0.2f);
}
