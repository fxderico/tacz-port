package com.tacz.guns.mixin.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Mixin;
//? if fabric && <1.21.5 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.nio.IntBuffer;
*///?}

/**
 * Before 1.21.5's GPU device (see GlDeviceStencilMixin) render targets made their own depth texture. Scopes mask
 * their eyepiece with the stencil buffer, so the depth texture gets a stencil aspect, attached as depth + stencil.
 */
@Mixin(RenderTarget.class)
public abstract class RenderTargetStencilMixin {
    //? if fabric && <1.21.5 {
    /*@WrapOperation(method = "createBuffers", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V", ordinal = 0))
    private void tacz$depthStencilTexture(int target, int level, int internalFormat, int width, int height, int border, int format, int type,
                                         IntBuffer pixels, Operation<Void> original) {
        if (internalFormat == GL30.GL_DEPTH_COMPONENT) {
            original.call(target, level, GL30.GL_DEPTH32F_STENCIL8, width, height, border, GL30.GL_DEPTH_STENCIL, GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV, pixels);
        } else {
            original.call(target, level, internalFormat, width, height, border, format, type, pixels);
        }
    }

    @ModifyConstant(method = "createBuffers", constant = @Constant(intValue = GL30.GL_DEPTH_ATTACHMENT))
    private int tacz$depthStencilAttachment(int attachment) {
        return GL30.GL_DEPTH_STENCIL_ATTACHMENT;
    }
    *///?}
}
