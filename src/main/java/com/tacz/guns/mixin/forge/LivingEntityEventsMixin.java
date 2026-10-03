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
    //? if >=1.21.2 {
    private void tacz$livingHurt(ServerLevel level, DamageSource source, float amount, Operation<Void> original) {
    //?} else {
    /*private void tacz$livingHurt(DamageSource source, float amount, Operation<Void> original) {
    *///?}
        LivingHurtEvent event = new LivingHurtEvent((LivingEntity) (Object) this, source, amount);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return;
        }
        original.call(/*? if >=1.21.2 {*/level, /*?}*/source, event.getAmount());
    }

    //? if >=26.2 {
    /*// 26.2 passes the damage along, the five argument overload delegates here
    @WrapMethod(method = "knockback(DDDLnet/minecraft/world/damagesource/DamageSource;FZ)V")
    private void tacz$knockback(double strength, double x, double z, net.minecraft.world.damagesource.DamageSource source, float damage,
                                boolean comesFromEffect, Operation<Void> original) {
        LivingKnockBackEvent event = new LivingKnockBackEvent((LivingEntity) (Object) this, (float) strength, x, z);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return;
        }
        original.call((double) event.getStrength(), event.getRatioX(), event.getRatioZ(), source, damage, comesFromEffect);
    }
    *///?} else {
    @WrapMethod(method = "knockback")
    private void tacz$knockback(double strength, double x, double z, Operation<Void> original) {
        LivingKnockBackEvent event = new LivingKnockBackEvent((LivingEntity) (Object) this, (float) strength, x, z);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return;
        }
        original.call((double) event.getStrength(), event.getRatioX(), event.getRatioZ());
    }
    //?}
}
