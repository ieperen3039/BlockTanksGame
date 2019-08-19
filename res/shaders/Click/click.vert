// @author Geert van Ieperen

#version 330

layout (location = 0) in vec3 position;

uniform mat4 modelMatrix;
uniform mat4 viewProjectionMatrix;

void main() {
	vec4 mPosition = modelMatrix * vec4(position, 1.0);
    gl_Position = viewProjectionMatrix * mPosition;
}
