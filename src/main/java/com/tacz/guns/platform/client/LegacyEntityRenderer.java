package com.tacz.guns.platform.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Same idea as LegacyBlockEntityRenderer, for entities: the render state keeps the entity and the old
 * render method draws into the main buffer source.
 */
@Environment(EnvType.CLIENT)
public abstract class LegacyEntityRenderer<T extends Entity> extends EntityRenderer<T, LegacyEntityRenderer.State<T>> {
    public static class State<T> extends EntityRenderState {
        public @Nullable T entity;
        public float partialTick;
        public float yaw;
    }

    protected LegacyEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public abstract void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight);

    @Override
    public State<T> createRenderState() {
        return new State<>();
    }

    @Override
    public void extractRenderState(T entity, State<T> state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.entity = entity;
        state.partialTick = partialTick;
        state.yaw = entity.getYRot(partialTick);
    }

    @Override
    public void submit(State<T> state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (state.entity != null) {
            render(state.entity, state.yaw, state.partialTick, poseStack, Minecraft.getInstance().renderBuffers().bufferSource(), state.lightCoords);
        }
    }
}
