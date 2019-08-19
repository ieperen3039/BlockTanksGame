#version 330

const float LINE_THICKNESS = 0.00f;

// normal of the vertex
in vec3 gVertexNormal;
// position of the vertex
in vec3 gVertexPosition;
// texture coordinates
in vec2 gTexCoord;
// color transformation
in vec4 gColor;
// distances between points in triangle
in vec3 realDistances;
// distance of this pixel to the edges
smooth in vec3 thisDistances;

out vec4 fragColor;

struct PointLight
{
    vec3 color;
    vec3 mPosition;
    float intensity;
};

struct DirectionalLight
{
    vec3 color;
    vec3 direction;
    float intensity;
    mat4 lightSpaceMatrix;
    // Shadow Maps
    bool shadowEnable;
};

struct Material
{
    vec4 diffuse;
    vec4 specular;
    float reflectance;
};

const int MAX_POINT_LIGHTS = 10;

const float ATT_LIN = 0.1f;
const float ATT_EXP = 0.01f;

uniform Material material;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform DirectionalLight directionalLight;

uniform sampler2D texture_sampler;
uniform vec3 ambientLight;
uniform float specularPower;

uniform sampler2D staticShadowMap;
uniform sampler2D dynamicShadowMap;

uniform vec3 cameraPosition;
uniform mat4 viewProjectionMatrix;

uniform bool hasTexture;
uniform bool hasColor;

uniform vec4 lineColor;

// global variables
vec4 diffuse_color;
vec4 specular_color;

// Blinn-Phong lighting
// calculates the diffuse and specular color component caused by one light
vec3 calcBlinnPhong(vec3 light_color, vec3 position, vec3 light_direction, vec3 normal, float attenuatedIntensity) {
    // Diffuse component
    float diff = max(dot(normal, light_direction), 0.0) * attenuatedIntensity;
    vec3 diffuse = diffuse_color.xyz * light_color * diff;

    // Specular component
    vec3 viewDir = normalize(cameraPosition - position);
    vec3 halfwayDir = normalize(light_direction + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0.0), specularPower);
    vec3 specular = specular_color.xyz * attenuatedIntensity * spec * material.reflectance * light_color;

    return (diffuse + specular);
}

// Calculate Attenuation
// calculates the falloff of light on a given distance vector
float calcAttenuation(vec3 light_direction) {
    float distance = length(light_direction);
    return (1.0f / (1.0f + ATT_LIN * distance + ATT_EXP * distance * distance));
}

// calculates the shadow value caused by a lightsource given as a matrix.
float calcShadow2D(mat4 lsMatrix, vec3 vPosition, vec3 vNormal, sampler2D shadowMap) {
    vec4 coord = lsMatrix * vec4(vPosition, 1.0);
    vec3 projCoords = coord.xyz / coord.w;
    projCoords = projCoords * 0.5 + 0.5;

    vec3 lightDir = normalize((lsMatrix * vec4(0, 0, -1, 0)).xyz);

    float currentDepth = projCoords.z;
    if (currentDepth > 1.0f) return 0.0f;

    float bias = max(0.05 * (1.0 - dot(vNormal, lightDir)), 0.001);

    float shadow = 0.0;
    vec2 texelSize = 1.0 / textureSize(shadowMap, 0);
    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            float pcfDepth = texture(shadowMap, projCoords.xy + vec2(x, y) * texelSize).r;
            shadow += currentDepth - bias < pcfDepth ? 1.0 : 0.0;
        }
    }
    shadow = min(1.0, shadow / 9.0);

    return shadow;
}

// caluclates the color addition caused by a point-light
vec3 calcPointLightComponents(PointLight light) {
    if (light.intensity == 0) return vec3(0, 0, 0);

    vec3 light_direction = light.mPosition - gVertexPosition;
    float att = calcAttenuation(light_direction);

    if (att == 0) {
        return vec3(0, 0, 0);
    } else {
        float attenuatedIntensity = att * light.intensity;
        return calcBlinnPhong(light.color, gVertexPosition, normalize(light_direction), gVertexNormal, attenuatedIntensity);
    }
}

// caluclates the color addition caused by an infinitely far away light, including shadows.
vec3 calcDirectionalLightComponents(DirectionalLight light) {

    if (light.intensity == 0.0){
        return vec3(0, 0, 0);

    } else if (light.shadowEnable) {
        vec3 component = vec3(0.0, 0.0, 0.0);

        float staticShadow = 1.0, dynamicShadow = 1.0;
        staticShadow = calcShadow2D(light.lightSpaceMatrix, gVertexPosition, gVertexNormal, staticShadowMap);
        dynamicShadow = calcShadow2D(light.lightSpaceMatrix, gVertexPosition, gVertexNormal, dynamicShadowMap);

        if (staticShadow > 0 && dynamicShadow > 0) {
            component = calcBlinnPhong(light.color, gVertexPosition, normalize(light.direction), gVertexNormal, light.intensity);
        }

        return component * staticShadow * dynamicShadow;

    } else {
        return calcBlinnPhong(light.color, gVertexPosition, normalize(light.direction), gVertexNormal, light.intensity);
    }
}

float sigm(float x){
    return x / sqrt(1 + x * x * x);
}

bool isOnLine(){
    if (LINE_THICKNESS == 0f) return false;

    if (thisDistances.x + thisDistances.y - realDistances.x < LINE_THICKNESS) return true;
    if (thisDistances.y + thisDistances.z - realDistances.y < LINE_THICKNESS) return true;
    if (thisDistances.z + thisDistances.x - realDistances.z < LINE_THICKNESS) return true;
    return false;
}

void main() {
    if (isOnLine()){
        fragColor = lineColor;
        return;
    }

    // Setup Material
    // TODO combine these options
    if (hasTexture){
        diffuse_color = texture(texture_sampler, gTexCoord);

    } else if (hasColor){
        diffuse_color = gColor;

    } else {
        diffuse_color = material.diffuse;
    }

    specular_color = material.specular;

    // diffuse and specular color accumulator
    vec3 diffuseSpecular = vec3(0.0, 0.0, 0.0);

    // Calculate directional light
    diffuseSpecular += calcDirectionalLightComponents(directionalLight);

    // Calculate Point Lights
    diffuseSpecular += calcPointLightComponents(pointLights[0]);
    diffuseSpecular += calcPointLightComponents(pointLights[1]);
    diffuseSpecular += calcPointLightComponents(pointLights[2]);
    diffuseSpecular += calcPointLightComponents(pointLights[3]);
    diffuseSpecular += calcPointLightComponents(pointLights[4]);
    diffuseSpecular += calcPointLightComponents(pointLights[5]);
    diffuseSpecular += calcPointLightComponents(pointLights[6]);
    diffuseSpecular += calcPointLightComponents(pointLights[7]);
    diffuseSpecular += calcPointLightComponents(pointLights[8]);
    diffuseSpecular += calcPointLightComponents(pointLights[9]);

    vec4 col = diffuse_color * vec4(ambientLight, 1.0) + vec4(diffuseSpecular, 0.0);

    fragColor = vec4(sigm(col.x), sigm(col.y), sigm(col.z), col.w);
}
