#version 330

// TACZ scope masking (26.2+): minecraft:core/entity plus the TaczScope screen circles

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

#ifdef DISSOLVE
uniform sampler2D DissolveMaskSampler;
#endif

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
#ifdef PER_FACE_LIGHTING
in vec4 vertexPerFaceColorBack;
in vec4 vertexPerFaceColorFront;
#else
in vec4 vertexColor;
#endif

#ifndef EMISSIVE
in vec4 lightMapColor;
#endif

#ifndef NO_OVERLAY
in vec4 overlayColor;
#endif

in vec2 texCoord0;

out vec4 fragColor;

// Screen circles in normalized device coordinates, radius in NDC y units, x is scaled by the aspect ratio.
// Lens0: the eyepiece of the scope (w = 1 when the reticle there also needs the aim circle)
// Lens1: a second eyepiece, for sights mounted on scopes (w = 1 when used)
// Aim: grows with the aiming progress, w is the aspect ratio
layout(std140) uniform TaczScope {
    vec4 Lens0;
    vec4 Lens1;
    vec4 Aim;
};

in vec4 taczClipPos;

float tacz_circle_distance(vec4 circle) {
    vec2 delta = taczClipPos.xy / taczClipPos.w - circle.xy;
    delta.x *= Aim.w;
    return length(delta) - circle.z;
}

void main() {
    bool inLens0 = Lens0.z > 0.0 && tacz_circle_distance(Lens0) < 0.0;
    bool inLens1 = Lens1.w > 0.0 && tacz_circle_distance(Lens1) < 0.0;
    bool inAim = tacz_circle_distance(Aim) < 0.0;
#ifdef TACZ_OUTSIDE_LENS
    // scope and gun bodies stay out of the eyepiece
    if (inLens0 || inLens1) {
        discard;
    }
#endif
#ifdef TACZ_OCULAR_MASK
    // the black eyepiece, opened up by the aim circle
    if (inAim) {
        discard;
    }
#endif
#ifdef TACZ_RETICLE
    if (!(inLens1 || (inLens0 && (Lens0.w == 0.0 || inAim)))) {
        discard;
    }
#endif
    vec4 color = texture(Sampler0, texCoord0);
#ifdef ALPHA_CUTOUT
    if (color.a < ALPHA_CUTOUT) {
        discard;
    }
#endif

#ifdef PER_FACE_LIGHTING
    vec4 faceVertexColor = gl_FrontFacing ? vertexPerFaceColorFront : vertexPerFaceColorBack;
#else
    vec4 faceVertexColor = vertexColor;
#endif

#ifdef DISSOLVE
    if (faceVertexColor.a < texture(DissolveMaskSampler, texCoord0).a) {
        discard;
    }
    // The dissolve effect entirely replaces translucency
    faceVertexColor.a = 1.0;
#endif

    color *= faceVertexColor * ColorModulator;
#ifndef NO_OVERLAY
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
#endif
#ifndef EMISSIVE
    color *= lightMapColor;
#endif

    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
