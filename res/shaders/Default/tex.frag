#version 330 core
out vec4 FragColor;

in vec3 ourColor;
in vec2 TexCoord;

// texture samplers
uniform sampler2D texture1;
uniform sampler2D texture2;

void main()
{
    vec2 texelSize = 1.0 / textureSize(texture1, 0);
	// linearly interpolate between both textures (80% container, 20% awesomeface)
	float pcfDepth = texture(texture2, TexCoord * texelSize).r;
	FragColor = vec4(1.0, 1.0, 1.0, pcfDepth);
}
