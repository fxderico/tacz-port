package com.tacz.guns.mixin.client.forge;

import com.tacz.guns.platform.forge.client.event.InputEvent;
import com.tacz.guns.platform.forge.common.MinecraftForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
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

    @Inject(method = "onButton", at = @At("TAIL"))
    private void tacz$mouseButton(long window, MouseButtonInfo info, int action, CallbackInfo ci) {
        if (window == this.minecraft.getWindow().handle()) {
            MinecraftForge.EVENT_BUS.post(new InputEvent.MouseButton.Post(info.button(), action, info.modifiers()));
        }
    }
}
