package com.tacz.guns.platform.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
//? if >=1.21.9
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
//? if >=1.21.9
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
//? if >=1.21.9
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * 1.21.9 block entity renderers extract a render state and submit it. TACZ's renderers still draw straight
 * from the block entity, so the state just carries it along and the drawing goes to the main buffer source
 * (flushed by LevelRenderer after the submit pass).
 */
//? if >=1.21.9 {
@Environment(EnvType.CLIENT)
public abstract class LegacyBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, LegacyBlockEntityRenderer.State<T>> {
    public static class State<T> extends BlockEntityRenderState {
        public @Nullable T blockEntity;
        public float partialTick;
    }

    public abstract void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay);

    @Override
    public State<T> createRenderState() {
        return new State<>();
    }

    @Override
    public void extractRenderState(T blockEntity, State<T> state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, crumbling);
        state.blockEntity = blockEntity;
        state.partialTick = partialTick;
    }

    @Override
    public void submit(State<T> state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.blockEntity != null) {
            SubmitScope.run(collector, () -> render(state.blockEntity, state.partialTick, poseStack, Minecraft.getInstance().renderBuffers().bufferSource(), state.lightCoords, OverlayTexture.NO_OVERLAY));
        }
    }
}
//?} else {
/*@Environment(EnvType.CLIENT)
public abstract class LegacyBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    public abstract void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay);

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, Vec3 cameraPos) {
        render(blockEntity, partialTick, poseStack, buffer, packedLight, packedOverlay);
    }

    // the argumentless form came with 1.21.9
    public boolean shouldRenderOffScreen() {
        return false;
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return shouldRenderOffScreen();
    }
}
*///?}
