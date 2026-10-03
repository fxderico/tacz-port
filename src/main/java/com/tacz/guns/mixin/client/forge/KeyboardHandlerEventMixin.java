package com.tacz.guns.mixin.client.forge;

import com.tacz.guns.platform.forge.client.event.InputEvent;
import com.tacz.guns.platform.forge.common.MinecraftForge;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
//? if >=1.21.9
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerEventMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "keyPress", at = @At("TAIL"))
    //? if >=1.21.9 {
    private void tacz$keyInput(long window, int action, KeyEvent keyEvent, CallbackInfo ci) {
        if (window == this.minecraft.getWindow().handle()) {
            MinecraftForge.EVENT_BUS.post(new InputEvent.Key(keyEvent.key(), keyEvent./*? if >=26.3 {*//*keycode*//*?} else {*/scancode/*?}*/(), action, keyEvent.modifiers()));
        }
    }
    //?} else {
    /*private void tacz$keyInput(long window, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (window == this.minecraft.getWindow().getWindow()) {
            MinecraftForge.EVENT_BUS.post(new InputEvent.Key(key, scanCode, action, modifiers));
        }
    }
    *///?}
}
