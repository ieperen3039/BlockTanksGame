#version 330

layout (location = 0) in vec3 origin;// position of the middle of the triangle at t = 0
layout (location = 1) in vec3 movement;// movement per second
layout (location = 2) in vec4 color;
layout (location = 3) in vec2 beginEndTime;// (beginTime, endTime)
layout (location = 4) in int randomInt;// (beginTime, endTime)

uniform mat4 viewProjectionMatrix;
uniform float currentTime;

out vec3 geoMiddle;// position of middle
out vec4 geoColor;
out vec2 geoBeginEndTime;// (beginTime, endTime)
out int randomNumber;

void main(){
    float t = currentTime - beginEndTime.x;
    geoMiddle = origin + (movement * t);
    // calculate vertex position in world-space
    geoBeginEndTime = beginEndTime;
    geoColor = color;
    randomNumber = randomInt;
}
