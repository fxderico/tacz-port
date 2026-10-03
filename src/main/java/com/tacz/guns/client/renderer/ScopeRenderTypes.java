package com.tacz.guns.client.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
//? if >=26.1 {
/*import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.platform.CompareOp;
import java.util.Optional;
*///?} else {
import com.mojang.blaze3d.platform.DepthTestFunction;
//?}
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.tacz.guns.GunMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderPipelines;
//? if >=1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderSetup;
//?}
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

/**
 * Render types for the scope stencil passes. The Forge version toggled colorMask / depthMask / depth test around
 * these draws, render pipelines set that state on every draw so it has to live in the pipeline instead.
 */
@Environment(EnvType.CLIENT)
public final class ScopeRenderTypes {
    // OpenGL stencil, replaced by shader masks in 26.2 (Vulkan has no stencil here), see ScopeMask
    //? if <26.2 {
    /**
     * Ocular drawn only into the stencil buffer: no color, no depth write, still depth tested and alpha cut.
     */
    private static final RenderPipeline OCULAR_STENCIL_PIPELINE = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
            .withLocation(id("pipeline/ocular_stencil"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withSampler("Sampler1")
            //? if >=26.1 {
            /*.withColorTargetState(new ColorTargetState(Optional.empty(), ColorTargetState.WRITE_NONE))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
            *///?} else {
            .withColorWrite(false)
            .withDepthWrite(false)
            //?}
            .build();

    /**
     * Reticle of red dot sights, drawn over everything inside its ocular.
     */
    private static final RenderPipeline DIVISION_NO_DEPTH_PIPELINE = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
            .withLocation(id("pipeline/division_no_depth"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withSampler("Sampler1")
            //? if >=26.1 {
            /*.withDepthStencilState(Optional.empty())
            *///?} else {
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            //?}
            .build();

    /**
     * The circle that grows with the aiming progress, only inverts stencil values.
     */
    private static final RenderPipeline SCOPE_VIEW_CIRCLE_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withLocation(id("pipeline/scope_view_circle"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN)
            .withCull(false)
            //? if >=26.1 {
            /*.withColorTargetState(new ColorTargetState(Optional.empty(), ColorTargetState.WRITE_NONE))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
            *///?} else {
            .withColorWrite(false)
            .withDepthWrite(false)
            //?}
            .build();

    //? if >=1.21.11 {
    private static final Function<Identifier, RenderType> OCULAR_STENCIL = Util.memoize(texture -> RenderType.create("tacz_ocular_stencil",
            RenderSetup.builder(OCULAR_STENCIL_PIPELINE).withTexture("Sampler0", texture).useLightmap().useOverlay().createRenderSetup()));

    private static final Function<Identifier, RenderType> DIVISION_NO_DEPTH = Util.memoize(texture -> RenderType.create("tacz_division_no_depth",
            RenderSetup.builder(DIVISION_NO_DEPTH_PIPELINE).withTexture("Sampler0", texture).useLightmap().useOverlay().createRenderSetup()));

    public static final RenderType SCOPE_VIEW_CIRCLE = RenderType.create("tacz_scope_view_circle",
            RenderSetup.builder(SCOPE_VIEW_CIRCLE_PIPELINE).createRenderSetup());
    //?} else {
    /*// before 1.21.11 render types were a pipeline plus composite state shards
    private static final Function<Identifier, RenderType> OCULAR_STENCIL = Util.memoize(texture -> RenderType.create("tacz_ocular_stencil",
            1536, false, false, OCULAR_STENCIL_PIPELINE, textured(texture)));

    private static final Function<Identifier, RenderType> DIVISION_NO_DEPTH = Util.memoize(texture -> RenderType.create("tacz_division_no_depth",
            1536, false, false, DIVISION_NO_DEPTH_PIPELINE, textured(texture)));

    public static final RenderType SCOPE_VIEW_CIRCLE = RenderType.create("tacz_scope_view_circle",
            1536, false, false, SCOPE_VIEW_CIRCLE_PIPELINE, RenderType.CompositeState.builder().createCompositeState(false));

    private static RenderType.CompositeState textured(Identifier texture) {
        return RenderType.CompositeState.builder()
                .setTextureState(new net.minecraft.client.renderer.RenderStateShard.TextureStateShard(texture, false))
                .setLightmapState(net.minecraft.client.renderer.RenderStateShard.LIGHTMAP)
                .setOverlayState(net.minecraft.client.renderer.RenderStateShard.OVERLAY)
                .createCompositeState(true);
    }
    *///?}

    private ScopeRenderTypes() {
    }

    public static RenderType ocularStencil(Identifier texture) {
        return OCULAR_STENCIL.apply(texture);
    }

    public static RenderType divisionNoDepth(Identifier texture) {
        return DIVISION_NO_DEPTH.apply(texture);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(GunMod.MOD_ID, path);
    }
    //?}
}
