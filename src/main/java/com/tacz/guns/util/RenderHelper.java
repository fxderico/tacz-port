package com.tacz.guns.util;

//? if >=1.21.5 {
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.GpuTexture;
//?}
import com.mojang.blaze3d.systems.RenderSystem;
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

    private static int stencilClearFbo = -1;
    private static int stencilClearDepthTexture = -1;

    /**
     * Clears the stencil of the main render target, the Forge version did this on the bound framebuffer.
     * Uses a framebuffer of its own holding only the main depth-stencil texture, so it needs nothing from the
     * GPU device internals (package private since 26.1).
     */
    public static void clearStencil() {
        RenderSystem.assertOnRenderThread();
        //? if <1.21.5 {
        /*// render targets were bound for drawing before 1.21.5, the main one is bound while the hands render
        com.mojang.blaze3d.platform.GlStateManager._disableScissorTest();
        GL11.glStencilMask(0xFF);
        GL11.glClearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        *///?} else {
        GpuTexture depthTexture = Minecraft.getInstance().getMainRenderTarget().getDepthTexture();
        //? if neoforge && >=1.21.6 && <26.1 {
        /*// NeoForge wraps GPU objects for validation in dev
        if (depthTexture instanceof net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuTexture validation) {
            depthTexture = validation.getRealTexture();
        }
        *///?}
        if (!(depthTexture instanceof GlTexture glDepthTexture)) {
            return;
        }
        if (stencilClearFbo == -1) {
            stencilClearFbo = GL30.glGenFramebuffers();
        }
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, stencilClearFbo);
        if (stencilClearDepthTexture != glDepthTexture.glId()) {
            // the main target was resized or recreated
            stencilClearDepthTexture = glDepthTexture.glId();
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL11.GL_TEXTURE_2D, stencilClearDepthTexture, 0);
            GL11.glDrawBuffer(GL11.GL_NONE);
            GL11.glReadBuffer(GL11.GL_NONE);
        }
        GlStateManager._disableScissorTest();
        GL11.glStencilMask(0xFF);
        GL11.glClearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        //?}
    }

    public static void renderFirstPersonArm(LocalPlayer player, HumanoidArm hand, PoseStack matrixStack, int combinedLight) {
        Minecraft mc = Minecraft.getInstance();
        AvatarRenderer<net.minecraft.client.player.AbstractClientPlayer> renderer = /*? if >=26.3 || <1.21.9 {*//*(AvatarRenderer<net.minecraft.client.player.AbstractClientPlayer>) mc.getEntityRenderDispatcher().getRenderer(player)*//*?} else {*/mc.getEntityRenderDispatcher().getPlayerRenderer(player)/*?}*/;
        // queued on the game renderer, ItemInHandRenderer flushes the queue right after the hands are done
        SubmitNodeCollector collector = com.tacz.guns.platform.client.SubmitScope.current();
        //? if >=1.21.9 {
        Identifier skin = player.getSkin().body().texturePath();
        if (hand == HumanoidArm.RIGHT) {
            renderer.renderRightHand(matrixStack, collector, combinedLight, skin, player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE));
        } else {
            renderer.renderLeftHand(matrixStack, collector, combinedLight, skin, player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE));
        }
        //?} else if >=1.21.2 {
        /*Identifier skin = player.getSkin().texture();
        if (hand == HumanoidArm.RIGHT) {
            renderer.renderRightHand(matrixStack, collector.bufferSource(), combinedLight, skin, player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE));
        } else {
            renderer.renderLeftHand(matrixStack, collector.bufferSource(), combinedLight, skin, player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE));
        }
        *///?} else {
        /*// the renderer picked skin and sleeves from the player before 1.21.2
        if (hand == HumanoidArm.RIGHT) {
            renderer.renderRightHand(matrixStack, collector.bufferSource(), combinedLight, player);
        } else {
            renderer.renderLeftHand(matrixStack, collector.bufferSource(), combinedLight, player);
        }
        *///?}
    }
}
