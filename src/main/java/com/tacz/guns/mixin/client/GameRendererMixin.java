package com.tacz.guns.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.client.event.RenderItemInHandBobEvent;
import com.tacz.guns.api.client.event.RenderLevelBobEvent;
import com.tacz.guns.client.renderer.other.GunHurtBobTweak;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import com.tacz.guns.platform.forge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Unique
    private boolean tacz$useFovSetting;


    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    //? if >=26.1 {
    /*public void onBobHurt(net.minecraft.client.renderer.state.CameraRenderState cameraState, PoseStack pMatrixStack, CallbackInfo ci) {
        float pPartialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    *///?} else {
    public void onBobHurt(PoseStack pMatrixStack, float pPartialTicks, CallbackInfo ci) {
    //?}
        // 取消受伤导致的视角摇晃
        if (Minecraft.getInstance().getCameraEntity() instanceof LocalPlayer player && !player.isDeadOrDying()) {
            if (GunHurtBobTweak.onHurtBobTweak(player, pMatrixStack, pPartialTicks)) {
                ci.cancel();
                return;
            }
        }
        // 触发其他事件
        boolean cancel;
        if (!tacz$useFovSetting) {
            cancel = MinecraftForge.EVENT_BUS.post(new RenderItemInHandBobEvent.BobHurt());
        } else {
            cancel = MinecraftForge.EVENT_BUS.post(new RenderLevelBobEvent.BobHurt());
        }
        if (cancel) {
            ci.cancel();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    //? if >=26.1 {
    /*public void onBobView(net.minecraft.client.renderer.state.CameraRenderState cameraState, PoseStack pMatrixStack, CallbackInfo ci) {
    *///?} else {
    public void onBobView(PoseStack pMatrixStack, float pPartialTicks, CallbackInfo ci) {
    //?}
        boolean cancel;
        if (!tacz$useFovSetting) {
            cancel = MinecraftForge.EVENT_BUS.post(new RenderItemInHandBobEvent.BobView());
        } else {
            cancel = MinecraftForge.EVENT_BUS.post(new RenderLevelBobEvent.BobView());
        }
        if (cancel) {
            ci.cancel();
        }
    }

    //? if >=26.1 {
    /*// 26.1 computes both fovs up front in Camera#update, so mark the pass from the two methods that call the bobs
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void tacz$levelPass(net.minecraft.client.DeltaTracker deltaTracker, CallbackInfo ci) {
        this.tacz$useFovSetting = true;
    }

    @Inject(method = "renderItemInHand", at = @At("HEAD"))
    private void tacz$handPass(net.minecraft.client.renderer.state.CameraRenderState cameraState, float deltaPartialTick, org.joml.Matrix4fc modelViewMatrix, CallbackInfo ci) {
        this.tacz$useFovSetting = false;
    }
    *///?} else {
    /**
     * 是一个 hack 实现。因为 getFov 这个方法只有在构建 投影矩阵 的时候调用。
     * 因此可以根据 getFov 中的 pUseFovSetting 来判断当前准备渲染 Level 还是渲染 HandWithItem 。
     * 至于为什么不直接对 renderItemInHand 这个方法 mixin ，是因为安装了 Optifine 之后，这个方法的内容被大幅度修改了。
     */
    @Inject(method = "getFov", at = @At("HEAD"))
    public void switchRenderType(Camera pActiveRenderInfo, float pPartialTicks, boolean pUseFOVSetting, CallbackInfoReturnable<Float> cir) {
        this.tacz$useFovSetting = pUseFOVSetting;
    }
    //?}
}
