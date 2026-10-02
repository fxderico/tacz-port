package com.tacz.guns.mixin.common;

import com.tacz.guns.api.item.gun.AbstractGunItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Forge's IForgeItem#onEntitySwing: guns never play the arm swing animation.
 */
@Mixin(LivingEntity.class)
public abstract class GunSwingMixin {
    //? if >=26.3 {
    /*// 26.3: swing takes the animation and reports whether it started
    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/component/SwingAnimation;Z)Z", at = @At("HEAD"), cancellable = true)
    private void tacz$noGunSwing(InteractionHand hand, net.minecraft.world.item.component.SwingAnimation animation, boolean sendToSwingingEntity,
                                 org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.getItemInHand(hand).getItem() instanceof AbstractGunItem gun && gun.onEntitySwing(self.getItemInHand(hand), self)) {
            cir.setReturnValue(false);
        }
    }
    *///?} else {
    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), cancellable = true)
    private void tacz$noGunSwing(InteractionHand hand, boolean updateSelf, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.getItemInHand(hand).getItem() instanceof AbstractGunItem gun && gun.onEntitySwing(self.getItemInHand(hand), self)) {
            ci.cancel();
        }
    }
    //?}
}
