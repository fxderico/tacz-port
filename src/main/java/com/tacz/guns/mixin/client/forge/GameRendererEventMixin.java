package com.tacz.guns.mixin.client.forge;

import com.tacz.guns.platform.forge.client.event.ViewportEvent;
import com.tacz.guns.platform.forge.common.MinecraftForge;
import com.tacz.guns.platform.forge.event.TickEvent;
import com.tacz.guns.mixin.client.forge.CameraAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererEventMixin {
    @Shadow
    @Final
    private Camera mainCamera;

    // render(DeltaTracker, boolean) became render() in 26.3, the tracker is the client's either way
    @Inject(method = "render", at = @At("HEAD"))
    private void tacz$renderTickStart(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new TickEvent.RenderTickEvent(TickEvent.Phase.START, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true)));
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void tacz$renderTickEnd(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new TickEvent.RenderTickEvent(TickEvent.Phase.END, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true)));
    }

    // 26.1 moved these into Camera, see CameraEventMixin
    //? if <26.1 {
    // 1.21.9-1.21.10 set the camera up in renderLevel right before extractCamera, older versions right after Camera#setup
    @Inject(method = /*? if >=1.21.11 {*/"updateCamera", at = @At("TAIL")/*?} else if >=1.21.9 {*//*"extractCamera", at = @At("HEAD")*//*?} else {*//*"renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V", shift = At.Shift.AFTER)*//*?}*/)
    private void tacz$computeCameraAngles(CallbackInfo ci) {
        if (!this.mainCamera.isInitialized()) {
            return;
        }
        ViewportEvent.ComputeCameraAngles event = new ViewportEvent.ComputeCameraAngles((GameRenderer) (Object) this, this.mainCamera,
                this.mainCamera.getPartialTickTime(), this.mainCamera.yRot(), this.mainCamera.xRot(), 0);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getYaw() != this.mainCamera.yRot() || event.getPitch() != this.mainCamera.xRot()) {
            ((CameraAccessor) this.mainCamera).tacz$setRotation(event.getYaw(), event.getPitch());
        }
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void tacz$computeFov(Camera camera, float partialTick, boolean useConfiguredFov, CallbackInfoReturnable<Float> cir) {
        ViewportEvent.ComputeFov event = new ViewportEvent.ComputeFov((GameRenderer) (Object) this, camera, partialTick, cir.getReturnValueF(), useConfiguredFov);
        MinecraftForge.EVENT_BUS.post(event);
        cir.setReturnValue((float) event.getFOV());
    }
    //?}
}
