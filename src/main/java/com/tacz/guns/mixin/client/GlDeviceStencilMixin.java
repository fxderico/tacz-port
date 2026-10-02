package com.tacz.guns.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.textures.TextureFormat;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Scopes mask their eyepiece with the stencil buffer, but 1.21.5+ only allocates plain depth textures.
 * Every depth texture is allocated as depth + stencil instead, so blits between render targets still see
 * matching formats. Framebuffers attach them through {@link DirectStateAccessStencilMixin}.
 */
// GlDevice is package private since 26.1
@Mixin(targets = "com.mojang.blaze3d.opengl.GlDevice")
public abstract class GlDeviceStencilMixin {
    @WrapOperation(method = "createTexture(Ljava/lang/String;ILcom/mojang/blaze3d/textures/TextureFormat;IIII)Lcom/mojang/blaze3d/textures/GpuTexture;",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlConst;toGlInternalId(Lcom/mojang/blaze3d/textures/TextureFormat;)I"))
    private int tacz$depthStencilInternalFormat(TextureFormat format, Operation<Integer> original) {
        return format == TextureFormat.DEPTH32 ? GL30.GL_DEPTH32F_STENCIL8 : original.call(format);
    }

    @WrapOperation(method = "createTexture(Ljava/lang/String;ILcom/mojang/blaze3d/textures/TextureFormat;IIII)Lcom/mojang/blaze3d/textures/GpuTexture;",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlConst;toGlExternalId(Lcom/mojang/blaze3d/textures/TextureFormat;)I"))
    private int tacz$depthStencilExternalFormat(TextureFormat format, Operation<Integer> original) {
        return format == TextureFormat.DEPTH32 ? GL30.GL_DEPTH_STENCIL : original.call(format);
    }

    @WrapOperation(method = "createTexture(Ljava/lang/String;ILcom/mojang/blaze3d/textures/TextureFormat;IIII)Lcom/mojang/blaze3d/textures/GpuTexture;",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlConst;toGlType(Lcom/mojang/blaze3d/textures/TextureFormat;)I"))
    private int tacz$depthStencilType(TextureFormat format, Operation<Integer> original) {
        return format == TextureFormat.DEPTH32 ? GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV : original.call(format);
    }
}
