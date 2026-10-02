package com.tacz.guns.mixin.forge;

import com.tacz.guns.platform.forge.common.MinecraftForge;
import com.tacz.guns.platform.forge.event.entity.EntityTravelToDimensionEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityTravelMixin {
    @Inject(method = "teleport", at = @At("HEAD"), cancellable = true)
    private void tacz$travelToDimension(TeleportTransition transition, CallbackInfoReturnable<Entity> cir) {
        Entity self = (Entity) (Object) this;
        if (transition.newLevel().dimension() != self.level().dimension()
                && MinecraftForge.EVENT_BUS.post(new EntityTravelToDimensionEvent(self, transition.newLevel().dimension()))) {
            cir.setReturnValue(null);
        }
    }
}
