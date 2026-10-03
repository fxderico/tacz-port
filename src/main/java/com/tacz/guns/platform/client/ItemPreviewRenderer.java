package com.tacz.guns.platform.client;

//? if >=1.21.6 {
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.blaze3d.platform.Lighting;

/**
 * 1.21.6 GUIs can't draw 3D models directly anymore, they go through a picture-in-picture texture.
 * Used for the spinning item on the left of the gun smith table.
 */
@Environment(EnvType.CLIENT)
public class ItemPreviewRenderer extends PictureInPictureRenderer<ItemPreviewRenderState> {
    //? if >=26.2 {
    /*public ItemPreviewRenderer() {
    }
    *///?} else {
    public ItemPreviewRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }
    //?}

    @Override
    public Class<ItemPreviewRenderState> getRenderStateClass() {
        return ItemPreviewRenderState.class;
    }

    @Override
    //? if >=26.2 {
    /*protected void renderToTexture(ItemPreviewRenderState state, PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector collector) {
        SubmitScope.run(collector, () -> this.renderItem(state, poseStack, MultiBufferSource.immediate()));
    }
    *///?} else {
    protected void renderToTexture(ItemPreviewRenderState state, PoseStack poseStack) {
        this.renderItem(state, poseStack, this.bufferSource);
    }
    //?}

    private void renderItem(ItemPreviewRenderState state, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource) {
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_FLAT);
        poseStack.scale(1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(state.pitch()));
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yaw()));
        ItemRenderHelper.renderStatic(state.stack(), ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, null, 0);
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2.0F;
    }

    @Override
    protected String getTextureLabel() {
        return "tacz item preview";
    }
}
//?}
