#version 330

in vec3 mVertexPosition;
out vec4 fragColor;

uniform vec4 color;

void main() {
    fragColor = color;
}
