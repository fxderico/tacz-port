package com.tacz.guns.mixin.client.forge;

import com.tacz.guns.platform.forge.client.event.InputEvent;
import com.tacz.guns.platform.forge.common.MinecraftForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
//? if >=1.21.9
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerEventMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    //? if >=1.21.9 {
    @Inject(method = "onButton", at = @At("TAIL"))
    private void tacz$mouseButton(long window, MouseButtonInfo info, int action, CallbackInfo ci) {
        if (window == this.minecraft.getWindow().handle()) {
            MinecraftForge.EVENT_BUS.post(new InputEvent.MouseButton.Post(info.button(), action, info.modifiers()));
        }
    }
    //?} else {
    /*@Inject(method = "onPress", at = @At("TAIL"))
    private void tacz$mouseButton(long window, int button, int action, int modifiers, CallbackInfo ci) {
        if (window == this.minecraft.getWindow().getWindow()) {
            MinecraftForge.EVENT_BUS.post(new InputEvent.MouseButton.Post(button, action, modifiers));
        }
    }
    *///?}
}
