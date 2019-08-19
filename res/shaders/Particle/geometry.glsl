#version 330

layout (points) in;
layout (triangle_strip, max_vertices = 3) out;

in vec3[1] geoMiddle;// position of middle
in vec4[1] geoColor;
in vec2[1] geoBeginEndTime;// (beginTime, endTime)
in int[1] randomNumber;

out vec4 fragColor;

uniform mat4 viewProjectionMatrix;
uniform float currentTime;
uniform float particleSize;

int randomState;
const vec3 A = vec3(0.0, 0.0, 1.0);
const vec3 B = vec3(0.7, 0.0, -0.4);// cos(60)
const vec3 C = vec3(-0.7, 0.0, -0.4);

/* The state word must be initialized to non-zero 
 */
float rand() {
    int x = randomState;
    /* Algorithm "xor" from p. 4 of Marsaglia, "Xorshift RNGs" */
    x ^= x << 13;
    x ^= x >> 17;
    x ^= x << 5;

    randomState = x;
    float result = x % 256;
    return result / 256f;
}

void generateVertex(vec3 rel, vec3 rot, float angle){
    fragColor = geoColor[0];

    float x = rel.x, y = rel.y, z = rel.z;

    float hangle = angle * 0.5f;
    float sinAngle = sin(hangle);
    float qx = rot.x * sinAngle, qy = rot.y * sinAngle, qz = rot.z * sinAngle;
    float qw = cos(hangle);
    float w2 = qw * qw, x2 = qx * qx, y2 = qy * qy, z2 = qz * qz, zw = qz * qw;
    float xy = qx * qy, xz = qx * qz, yw = qy * qw, yz = qy * qz, xw = qx * qw;
    float nx = (w2 + x2 - z2 - y2) * x + (-zw + xy - zw + xy) * y + (yw + xz + xz + yw) * z;
    float ny = (xy + zw + zw + xy) * x + (y2 - z2 + w2 - x2) * y + (yz + yz - xw - xw) * z;
    float nz = (xz - yw + xz - yw) * x + (yz + yz + xw + xw) * y + (z2 - y2 - x2 + w2) * z;
    rel = vec3(nx, ny, nz) * particleSize;

    gl_Position = viewProjectionMatrix * vec4(geoMiddle[0] + rel, 1.0);
    EmitVertex();
}

void main() {
    randomState = randomNumber[0];// set random seed

    float t = currentTime - geoBeginEndTime[0].x;
    vec3 rot = normalize(vec3(rand() - 0.5, rand() - 0.5, rand() - 0.5));// chance on 0 vector is simply too small
    float angle = 8 * t * rand() + rand() * 6.28;

    // only draw if still visible
    if (t > 0 && currentTime < geoBeginEndTime[0].y) {

        generateVertex(A, rot, angle);
        generateVertex(B, rot, angle);
        generateVertex(C, rot, angle);

        EndPrimitive();
    }
}
