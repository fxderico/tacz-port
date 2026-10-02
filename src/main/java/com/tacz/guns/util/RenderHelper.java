package com.tacz.guns.util;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

@Environment(EnvType.CLIENT)
public final class RenderHelper {
    /**
     * Render pipelines don't carry stencil state, so the stencil test set here applies to every draw until it is
     * disabled again. Callers flush their buffers right after each part, like the Forge version did.
     * The main render target has a stencil aspect thanks to {@link com.tacz.guns.mixin.client.GlDeviceStencilMixin}.
     */
    public static void enableItemEntityStencilTest() {
        RenderSystem.assertOnRenderThread();
        GL11.glEnable(GL11.GL_STENCIL_TEST);
    }

    public static void disableItemEntityStencilTest() {
        RenderSystem.assertOnRenderThread();
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    public static void stencilFunc(int func, int ref, int mask) {
        GL11.glStencilFunc(func, ref, mask);
    }

    public static void stencilOp(int sfail, int dpfail, int dppass) {
        GL11.glStencilOp(sfail, dpfail, dppass);
    }

    /**
     * Clears the stencil of the main render target, the Forge version did this on the bound framebuffer.
     */
    public static void clearStencil() {
        RenderSystem.assertOnRenderThread();
        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        GpuTexture colorTexture = target.getColorTexture();
        if (colorTexture == null) {
            return;
        }
        int fbo = ((GlTexture) colorTexture).getFbo(((GlDevice) RenderSystem.getDevice()).directStateAccess(), target.getDepthTexture());
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
        GlStateManager._disableScissorTest();
        GL11.glStencilMask(0xFF);
        GL11.glClearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
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
