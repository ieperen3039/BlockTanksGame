// @author Geert van Ieperen

#version 330
#pragma optionNV (unroll all)

layout (location = 0) in vec3 position;

const float HEIGHT_MULTIPLIER = 0.5f; // how high the waves are
const float TEX_SCALING = 200; // how far waves are apart
const float WAVE_SPEED = 0.01; // how fast waves are moving

// position of the vertex
out vec3 mVertexPosition;

uniform float currentTime;
uniform mat4 modelMatrix;
uniform mat4 viewProjectionMatrix;
uniform sampler2D waterHeightMap;

float getDepth(vec2 coord){
    float shift = currentTime * WAVE_SPEED;
    return texture(waterHeightMap, coord + shift * vec2(0.1, 1)).r *
        texture(waterHeightMap, coord + shift * vec2(1, 0.1)).r;
}

float getDepth(float xCoord, float yCoord){
    return getDepth(vec2(xCoord, yCoord));
}

void main()
{
	vec4 mPosition = modelMatrix * vec4(position, 1.0);

    vec2 texCoord = mPosition.xy / TEX_SCALING;
    float waterHeight = getDepth(texCoord) * HEIGHT_MULTIPLIER;
    mPosition = vec4(mPosition.xy, waterHeight, 1.0);

    gl_Position = viewProjectionMatrix * mPosition;
    mVertexPosition = mPosition.xyz;
}
