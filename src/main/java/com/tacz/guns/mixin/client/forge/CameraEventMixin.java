package com.tacz.guns.mixin.client.forge;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
//? if >=26.1 {
/*import com.tacz.guns.platform.forge.client.event.ViewportEvent;
import com.tacz.guns.platform.forge.common.MinecraftForge;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
*///?}

/**
 * Since 26.1 the camera works out its rotation and both fovs itself in Camera#update, which GameRenderer used to do
 * in updateCamera and getFov (see GameRendererEventMixin for older versions, where this mixin stays empty).
 */
@Mixin(Camera.class)
public abstract class CameraEventMixin {
    //? if >=26.1 {
    /*@Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;alignWithEntity(F)V", shift = At.Shift.AFTER))
    private void tacz$computeCameraAngles(DeltaTracker deltaTracker, CallbackInfo ci) {
        Camera camera = (Camera) (Object) this;
        ViewportEvent.ComputeCameraAngles event = new ViewportEvent.ComputeCameraAngles(Minecraft.getInstance().gameRenderer, camera,
                camera.getCameraEntityPartialTicks(deltaTracker), camera.yRot(), camera.xRot(), 0);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getYaw() != camera.yRot() || event.getPitch() != camera.xRot()) {
            ((CameraAccessor) camera).tacz$setRotation(event.getYaw(), event.getPitch());
        }
    }

    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void tacz$computeLevelFov(float partialTicks, CallbackInfoReturnable<Float> cir) {
        tacz$postFov(partialTicks, true, cir);
    }

    @Inject(method = "calculateHudFov", at = @At("RETURN"), cancellable = true)
    private void tacz$computeHandFov(float partialTicks, CallbackInfoReturnable<Float> cir) {
        tacz$postFov(partialTicks, false, cir);
    }

    @org.spongepowered.asm.mixin.Unique
    private void tacz$postFov(float partialTicks, boolean useConfiguredFov, CallbackInfoReturnable<Float> cir) {
        ViewportEvent.ComputeFov event = new ViewportEvent.ComputeFov(Minecraft.getInstance().gameRenderer, (Camera) (Object) this,
                partialTicks, cir.getReturnValueF(), useConfiguredFov);
        MinecraftForge.EVENT_BUS.post(event);
        cir.setReturnValue((float) event.getFOV());
    }
    *///?}
}
