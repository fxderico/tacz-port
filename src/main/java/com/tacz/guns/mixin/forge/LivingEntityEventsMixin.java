package com.tacz.guns.mixin.forge;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.tacz.guns.platform.forge.common.MinecraftForge;
import com.tacz.guns.platform.forge.event.entity.living.LivingHurtEvent;
import com.tacz.guns.platform.forge.event.entity.living.LivingKnockBackEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public abstract class LivingEntityEventsMixin {
    @WrapMethod(method = "actuallyHurt")
    private void tacz$livingHurt(ServerLevel level, DamageSource source, float amount, Operation<Void> original) {
        LivingHurtEvent event = new LivingHurtEvent((LivingEntity) (Object) this, source, amount);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return;
        }
        original.call(level, source, event.getAmount());
    }

    @WrapMethod(method = "knockback")
    private void tacz$knockback(double strength, double x, double z, Operation<Void> original) {
        LivingKnockBackEvent event = new LivingKnockBackEvent((LivingEntity) (Object) this, (float) strength, x, z);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return;
        }
        original.call((double) event.getStrength(), event.getRatioX(), event.getRatioZ());
    }
}
