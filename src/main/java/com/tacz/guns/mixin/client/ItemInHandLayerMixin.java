package com.tacz.guns.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.model.functional.MuzzleFlashRender;
import com.tacz.guns.client.model.functional.ShellRender;
import com.tacz.guns.client.renderer.RenderStateEntity;
import com.tacz.guns.client.renderer.other.HumanoidOffhandRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {
    // arguments by type: 1.21.9 turned render(.., MultiBufferSource, ..) into submit(.., SubmitNodeCollector, ..)
    @Inject(method = /*? if >=1.21.9 {*/"submit"/*?} else {*//*"render"*//*?}*/, at = @At(value = "TAIL"))
    private void render(CallbackInfo ci, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) PoseStack matrixStack,
                        @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) int packedLight,
                        @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) ArmedEntityRenderState state) {
        MuzzleFlashRender.isSelf = false;
        ShellRender.isSelf = false;
        if (state instanceof RenderStateEntity access && access.tacz$getEntity() != null) {
            HumanoidOffhandRender.renderGun(access.tacz$getEntity(), matrixStack, Minecraft.getInstance().renderBuffers().bufferSource(), packedLight);
        }
    }

    @Inject(method = /*? if >=1.21.9 {*/"submitArmWithItem"/*?} else {*//*"renderArmWithItem"*//*?}*/, at = @At(value = "HEAD"), cancellable = true)
    private void renderArmWithItemHead(CallbackInfo ci, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) ArmedEntityRenderState state,
                                       @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) HumanoidArm arm) {
        LivingEntity livingEntity = state instanceof RenderStateEntity access ? access.tacz$getEntity() : null;
        if (livingEntity == null) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (livingEntity.equals(player)) {
            MuzzleFlashRender.isSelf = true;
            ShellRender.isSelf = true;
        }
        if (IGun.mainHandHoldGun(livingEntity) && arm == HumanoidArm.LEFT) {
            ci.cancel();
        }
    }

    @Inject(method = /*? if >=1.21.9 {*/"submitArmWithItem"/*?} else {*//*"renderArmWithItem"*//*?}*/, at = @At(value = "TAIL"))
    private void renderArmWithItemTail(CallbackInfo ci) {
        MuzzleFlashRender.isSelf = false;
        ShellRender.isSelf = false;
    }
}
