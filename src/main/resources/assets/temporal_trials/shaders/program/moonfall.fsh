#version 150

uniform sampler2D DiffuseSampler;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 uv = texCoord;

    float dist = distance(uv, vec2(0.5, 0.5));
    float vignette = smoothstep(0.4, 0.8, dist);

    vec3 color = texture(DiffuseSampler, uv).rgb;
    color.r += vignette * 0.4;
    color.g -= vignette * 0.2;
    color.b -= vignette * 0.2;

    fragColor = vec4(color, 1.0);
}
