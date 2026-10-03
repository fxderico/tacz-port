package com.tacz.guns.platform.client.compat;

//? if <1.21.6 {
/*import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.platform.client.ItemRenderHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/^*
 * The rotating 3D item of the gun smith table, drawn straight into the GUI like before 1.21.6
 * (newer versions go through a picture-in-picture texture, see ItemPreviewRenderer).
 ^/
@Environment(EnvType.CLIENT)
public final class ItemPreview {
    private ItemPreview() {
    }

    public static void render(GuiGraphics graphics, ItemStack stack, float pitch, float yaw, int x0, int y0, int x1, int y1, float scale) {
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        graphics.flush();
        graphics.enableScissor(x0, y0, x1, y1);
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate((x0 + x1) / 2.0F, (y0 + y1) / 2.0F, 200.0F);
        poseStack.scale(scale, -scale, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        Lighting.setupForFlatItems();
        ItemRenderHelper.renderStatic(stack, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, null, 0);
        bufferSource.endBatch();
        Lighting.setupFor3DItems();
        poseStack.popPose();
        graphics.disableScissor();
    }
}
*///?}
