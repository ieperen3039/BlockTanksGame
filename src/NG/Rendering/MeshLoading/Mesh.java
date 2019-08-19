package NG.Rendering.MeshLoading;

import NG.Rendering.MatrixStack.SGL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Geert van Ieperen created on 17-11-2017.
 */
public interface Mesh {
    /**
     * draws the object on the gl buffer. This method may only be called by a class that implements GL2
     * @param lock a non-null object that can only be generated by a GL2 object.
     */
    void render(SGL.Painter lock);

    void dispose();

    /**
     * a record class to describe a plane by indices. This object is not robust, thus one may not assume it is
     * immutable.
     */
    class Face {
        /** indices of the vertices of this face */
        public final int[] vert;
        /** indices of the normals of this face */
        public final int[] norm;
        /** indices of the texture coordinates of this face */
        public final int[] tex;
        /** indices of the colors of this face */
        public final int[] col;

        public Face(int[] vertexIndices, int[] normalIndices, int[] textureIndices, int[] colors) {
            int size = vertexIndices.length;
            assert (normalIndices.length == size && textureIndices.length == size);

            this.vert = vertexIndices;
            this.norm = normalIndices;
            this.tex = textureIndices;
            this.col = colors;
        }

        /**
         * a description of a plane, with the indices of the vertices and normals given. The indices should refer to a
         * list of vertices and normals that belong to a list of faces where this face is part of.
         */
        public Face(int[] vertices, int[] normals) {
            int size = vertices.length;
            assert (normals.length == size);

            this.vert = vertices;
            this.norm = normals;
            this.tex = null;
            this.col = null;
        }

        /**
         * a description of a plane, with the indices of the vertices and normals given. The indices should refer to a
         * list of vertices and normals that belong to a list of faces where this face is part of.
         */
        public Face(int[] vertices, int nInd) {
            this(vertices, new int[vertices.length]);
            Arrays.fill(norm, nInd);
        }

        public int size() {
            return vert.length; // vertices is always non-null
        }

        /**
         * @return this face split in triangles (or this if this is a triangle)
         */
        public List<Face> triangulate() {
            if (size() == 3) {
                return Collections.singletonList(this);

            } else {
                List<Face> result = new ArrayList<>();
                for (int i = 0; i < size() - 2; i++) {
                    result.add(new Face(
                            new int[]{vert[i], vert[1 + i], vert[2 + 1]},
                            new int[]{norm[i], norm[1 + i], norm[2 + 1]},
                            new int[]{tex [i], tex [1 + i], tex [2 + 1]},
                            new int[]{col [i], col [1 + i], col [2 + 1]}
                    ));
                }
                return result;
            }
        }
    }
}
