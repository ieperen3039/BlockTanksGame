#version 330
#pragma optionNV (unroll all)

const float TEX_SCALING = 200; // how far waves are apart
const float EPSILON = 0.002; // inverse of resolution of shadows on waves (small is more accurate)
const float WAVE_SPEED = 0.01; // how fast waves are moving
const float NORMAL_STEEPNESS = 0.01f; // how steep a wave is in shadow calculation

in vec3 mVertexPosition;

out vec4 fragColor;

struct Light
{
    vec4 mPosition;
    vec3 color;
    float intensity;
};

struct Material
{
    vec4 diffuse;
    vec4 specular;
    float reflectance;
};

const int MAX_NUM_LIGHTS = 16;
const float MIN_LIGHT_INTENSITY = 0.001f;

uniform Light lights[MAX_NUM_LIGHTS];
uniform Material material;
uniform vec3 cameraPosition;
uniform vec3 ambientLight;
uniform float currentTime;
uniform mat3 normalMatrix;
uniform sampler2D waterHeightMap;

vec3 calcSurface(Light light, vec3 vNorm, vec3 unitToLight, vec3 eye){
    vec3 result = vec3(0.0, 0.0, 0.0);

    // diffuse component
    float intensity = max(0.0, dot(vNorm, unitToLight));
    if (intensity != 0.0) {
        result += intensity * light.color * material.diffuse.xyz;

        // specular
        vec3 lightReflect = reflect(unitToLight, vNorm);
        vec3 virtualLightPosition = normalize(-lightReflect);
        float linearSpec = max(0.0, dot(virtualLightPosition, normalize(eye)));
        float shine = pow(linearSpec, material.reflectance);
        result += shine * shine * light.color * material.specular.xyz * material.specular.w;
    }

    return result;
}

vec3 pointLightEffect(Light light, vec3 vPos, vec3 vNorm, vec3 eye){
    if (light.intensity == 0.0) return vec3(0.0, 0.0, 0.0);

    vec3 vecToLight = light.mPosition.xyz - vPos;
    float distance = length(vecToLight);

    float maxLight = light.intensity / distance;
    if (maxLight < MIN_LIGHT_INTENSITY) return vec3(0.0, 0.0, 0.0);

    vec3 unitToLight = vecToLight / distance;
    return calcSurface(light, vNorm, unitToLight, eye) / distance;

}

vec3 infiniteLightEffect(Light light, vec3 vNorm, vec3 eye){
    vec3 unitToLight = normalize(light.mPosition.xyz);
    return calcSurface(light, vNorm, unitToLight, eye);
}

vec3 lightEffect(Light l, vec3 vPos, vec3 vNorm, vec3 eye){
    if (l.intensity > 0){
        if (l.mPosition.w == 0){
            return infiniteLightEffect(l, vNorm, eye);
        } else {
            return pointLightEffect(l, vPos, vNorm, eye);
        }
    } else {
        return vec3(0.0, 0.0, 0.0);
    }
}

float getDepth(vec2 coord){
    float shift = currentTime * WAVE_SPEED;
    return texture(waterHeightMap, coord + shift * vec2(0.1, 1)).r *
    texture(waterHeightMap, coord + shift * vec2(1, 0.1)).r;
}

float getDepth(float xCoord, float yCoord){
    return getDepth(vec2(xCoord, yCoord));
}

void main() {
    vec2 texCoord = mVertexPosition.xy / TEX_SCALING;
    float dx = getDepth(texCoord.x - EPSILON, texCoord.y) - getDepth(texCoord.x + EPSILON, texCoord.y);
    float dy = getDepth(texCoord.x, texCoord.y - EPSILON) - getDepth(texCoord.x, texCoord.y + EPSILON);
    vec3 mVertexNormal = normalize(vec3(dx, dy, EPSILON / NORMAL_STEEPNESS));
    mVertexNormal += 0.001 * normalMatrix * mVertexNormal;

    vec3 diffuse = vec3(0.0, 0.0, 0.0);

    for (int i = 0; i < MAX_NUM_LIGHTS; i++){
        diffuse += lightEffect(lights[i], mVertexPosition, mVertexNormal, cameraPosition);
    }

    fragColor = vec4(material.diffuse.xyz * ambientLight + diffuse, material.diffuse.a);
//    fragColor = vec4(mVertexNormal.xyz, material.diffuse.a);
//    fragColor = vec4(mVertexNormal.xyz, 1.0);
}
