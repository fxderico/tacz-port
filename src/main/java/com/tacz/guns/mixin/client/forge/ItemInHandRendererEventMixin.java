package com.tacz.guns.mixin.client.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.platform.forge.client.event.RenderHandEvent;
import com.tacz.guns.platform.forge.common.MinecraftForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * RenderHandEvent. Listeners draw straight into the main buffer source, which renderHandsWithItems flushes afterwards.
 */
@Mixin(/*? if >=26.3 {*//*net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer.class*//*?} else {*/net.minecraft.client.renderer.ItemInHandRenderer.class/*?}*/)
public abstract class ItemInHandRendererEventMixin {
    //? if >=26.3 {
    /*@Inject(method = "submitHandsWithItems", at = @At("HEAD"))
    private void tacz$beforeHands(float partialTicks, PoseStack poseStack, SubmitNodeCollector collector,
                                  net.minecraft.client.renderer.state.level.PlayerRenderState playerState,
                                  net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState state, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new com.tacz.guns.api.client.event.BeforeRenderHandEvent(poseStack));
    }

    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void tacz$renderHand(net.minecraft.client.renderer.state.level.PlayerRenderState playerState,
                                 net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState state, float partialTick,
                                 float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equipProgress,
                                 PoseStack poseStack, SubmitNodeCollector collector, int light, CallbackInfo ci) {
    *///?} else {
    @Inject(method = /*? if >=26.2 {*//*"submitArmWithItem"*//*?} else {*/"renderArmWithItem"/*?}*/, at = @At("HEAD"), cancellable = true)
    private void tacz$renderHand(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swingProgress,
                                 ItemStack stack, float equipProgress, PoseStack poseStack, /*? if >=1.21.9 {*/SubmitNodeCollector collector/*?} else {*//*net.minecraft.client.renderer.MultiBufferSource buffer*//*?}*/, int light, CallbackInfo ci) {
    //?}
        //? if <1.21.9 {
        /*SubmitNodeCollector collector = new SubmitNodeCollector(buffer);
        *///?}
        RenderHandEvent event = new RenderHandEvent(hand, poseStack, /*? if >=1.21.9 {*/Minecraft.getInstance().renderBuffers().bufferSource()/*?} else {*//*buffer*//*?}*/, light,
                partialTick, pitch, swingProgress, equipProgress, stack);
        boolean[] cancelled = new boolean[1];
        com.tacz.guns.platform.client.SubmitScope.run(collector, () -> cancelled[0] = MinecraftForge.EVENT_BUS.post(event));
        if (cancelled[0]) {
            ci.cancel();
        }
    }
}
