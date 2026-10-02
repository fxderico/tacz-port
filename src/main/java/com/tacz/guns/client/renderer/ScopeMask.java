package com.tacz.guns.client.renderer;

//? if >=26.2 {
/*import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.GunMod;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/^*
 * Scope eyepieces without a stencil buffer (26.2 render pipelines have none and may run on Vulkan).
 * The eyepieces and the aim circle are projected to screen circles once per frame and handed to
 * tacz:core/scope_entity in the TaczScope uniform block; each scope part discards its own fragments:
 * <ul>
 *     <li>outside lens: scope and gun bodies, nothing of them inside an eyepiece</li>
 *     <li>ocular mask: the black eyepiece, cut open by the aim circle</li>
 *     <li>reticle: only inside the eyepiece (and the aim circle for scopes)</li>
 * </ul>
 *^/
@Environment(EnvType.CLIENT)
public final class ScopeMask {
    private static final int UBO_SIZE = 3 * 16;
    private static final BindGroupLayout SCOPE_LAYOUT = BindGroupLayout.builder().withUniform("TaczScope", UniformType.UNIFORM_BUFFER).build();

    private static final RenderPipeline OUTSIDE_LENS_PIPELINE = pipeline("outside_lens", "TACZ_OUTSIDE_LENS", true);
    private static final RenderPipeline OCULAR_MASK_PIPELINE = pipeline("ocular_mask", "TACZ_OCULAR_MASK", true);
    private static final RenderPipeline RETICLE_PIPELINE = pipeline("reticle", "TACZ_RETICLE", true);
    private static final RenderPipeline RETICLE_NO_DEPTH_PIPELINE = pipeline("reticle_no_depth", "TACZ_RETICLE", false);
    private static final Set<RenderPipeline> PIPELINES = Set.of(OUTSIDE_LENS_PIPELINE, OCULAR_MASK_PIPELINE, RETICLE_PIPELINE, RETICLE_NO_DEPTH_PIPELINE);

    private static final Function<Identifier, RenderType> OUTSIDE_LENS = renderTypes("tacz_scope_outside_lens", OUTSIDE_LENS_PIPELINE);
    private static final Function<Identifier, RenderType> OCULAR_MASK = renderTypes("tacz_scope_ocular_mask", OCULAR_MASK_PIPELINE);
    private static final Function<Identifier, RenderType> RETICLE = renderTypes("tacz_scope_reticle", RETICLE_PIPELINE);
    private static final Function<Identifier, RenderType> RETICLE_NO_DEPTH = renderTypes("tacz_scope_reticle_no_depth", RETICLE_NO_DEPTH_PIPELINE);

    private static final Vector4f lens0 = new Vector4f();
    private static final Vector4f lens1 = new Vector4f();
    private static final Vector4f aim = new Vector4f();
    private static @Nullable GpuBuffer ubo;
    private static boolean dirty = true;

    private ScopeMask() {
    }

    private static RenderPipeline pipeline(String name, String define, boolean depthTest) {
        RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "pipeline/scope_" + name))
                .withVertexShader(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "core/scope_entity"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "core/scope_entity"))
                .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                .withShaderDefine("PER_FACE_LIGHTING")
                .withShaderDefine(define)
                .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                .withBindGroupLayout(SCOPE_LAYOUT)
                .withCull(false);
        if (!depthTest) {
            builder.withDepthStencilState(Optional.empty());
        }
        return builder.build();
    }

    private static Function<Identifier, RenderType> renderTypes(String name, RenderPipeline pipeline) {
        return Util.memoize(texture -> RenderType.create(name,
                RenderSetup.builder(pipeline).withTexture("Sampler0", texture).useLightmap().useOverlay().createRenderSetup()));
    }

    public static RenderType outsideLens(Identifier texture) {
        return OUTSIDE_LENS.apply(texture);
    }

    public static RenderType ocularMask(Identifier texture) {
        return OCULAR_MASK.apply(texture);
    }

    public static RenderType reticle(Identifier texture, boolean depthTest) {
        return (depthTest ? RETICLE : RETICLE_NO_DEPTH).apply(texture);
    }

    /^*
     * Sets the eyepieces for this frame.
     *
     * @param primary   eyepiece of the scope, or of the sight when there is no scope
     * @param needsAim  whether the reticle in the primary eyepiece only shows inside the aim circle (scopes)
     * @param secondary eyepiece of a sight mounted on the scope, or null
     * @param aimRadius aim circle radius in view space at a distance of 90, like the Forge version drew it
     *^/
    public static void setLenses(PoseStack poseStack, ItemDisplayContext transformType, List<BedrockPart> primary, boolean needsAim,
                                 @Nullable List<BedrockPart> secondary, float aimRadius) {
        float aspect = (float) Minecraft.getInstance().getWindow().getWidth() / Math.max(1, Minecraft.getInstance().getWindow().getHeight());
        float focal = 1.0F / (float) Math.tan(Math.toRadians(HandProjection.hudFov) / 2.0);
        Matrix4f toClip = new Matrix4f().perspective((float) Math.toRadians(HandProjection.hudFov), aspect, 0.05F, 100.0F)
                .mul(RenderSystem.getModelViewStack());
        projectLens(poseStack, transformType, primary, toClip, aspect, lens0);
        lens0.w = needsAim ? 1 : 0;
        if (secondary != null) {
            projectLens(poseStack, transformType, secondary, toClip, aspect, lens1);
            lens1.w = 1;
        } else {
            lens1.set(0, 0, 0, 0);
        }
        aim.set(lens0.x, lens0.y, Math.max(0, focal * aimRadius / 90.0F), aspect);
        dirty = true;
        // submitting happens before the frame's render passes open, safe to write the buffer now
        upload();
    }

    /^*
     * Screen circle inscribed in the projected eyepiece geometry.
     *^/
    private static void projectLens(PoseStack poseStack, ItemDisplayContext transformType, List<BedrockPart> path, Matrix4f toClip, float aspect, Vector4f out) {
        PositionCapture capture = new PositionCapture();
        poseStack.pushPose();
        for (int i = 0; i < path.size() - 1; ++i) {
            path.get(i).translateAndRotateAndScale(poseStack);
        }
        BedrockPart part = path.get(path.size() - 1);
        boolean visible = part.visible;
        part.visible = true;
        part.render(poseStack, transformType, capture, 0, 0);
        part.visible = visible;
        poseStack.popPose();
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        Vector4f point = new Vector4f();
        for (int i = 0; i < capture.positions.size(); i += 3) {
            point.set(capture.positions.get(i), capture.positions.get(i + 1), capture.positions.get(i + 2), 1.0F);
            toClip.transform(point);
            if (point.w <= 0) {
                continue;
            }
            float x = point.x / point.w, y = point.y / point.w;
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        if (minX > maxX) {
            out.set(0, 0, 0, 0);
            return;
        }
        float radius = Math.min((maxX - minX) * aspect, maxY - minY) / 2.0F;
        out.set((minX + maxX) / 2.0F, (minY + maxY) / 2.0F, radius, 0);
    }

    /^*
     * Called for every draw of a render type, binds the TaczScope block to the scope pipelines.
     *^/
    public static void bind(RenderPass renderPass, RenderPipeline pipeline) {
        if (!PIPELINES.contains(pipeline)) {
            return;
        }
        if (ubo == null) {
            ubo = RenderSystem.getDevice().createBuffer(() -> "TACZ scope mask", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, UBO_SIZE);
        }
        renderPass.setUniform("TaczScope", ubo.slice());
    }

    /^*
     * Uploads the circles, must run outside of a render pass (between submitting and drawing a frame).
     *^/
    public static void upload() {
        if (!dirty) {
            return;
        }
        if (ubo == null) {
            ubo = RenderSystem.getDevice().createBuffer(() -> "TACZ scope mask", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, UBO_SIZE);
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer data = Std140Builder.onStack(stack, UBO_SIZE).putVec4(lens0).putVec4(lens1).putVec4(aim).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(ubo.slice(), data);
        }
        dirty = false;
    }

    /^*
     * Only keeps vertex positions.
     *^/
    private static final class PositionCapture implements VertexConsumer {
        private final it.unimi.dsi.fastutil.floats.FloatArrayList positions = new it.unimi.dsi.fastutil.floats.FloatArrayList();

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.positions.add(x);
            this.positions.add(y);
            this.positions.add(z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            return this;
        }
    }
}
*///?}
