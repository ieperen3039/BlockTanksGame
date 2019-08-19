package NG.Rendering.MeshLoading;

import NG.Rendering.MatrixStack.SGL;
import NG.Tools.Toolbox;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * @author Geert van Ieperen created on 1-2-2019.
 */
public abstract class AbstractMesh implements Mesh {
    protected static Queue<AbstractMesh> loadedMeshes = new ArrayDeque<>();

    private int VAO_ID = 0;
    private int EBO_ID = 0;
    private int nrOfElements = 0;
    private int[] VBOIndices;

    public void render(SGL.Painter lock) {
        if (VAO_ID == 0) return;

        glBindVertexArray(VAO_ID);

        // enable all non-null attributes
        for (int i = 0; i < VBOIndices.length; i++) {
            if (VBOIndices[i] != 0) {
                glEnableVertexAttribArray(i);
            }
        }

        if (EBO_ID == 0) {
            // draw the regular way
            glDrawArrays(GL_TRIANGLES, 0, nrOfElements);

        } else {
            // draw using an index buffer
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO_ID);
            glDrawElements(GL_TRIANGLES, nrOfElements, GL_UNSIGNED_INT, 0);
        }

        // disable all enabled attributes
        for (int i = 0; i < VBOIndices.length; i++) {
            if (VBOIndices[i] != 0) {
                glDisableVertexAttribArray(i);
            }
        }

        glBindVertexArray(0);
    }

    /**
     * loads an index array for indexed rendering
     * @param indices an array of indices, which isnot modified nor cached.
     */
    public void createIndexBuffer(int[] indices) {
        assert EBO_ID == 0;
        EBO_ID = glGenBuffers();
        setElementCount(indices.length);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO_ID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
    }

    /**
     * Initiates the creation of a mesh on the GPU.
     */
    protected void createVAO() {
        assert VAO_ID == 0;
        VAO_ID = glGenVertexArrays();
        loadedMeshes.add(this);
    }

    public int getVAO() {
        return VAO_ID;
    }

    public int getElementCount() {
        return nrOfElements;
    }

    protected void setElementCount(int elementCount) {
        assert this.nrOfElements == 0;
        this.nrOfElements = elementCount;
    }

    protected void createVBOTable() {
        assert VBOIndices == null;
        this.VBOIndices = new int[5];
    }

    public int[] getVBOTable() {
        return VBOIndices;
    }

    public void dispose() {
        if (VAO_ID == 0) return;

        glDisableVertexAttribArray(0);
        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        for (int vbo : VBOIndices) {
            glDeleteBuffers(vbo);
        }

        if (EBO_ID != 0) {
            glDeleteBuffers(EBO_ID);
        }

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(VAO_ID);

        loadedMeshes.remove(this);
        VAO_ID = 0;
    }

    /**
     * all meshes that have been written to the GPU will be removed
     */
    public static void cleanAll() {
        while (!loadedMeshes.isEmpty()) {
            loadedMeshes.peek().dispose();
        }
        Toolbox.checkGLError();
    }

    /**
     * Creates a buffer object to transfer data to the GPU. The reference to the resulting VBO is placed at
     * VBOIndices[index].
     * @param data  data to transfer
     * @param index index of the VBO, one of the constants given in {@link NG.Rendering.Shaders.ShaderProgram}
     * @param size  number of elements in each attribute
     */
    @SuppressWarnings("Duplicates")
    public void createVBO(float[] data, int index, int size) {
        if (index < 0 || index >= VBOIndices.length) {
            throw new IndexOutOfBoundsException(
                    "Given index out of bounds: " + index + " on size " + VBOIndices.length);
        }

//        FloatBuffer buffer = FloatBuffer.wrap(data);
        FloatBuffer buffer = MemoryUtil.memAllocFloat(data.length);
        buffer.put(data).flip();

        try {
            int vboId = glGenBuffers();
            assert vboId != 0; // this is not guaranteed by specification

            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
            glVertexAttribPointer(index, size, GL_FLOAT, false, 0, 0);

            VBOIndices[index] = vboId;

        } finally {
            MemoryUtil.memFree(buffer);
        }
    }

    /**
     * Creates a buffer object to transfer data to the GPU. The reference to the resulting VBO is placed at
     * VBOIndices[index].
     * @param data  data to transfer
     * @param index index of the VBO
     * @param size  number of elements in each attribute
     * @throws IndexOutOfBoundsException if index is out of bounds of VBO; if (index < 0 || index >= VBOIndices.length)
     */
    @SuppressWarnings("Duplicates")
    public void createVBO(int[] data, int index, int size) {
        if (index < 0 || index >= VBOIndices.length) {
            throw new IndexOutOfBoundsException(
                    "Given index out of bounds: " + index + " on size " + VBOIndices.length);
        }

//        IntBuffer buffer = IntBuffer.wrap(data);
        IntBuffer buffer = MemoryUtil.memAllocInt(data.length);
        buffer.put(data).flip();

        try {
            int vboId = glGenBuffers();

            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
            glVertexAttribPointer(index, size, GL_FLOAT, false, 0, 0);

            VBOIndices[index] = vboId;

        } finally {
            MemoryUtil.memFree(buffer);
        }
    }
}
