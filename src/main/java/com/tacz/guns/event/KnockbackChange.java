package com.tacz.guns.event;

import com.tacz.guns.api.entity.KnockBackModifier;
import com.tacz.guns.platform.forge.event.entity.living.LivingKnockBackEvent;
import com.tacz.guns.platform.forge.eventbus.api.SubscribeEvent;
import com.tacz.guns.platform.forge.fml.common.Mod;

@Mod.EventBusSubscriber
public class KnockbackChange {
    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        KnockBackModifier modifier = KnockBackModifier.fromLivingEntity(event.getEntity());
        double strength = modifier.getKnockBackStrength();
        if (strength >= 0) {
            event.setStrength((float) strength);
        }
    }
}
