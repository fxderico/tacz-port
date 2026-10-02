package com.tacz.guns.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;

@Environment(EnvType.CLIENT)
public final class RenderHelper {
    /**
     * Stencil masking is not available on the 1.21.5+ render pipelines, kept as no-ops so the callers stay the same.
     */
    public static void enableItemEntityStencilTest() {
    }

    public static void disableItemEntityStencilTest() {
    }

    public static void renderFirstPersonArm(LocalPlayer player, HumanoidArm hand, PoseStack matrixStack, int combinedLight) {
        Minecraft mc = Minecraft.getInstance();
        AvatarRenderer<net.minecraft.client.player.AbstractClientPlayer> renderer = mc.getEntityRenderDispatcher().getPlayerRenderer(player);
        // queued on the game renderer, ItemInHandRenderer flushes the queue right after the hands are done
        SubmitNodeCollector collector = mc.gameRenderer.getSubmitNodeStorage();
        Identifier skin = player.getSkin().body().texturePath();
        if (hand == HumanoidArm.RIGHT) {
            renderer.renderRightHand(matrixStack, collector, combinedLight, skin, player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE));
        } else {
            renderer.renderLeftHand(matrixStack, collector, combinedLight, skin, player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE));
        }
    }
}
