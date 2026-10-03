package com.tacz.guns.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.client.event.BeforeRenderHandEvent;
import com.tacz.guns.api.client.other.KeepingItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import com.tacz.guns.platform.forge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hand item bookkeeping; 26.3 moved it out of the renderer into LocalPlayer#firstPersonHandsAndItems.
 */
@Mixin(/*? if >=26.3 {*//*net.minecraft.client.player.FirstPersonHandsAndItems.class*//*?} else {*/net.minecraft.client.renderer.ItemInHandRenderer.class/*?}*/)
public class ItemInHandRendererMixin implements KeepingItemRenderer {
    @Shadow
    private float mainHandHeight;
    @Shadow
    private float oMainHandHeight;
    @Shadow
    private ItemStack mainHandItem;
    @Unique
    private ItemStack tacz$KeepItem;
    @Unique
    private long tacz$KeepTimeMs;
    @Unique
    private long tacz$KeepTimestamp;

    // 26.3: posted from ItemInHandRendererEventMixin, the renderer is a separate class there
    //? if <26.3 {
    @Inject(method = /*? if >=26.2 {*//*"submitHandsWithItems"*//*?} else {*/"renderHandsWithItems"/*?}*/, at = @At("HEAD"))
    public void beforeHandRender(CallbackInfo ci, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) PoseStack pMatrixStack) {
        MinecraftForge.EVENT_BUS.post(new BeforeRenderHandEvent(pMatrixStack));
    }
    //?}

    @Inject(method = "tick", at = @At("HEAD"))
    public void cancelEquippedProgress(CallbackInfo ci) {
//        if (Minecraft.getInstance().player == null) {
//            return;
//        }
//        if (tacz$KeepItem != null) {
//            long time = System.currentTimeMillis() - tacz$KeepTimestamp;
//            if (time < tacz$KeepTimeMs) {
//                mainHandHeight = 1.0f;
//                oMainHandHeight = 1.0f;
//                mainHandItem = tacz$KeepItem;
//                return;
//            }
//        }
//        ItemStack itemStack = Minecraft.getInstance().player.getMainHandItem();
//        IGun iGun = IGun.getIGunOrNull(itemStack);
//        if (iGun != null) {
//            mainHandHeight = 1.0f;
//            oMainHandHeight = 1.0f;
//            mainHandItem = itemStack;
//        }
    }

    @Unique
    @Override
    public void keep(ItemStack itemStack, long timeMs) {
        long time = System.currentTimeMillis() - tacz$KeepTimestamp;
        if (time < tacz$KeepTimeMs) {
            return;
        }
        this.tacz$KeepTimeMs = timeMs;
        this.tacz$KeepTimestamp = System.currentTimeMillis();
        this.tacz$KeepItem = itemStack;
        this.mainHandItem = itemStack;
    }

    @Override
    public ItemStack getCurrentItem() {
        if (Minecraft.getInstance().player == null) {
            return mainHandItem;
        }
        if (tacz$KeepItem != null) {
            long time = System.currentTimeMillis() - tacz$KeepTimestamp;
            if (time < tacz$KeepTimeMs) {
                return tacz$KeepItem;
            } else {
                tacz$KeepItem = null;
            }
        }
        return mainHandItem;
    }
}
