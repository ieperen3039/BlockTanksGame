// @author Geert van Ieperen

#version 330

layout (location = 0) in vec3 position;

const float HEIGHT_MULTIPLIER = 0.5f; // how high the waves are
const float TEX_SCALING = 200; // how far waves are apart
const float EPSILON = 0.0015; // inverse of resolution of shadows on waves (small is more accurate)
const float WAVE_SPEED = 0.01; // how fast waves are moving
const float NORMAL_STEEPNESS = 0.01f; // how steep a wave is in shadow calculation

// normal of the vertex
out vec3 mVertexNormal;
// position of the vertex
out vec3 mVertexPosition;

uniform float currentTime;
uniform mat4 modelMatrix;
uniform mat4 viewProjectionMatrix;
uniform mat3 normalMatrix;
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

    float dx = getDepth(texCoord.x - EPSILON, texCoord.y) - getDepth(texCoord.x + EPSILON, texCoord.y);
    float dy = getDepth(texCoord.x, texCoord.y - EPSILON) - getDepth(texCoord.x, texCoord.y + EPSILON);
    vec3 vertexNormal = vec3(dx, dy, EPSILON / NORMAL_STEEPNESS);

	mVertexNormal = normalize(normalMatrix * vertexNormal);
    mVertexNormal = normalize(vertexNormal);
    mVertexPosition = mPosition.xyz;
}
