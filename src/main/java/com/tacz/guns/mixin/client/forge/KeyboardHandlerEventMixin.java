package com.tacz.guns.mixin.client.forge;

import com.tacz.guns.platform.forge.client.event.InputEvent;
import com.tacz.guns.platform.forge.common.MinecraftForge;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
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
    private void tacz$keyInput(long window, int action, KeyEvent keyEvent, CallbackInfo ci) {
        if (window == this.minecraft.getWindow().handle()) {
            MinecraftForge.EVENT_BUS.post(new InputEvent.Key(keyEvent.key(), keyEvent.scancode(), action, keyEvent.modifiers()));
        }
    }
}
