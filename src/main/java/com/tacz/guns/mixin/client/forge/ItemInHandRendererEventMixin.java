package com.tacz.guns.mixin.client.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.platform.forge.client.event.RenderHandEvent;
import com.tacz.guns.platform.forge.common.MinecraftForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * RenderHandEvent. Listeners draw straight into the main buffer source, which renderHandsWithItems flushes afterwards.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererEventMixin {
    @Inject(method = /*? if >=26.2 {*//*"submitArmWithItem"*//*?} else {*/"renderArmWithItem"/*?}*/, at = @At("HEAD"), cancellable = true)
    private void tacz$renderHand(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swingProgress,
                                 ItemStack stack, float equipProgress, PoseStack poseStack, SubmitNodeCollector collector, int light, CallbackInfo ci) {
        RenderHandEvent event = new RenderHandEvent(hand, poseStack, Minecraft.getInstance().renderBuffers().bufferSource(), light,
                partialTick, pitch, swingProgress, equipProgress, stack);
        boolean[] cancelled = new boolean[1];
        com.tacz.guns.platform.client.SubmitScope.run(collector, () -> cancelled[0] = MinecraftForge.EVENT_BUS.post(event));
        if (cancelled[0]) {
            ci.cancel();
        }
    }
}
