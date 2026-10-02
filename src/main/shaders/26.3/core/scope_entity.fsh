#version 330
#extension GL_ARB_separate_shader_objects : require

// TACZ scope masking (26.3+, shaderc): minecraft:core/entity plus the TaczScope screen circles

#ifdef GLINT
#include <minecraft:globals.glsl>
#endif
#include <minecraft:fog.glsl>
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:oit.glsl>

uniform sampler2D Sampler0;

#ifdef DISSOLVE
uniform sampler2D DissolveMaskSampler;
#endif

#ifdef GLINT
uniform sampler2D GlintSampler;
#endif

layout(location = 0) in float sphericalVertexDistance;
layout(location = 1) in float cylindricalVertexDistance;
#ifdef PER_FACE_LIGHTING
layout(location = 2) in vec4 vertexPerFaceColorBack;
layout(location = 3) in vec4 vertexPerFaceColorFront;
#else
layout(location = 2) in vec4 vertexColor;
#endif

#ifndef EMISSIVE
layout(location = 4) in vec4 lightMapColor;
#endif

#ifndef NO_OVERLAY
layout(location = 5) in vec4 overlayColor;
#endif

layout(location = 6) in vec2 texCoord0;
#ifdef GLINT
layout(location = 7) in vec2 texCoordGlint;
#endif
layout(location = 8) in vec4 taczClipPos;

// Screen circles in normalized device coordinates, radius in NDC y units, x is scaled by the aspect ratio.
// Lens0: the eyepiece of the scope (w = 1 when the reticle there also needs the aim circle)
// Lens1: a second eyepiece, for sights mounted on scopes (w = 1 when used)
// Aim: grows with the aiming progress, w is the aspect ratio
layout(std140) uniform TaczScope {
    vec4 Lens0;
    vec4 Lens1;
    vec4 Aim;
};

float tacz_circle_distance(vec4 circle) {
    vec2 delta = taczClipPos.xy / taczClipPos.w - circle.xy;
    delta.x *= Aim.w;
    return length(delta) - circle.z;
}

#ifndef OIT_ALPHA_ONLY
layout(location = 0) out vec4 fragColor;
#endif

vec4 calculateFinalColor(vec4 color) {
    #ifndef NO_OVERLAY
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    #endif

    #ifndef EMISSIVE
    color *= lightMapColor;
    #endif

    #ifdef GLINT
    vec4 glintColor = GlintAlpha * texture(GlintSampler, texCoordGlint);
    // Matches BlendFuntion.GLINT
    color.rgb += glintColor.rgb * glintColor.rgb;
    #endif

    #ifdef OIT_ACCUMULATE
    color = sampleColorForAccumulation(color);
    vec4 fogColor = vec4(FogColor.rgb * color.a, FogColor.a);
    #else
    vec4 fogColor = FogColor;
    #endif

    return apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, fogColor);
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

    #ifdef OIT_ADDITIVE
    color.a = min(0.99, color.a);
    #endif

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

    #ifdef GLINT
    color.a = max(color.a, GlintAlpha);
    #endif

    #ifdef OIT_ALPHA_ONLY
    executeAlphaOnlyPhase(gl_FragCoord.z, color.a);
    #else
    fragColor = calculateFinalColor(color);
    #endif
}
