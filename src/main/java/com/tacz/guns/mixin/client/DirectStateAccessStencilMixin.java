package com.tacz.guns.mixin.client;

import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Depth textures carry a stencil aspect (see {@link GlDeviceStencilMixin}), so framebuffers attach them as
 * depth + stencil. Attaching texture 0 there detaches both, same as before.
 */
@Mixin(targets = {
        "com.mojang.blaze3d.opengl.DirectStateAccess$Core",
        "com.mojang.blaze3d.opengl.DirectStateAccess$Emulated",
        "com.mojang.blaze3d.opengl.GlCommandEncoder"
})
public abstract class DirectStateAccessStencilMixin {
    // OpenGL stencil, replaced by shader masks in 26.2 (Vulkan has no stencil here), see ScopeMask
    // NeoForge has a stencil switch of its own (ConfigureMainRenderTargetEvent), see GunModNeoForgeClient
    //? if <26.2 && fabric {
    @ModifyConstant(method = {"bindFrameBufferTextures", "clearDepthTexture"}, constant = @Constant(intValue = GL30.GL_DEPTH_ATTACHMENT))
    private int tacz$depthStencilAttachment(int attachment) {
        return GL30.GL_DEPTH_STENCIL_ATTACHMENT;
    }
    //?}
}
