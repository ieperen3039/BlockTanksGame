#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec3 vertexNormal;
layout (location=2) in vec4 color;
layout (location=3) in vec2 texCoord;

// normal of the vertex in model space
out vec3 mVertexNormal;
// position of the vertex in model space
out vec3 mVertexPosition;
// texture coordinates
out vec2 mTexCoord;
// color transformation
out vec4 mColor;

uniform mat4 modelMatrix;
uniform mat4 viewProjectionMatrix;
uniform mat3 normalMatrix;

void main()
{
    vec4 mPosition = modelMatrix * vec4(position, 1.0);
    gl_Position = viewProjectionMatrix * mPosition;

    mVertexNormal = normalize(normalMatrix * vertexNormal);
    mVertexPosition = mPosition.xyz;
    mTexCoord = vec2(texCoord.x, -texCoord.y);
    mColor = color;
}
