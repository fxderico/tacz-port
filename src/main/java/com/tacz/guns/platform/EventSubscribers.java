package com.tacz.guns.platform;

import com.tacz.guns.platform.forge.common.MinecraftForge;

/**
 * Generated from the old @Mod.EventBusSubscriber annotations.
 */
public final class EventSubscribers {
    public static void registerCommon() {
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.event.EntityDamageEvent.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.event.HitboxHelperEvent.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.event.KnockbackChange.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.event.PlayerRespawnEvent.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.event.PreventGunClick.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.event.ServerTickEvent.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.event.SyncBaseTimestamp.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.event.SyncedEntityDataEvent.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.event.TravelToDimensionEvent.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.event.ammo.BellRing.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.event.ammo.DestroyGlassBlock.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.resource.CommonAssetsManager.class);
    }
}
