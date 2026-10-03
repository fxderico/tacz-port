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
    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/ArmedEntityRenderState;FF)V", at = @At(value = "TAIL"))
    private void render(PoseStack matrixStack, SubmitNodeCollector collector, int packedLight, ArmedEntityRenderState state, float yRot, float xRot, CallbackInfo ci) {
        MuzzleFlashRender.isSelf = false;
        ShellRender.isSelf = false;
        if (state instanceof RenderStateEntity access && access.tacz$getEntity() != null) {
            HumanoidOffhandRender.renderGun(access.tacz$getEntity(), matrixStack, Minecraft.getInstance().renderBuffers().bufferSource(), packedLight);
        }
    }

    @Inject(method = "submitArmWithItem", at = @At(value = "HEAD"), cancellable = true)
    private void renderArmWithItemHead(ArmedEntityRenderState state, ItemStackRenderState itemState, /*? if >=1.21.11 {*/ItemStack itemStack, /*?}*/HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector collector, int packedLight, CallbackInfo ci) {
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

    @Inject(method = "submitArmWithItem", at = @At(value = "TAIL"))
    private void renderArmWithItemTail(ArmedEntityRenderState state, ItemStackRenderState itemState, /*? if >=1.21.11 {*/ItemStack itemStack, /*?}*/HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector collector, int packedLight, CallbackInfo ci) {
        MuzzleFlashRender.isSelf = false;
        ShellRender.isSelf = false;
    }
}
