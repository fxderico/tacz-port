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
//? if >=1.21.4 {
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
//?} else if >=1.21.2 {
/*import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
*///?}
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
    // (with the descriptor before 1.21.2, the generic layer has a bridge method taking any entity)
    @Inject(method = /*? if >=1.21.9 {*/"submit"/*?} else if >=1.21.2 {*//*"render"*//*?} else {*//*"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V"*//*?}*/, at = @At(value = "TAIL"))
    private void render(CallbackInfo ci, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) PoseStack matrixStack,
                        @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) int packedLight,
                        @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) /*? if >=1.21.4 {*/ArmedEntityRenderState/*?} else if >=1.21.2 {*//*LivingEntityRenderState*//*?} else {*//*LivingEntity*//*?}*/ state) {
        MuzzleFlashRender.isSelf = false;
        ShellRender.isSelf = false;
        // render states came with 1.21.2, before that the layer got the entity itself
        LivingEntity livingEntity = /*? if >=1.21.2 {*/state instanceof RenderStateEntity access ? access.tacz$getEntity() : null/*?} else {*//*state*//*?}*/;
        if (livingEntity != null) {
            HumanoidOffhandRender.renderGun(livingEntity, matrixStack, Minecraft.getInstance().renderBuffers().bufferSource(), packedLight);
        }
    }

    @Inject(method = /*? if >=1.21.9 {*/"submitArmWithItem"/*?} else {*//*"renderArmWithItem"*//*?}*/, at = @At(value = "HEAD"), cancellable = true)
    private void renderArmWithItemHead(CallbackInfo ci, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) /*? if >=1.21.4 {*/ArmedEntityRenderState/*?} else if >=1.21.2 {*//*LivingEntityRenderState*//*?} else {*//*LivingEntity*//*?}*/ state,
                                       @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) HumanoidArm arm) {
        LivingEntity livingEntity = /*? if >=1.21.2 {*/state instanceof RenderStateEntity access ? access.tacz$getEntity() : null/*?} else {*//*state*//*?}*/;
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
