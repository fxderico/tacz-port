package com.tacz.guns.mixin.client.forge;

import com.tacz.guns.platform.forge.client.event.ComputeFovModifierEvent;
import com.tacz.guns.platform.forge.common.MinecraftForge;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerFovMixin {
    @Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
    private void tacz$fovModifier(/*? if >=1.21.2 {*/boolean firstPerson, float fovEffectScale, /*?}*/CallbackInfoReturnable<Float> cir) {
        ComputeFovModifierEvent event = new ComputeFovModifierEvent((AbstractClientPlayer) (Object) this, cir.getReturnValueF());
        MinecraftForge.EVENT_BUS.post(event);
        cir.setReturnValue(event.getNewFovModifier());
    }
}
