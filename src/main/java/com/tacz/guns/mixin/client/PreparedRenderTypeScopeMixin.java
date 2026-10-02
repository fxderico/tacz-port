package com.tacz.guns.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
//? if >=26.2 {
/*import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderPass;
import com.tacz.guns.client.renderer.ScopeMask;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?}

/**
 * Binds the TaczScope uniform block of the scope pipelines (26.2+, see ScopeMask). The class only exists since
 * 26.2, the mixin is pseudo so older versions simply skip it.
 */
@Pseudo
@Mixin(targets = "net.minecraft.client.renderer.rendertype.PreparedRenderType")
public abstract class PreparedRenderTypeScopeMixin {
    //? if >=26.2 {
    /*@Inject(method = "drawFromBuffer(Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/IndexType;III)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;bindDefaultUniforms(Lcom/mojang/blaze3d/systems/RenderPass;)V", shift = At.Shift.AFTER))
    private void tacz$bindScopeMask(CallbackInfo ci, @Local RenderPass renderPass) {
        ScopeMask.bind(renderPass, ((PreparedRenderType) (Object) this).pipeline());
    }
    *///?}
}
