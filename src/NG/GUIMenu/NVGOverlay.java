package NG.GUIMenu;

import NG.DataStructures.Generic.Color4f;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * @author Jorren & Geert van Ieperen
 */
public final class NVGOverlay {
    private long vg;
    private NVGColor nvgColorBuffer;
    private NVGPaint paint;

    /* fontbuffer MUST be a field */
    @SuppressWarnings("FieldCanBeLocal")
    private final ByteBuffer[] fontBuffer = new ByteBuffer[NGFonts.values().length];
    private Map<Path, Integer> imageBuffer = new HashMap<>();

    private final Collection<Consumer<GUIPainter>> drawBuffer = new ArrayList<>();
    private final Lock drawBufferLock = new ReentrantLock();

    /**
     * @throws IOException If an error occurs during the setup of the Hud.
     * @param antialiasLevel
     */
    public void init(int antialiasLevel) throws IOException {
        if (antialiasLevel > 0) {
            vg = nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES);
        } else {
            vg = nvgCreate(NVG_STENCIL_STROKES);
        }

        if (vg == NULL) {
            throw new IOException("Could not initialize NanoVG");
        }

        NGFonts[] fonts = NGFonts.values();
        for (int i = 0; i < fonts.length; i++) {
            fontBuffer[i] = fonts[i].asByteBuffer();
            if (nvgCreateFontMem(vg, fonts[i].name, fontBuffer[i], 1) == -1) {
                Logger.ERROR.print("Could not create font " + fonts[i].name);
            }
        }

