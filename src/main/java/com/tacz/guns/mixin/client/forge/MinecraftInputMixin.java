package com.tacz.guns.mixin.client.forge;

import com.tacz.guns.platform.forge.client.event.InputEvent;
import com.tacz.guns.platform.forge.common.MinecraftForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.Options;

/**
 * Fires InputEvent.InteractionKeyMappingTriggered like Forge does for attack / use / pick.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftInputMixin {
    @Shadow
    @Nullable
    public LocalPlayer player;
    @Shadow
    @Nullable
    public MultiPlayerGameMode gameMode;
    @Shadow
    @Final
    public Options options;

    private boolean tacz$fire(int button, InteractionHand hand) {
        InputEvent.InteractionKeyMappingTriggered event = new InputEvent.InteractionKeyMappingTriggered(button,
                button == 0 ? options.keyAttack : button == 1 ? options.keyUse : options.keyPickItem, hand);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            if (event.shouldSwingHand() && player != null) {
                //? if >=26.3 {
                /*player.swing(hand, net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);
                *///?} else {
                player.swing(hand);
                //?}
            }
            return true;
        }
        return false;
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void tacz$startAttack(CallbackInfoReturnable<Boolean> cir) {
        if (player != null && tacz$fire(0, InteractionHand.MAIN_HAND)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void tacz$continueAttack(boolean leftClick, CallbackInfo ci) {
        if (leftClick && player != null && !player.isUsingItem() && tacz$fire(0, InteractionHand.MAIN_HAND)) {
            if (gameMode != null) {
                gameMode.stopDestroyBlock();
            }
            ci.cancel();
        }
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void tacz$startUseItem(CallbackInfo ci) {
        if (player != null && tacz$fire(1, InteractionHand.MAIN_HAND)) {
            ci.cancel();
        }
    }

    @Inject(method = "pickBlock", at = @At("HEAD"), cancellable = true)
    private void tacz$pickBlock(CallbackInfo ci) {
        if (player != null && tacz$fire(2, InteractionHand.MAIN_HAND)) {
            ci.cancel();
        }
    }
}
