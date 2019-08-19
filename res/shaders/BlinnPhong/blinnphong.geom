#version 330

layout (triangles) in;
layout (triangle_strip, max_vertices = 3) out;

// normal of the vertex in model space
in vec3[3] mVertexNormal;
// position of the vertex in model space
in vec3[3] mVertexPosition;
// texture coordinates
in vec2[3] mTexCoord;
// color transformation
in vec4[3] mColor;

out vec3 gVertexPosition;
out vec3 gVertexNormal;
out vec2 gTexCoord;
out vec4 gColor;

out vec3 realDistances;
out smooth vec3 thisDistances;

uniform mat4 modelMatrix;
uniform mat4 viewProjectionMatrix;
uniform mat3 normalMatrix;

void main() {
    vec3 A = mVertexPosition[0];
    vec3 B = mVertexPosition[1];
    vec3 C = mVertexPosition[2];
    float AB = distance(A, B);
    float BC = distance(B, C);
    float CA = distance(C, A);

    for (int i = 0; i < 3; i++){
        gl_Position = gl_in[i].gl_Position;
        gVertexPosition = mVertexPosition[i];
        gVertexNormal = mVertexNormal[i];
        gTexCoord = mTexCoord[i];
        gColor = mColor[i];

        realDistances = vec3(AB, BC, CA);

        switch (i){
            case 0: thisDistances = vec3(0.0, AB, CA);
            break;
            case 1: thisDistances = vec3(AB, 0.0, BC);
            break;
            case 2: thisDistances = vec3(CA, BC, 0.0);
            break;
        }

        EmitVertex();
    }
    EndPrimitive();
}