        nvgColorBuffer = NVGColor.create();
        paint = NVGPaint.create();
    }

    public void cleanup() {
        drawBufferLock.lock();
        try {
            drawBuffer.clear();
        } finally {
            drawBufferLock.unlock();
        }
    }

    public void addHudItem(Consumer<GUIPainter> render) {
        if (render == null) return;

        drawBufferLock.lock();
        try {
            drawBuffer.add(render);
        } finally {
            drawBufferLock.unlock();
        }
    }

    public void removeHudItem(Consumer<GUIPainter> render) {
        if (render == null) return;

        drawBufferLock.lock();
        try {
            drawBuffer.remove(render);
        } finally {
            drawBufferLock.unlock();
        }
    }


    /**
     * @param windowWidth    width of the current window drawn on
     * @param windowHeight   height of the current window
     * @param cameraPosition position of camera
     */
    public void draw(int windowWidth, int windowHeight, Vector3f cameraPosition) {
        GUIPainter vanGogh = new Painter(windowWidth, windowHeight, cameraPosition, 5, 5, 24);
        draw(windowWidth, windowHeight, vanGogh);
    }

    /**
     * @param windowWidth  width of the current window drawn on
     * @param windowHeight height of the current window
     * @param xRoll        x position of the debug printroll screen
     * @param yRoll        y position of ''
     * @param rollSize     font size of ''
     */
    public void draw(int windowWidth, int windowHeight, int xRoll, int yRoll, int rollSize) {
        GUIPainter bobRoss = new Painter(windowWidth, windowHeight, Vectors.Z, xRoll, yRoll, rollSize);
        draw(windowWidth, windowHeight, bobRoss);
    }

    /**
     * draw using the given painter
     */
    private synchronized void draw(int windowWidth, int windowHeight, GUIPainter painter) {
        // this should be the case
        glViewport(0, 0, windowWidth, windowHeight);
        // Begin NanoVG frame
        nvgBeginFrame(vg, windowWidth, windowHeight, 1);

        // Draw the buffer elements
        drawBufferLock.lock();
        try {
            drawBuffer.forEach(m -> m.accept(painter));
        } finally {
            drawBufferLock.unlock();
        }

        // End NanoVG frame
        nvgEndFrame(vg);

        // restore window state
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
    }

    public class Painter implements GUIPainter {
        private final int printRollSize;
        private final int yPrintRoll;
        private final int xPrintRoll;
        private int printRollEntry = 0;

        private Color4f fillColor = Color4f.WHITE;
        private Color4f strokeColor;
        private int strokeWidth = 1;
        private Color4f textColor = Color4f.WHITE;

        public final int windowWidth;
        public final int windowHeight;
        public final Vector3fc cameraPosition;

        /**
         * @param windowWidth    width of this hud display iteration
         * @param windowHeight   height of ''
         * @param cameraPosition renderposition of camera in worldspace
         * @param xPrintRoll     x position of where to start the printRoll
         * @param yPrintRoll     y position of where to start the printRoll
         * @param printRollSize  fontsize of printRoll
         */
        public Painter(
                int windowWidth, int windowHeight, Vector3fc cameraPosition,
                int xPrintRoll, int yPrintRoll, int printRollSize
        ) {
            this.windowWidth = windowWidth;
            this.windowHeight = windowHeight;
            this.cameraPosition = cameraPosition;
            this.printRollSize = printRollSize;
            this.yPrintRoll = printRollSize + yPrintRoll;
            this.xPrintRoll = xPrintRoll;
        }

        /**
         * Get an instance of NVGColor with the correct values. All nvgColorBuffer values are floating point numbers
         * supposed to be between 0f and 1f.
         * @param red   The red component.
         * @param green The green component.
         * @param blue  The blue component.
         * @param alpha The alpha component.
         * @return an instance of NVGColor.
         */
        private NVGColor toBuffer(float red, float green, float blue, float alpha) {
            nvgColorBuffer.r(red);
            nvgColorBuffer.g(green);
            nvgColorBuffer.b(blue);
            nvgColorBuffer.a(alpha);

            return nvgColorBuffer;
        }

        /** @see #toBuffer(float, float, float, float) */
        private NVGColor toBuffer(Color4f color) {
            return toBuffer(color.red, color.green, color.blue, color.alpha);
        }

        @Override
        public void setFillColor(Color4f color) {
            this.fillColor = color;
            nvgFillColor(vg, toBuffer(color));
        }

        @Override
        public void setStroke(Color4f color, int width) {
            this.strokeWidth = width;
            this.strokeColor = color;
            nvgStrokeWidth(vg, width);
            nvgStrokeColor(vg, toBuffer(color));
        }

        @Override
        public void rectangle(int x, int y, int width, int height) {
            assert width >= 0 : "Negative width: " + width + " (height = " + height + ")";
            assert height >= 0 : "Negative height: " + height + " (width = " + width + ")";

            nvgBeginPath(vg);
            nvgRect(vg, x, y, width, height);

            nvgFill(vg);
            nvgStroke(vg);
        }

        @Override
        public void rectangle(
                int x, int y, int width, int height, Color4f fillColor, Color4f strokeColor, int strokeWidth
        ) {
            nvgFillColor(vg, toBuffer(fillColor));
            nvgStrokeColor(vg, toBuffer(strokeColor));
            nvgStrokeWidth(vg, strokeWidth);
            rectangle(x, y, width, height);
            restoreColors();
        }

        /**
         * resets the colors to the colors given using {@link #setFillColor(Color4f)} and {@link #setStroke(Color4f,
         * int)}
         */
        private void restoreColors() {
            nvgFillColor(vg, toBuffer(fillColor));
            nvgStrokeWidth(vg, strokeWidth);
            nvgStrokeColor(vg, toBuffer(strokeColor));
        }

        @Override
        public void circle(int x, int y, int radius) {
            nvgBeginPath(vg);
            nvgCircle(vg, x, y, radius);

            nvgFill(vg);
            nvgStroke(vg);
        }

        @Override
        public void polygon(Color4f fillColor, Color4f strokeColor, int strokeWidth, Vector2i... points) {
            nvgFillColor(vg, toBuffer(fillColor));
            nvgStrokeColor(vg, toBuffer(strokeColor));
            nvgStrokeWidth(vg, strokeWidth);
            polygon(points);
            restoreColors();
        }

        @Override
        public void polygon(Vector2i... points) {
            nvgBeginPath(vg);

            nvgMoveTo(vg, points[points.length - 1].x, points[points.length - 1].y);
            for (Vector2i point : points) {
                nvgLineTo(vg, point.x, point.y);
            }

            nvgFill(vg);
            nvgStroke(vg);
        }

        @Override
        public void line(int strokeWidth, Color4f strokeColor, Vector2i... points) {
            nvgStrokeColor(vg, toBuffer(strokeColor));
            nvgStrokeWidth(vg, strokeWidth);
            nvgBeginPath(vg);
            nvgMoveTo(vg, points[0].x, points[0].y);

            for (int i = 1; i < points.length; i++) {
                nvgLineTo(vg, points[i].x, points[i].y);
            }

            nvgStroke(vg);
            restoreColors();
        }

        // non-shape functions

        @Override
        public void text(
                int x, int y, float size, NGFonts font, EnumSet<Alignment> alignment, Color4f color, String text
        ) {
            int alignFlags = getAlignFlags(alignment);

            nvgFontSize(vg, size);
            nvgFontFace(vg, font.name);
            nvgTextAlign(vg, alignFlags);
            nvgFillColor(vg, toBuffer(color));
            nvgText(vg, x, y, text);

            nvgFillColor(vg, toBuffer(fillColor));
        }

        private int getAlignFlags(EnumSet<Alignment> alignment) {
            int alignFlags = 0;

            if (alignment.contains(Alignment.ALIGN_TOP)) {
                alignFlags |= NVG_ALIGN_TOP;
            } else if (alignment.contains(Alignment.ALIGN_BOTTOM)) {
                alignFlags |= NVG_ALIGN_BOTTOM;
            } else {
                alignFlags |= NVG_ALIGN_MIDDLE;
            }

            if (alignment.contains(Alignment.ALIGN_LEFT)) {
                alignFlags |= NVG_ALIGN_LEFT;
            } else if (alignment.contains(Alignment.ALIGN_RIGHT)) {
                alignFlags |= NVG_ALIGN_RIGHT;
            } else {
                alignFlags |= NVG_ALIGN_CENTER;
            }

            return alignFlags;
        }

        @Override
        public void printRoll(String text) {
            int y = yPrintRoll + ((printRollSize + 5) * printRollEntry);

            text(xPrintRoll, y, printRollSize, NGFonts.LUCIDA_CONSOLE, EnumSet.of(Alignment.ALIGN_LEFT), textColor, text);
            printRollEntry++;
        }

        @Override
        public void image(Path filename, int x, int y, int width, int height, float alpha) throws IOException {
            image(filename, x, y, width, height, 0f, alpha, NVG_IMAGE_GENERATE_MIPMAPS);
        }

        @Override
        public void render(Runnable action) {
            nvgEndFrame(vg);
            glEnable(GL_DEPTH_TEST);
            glEnable(GL_STENCIL_TEST);

            action.run();

            glDisable(GL_DEPTH_TEST);
            nvgBeginFrame(vg, windowWidth, windowHeight, 1);
            restoreColors();
        }

        private void image(Path fileName, int x, int y, int width, int height, float angle, float alpha, int imageFlags)
                throws IOException {
            int img = getImage(fileName, imageFlags);
            NVGPaint p = nvgImagePattern(vg, x, y, width, height, angle, img, alpha, paint);

            rectangle(x, y, width, height);

            nvgFillPaint(vg, p);
            nvgFill(vg);
        }

        private int getImage(Path filePath, int imageFlags) throws IOException {
            if (imageBuffer.containsKey(filePath)) {
                return imageBuffer.get(filePath);
            }
            ByteBuffer image = Toolbox.toByteBuffer(filePath);
            int img = nvgCreateImageMem(vg, imageFlags, image);
            imageBuffer.put(filePath, img);
            return img;
        }

    }
}
