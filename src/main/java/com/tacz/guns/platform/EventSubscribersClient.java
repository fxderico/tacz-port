package com.tacz.guns.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.tacz.guns.platform.forge.common.MinecraftForge;

/**
 * Generated from the old @Mod.EventBusSubscriber annotations.
 */
@Environment(EnvType.CLIENT)
public final class EventSubscribersClient {
    public static void registerClient() {
        MinecraftForge.EVENT_BUS.register(com.github.mcmodderanchor.simplebedrockmodel.v1.client.handler.FirstPersonRenderHandler.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.animation.screen.RefitTransform.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.event.CameraSetupEvent.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.event.ClientHitMark.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.event.ClientPreventGunClick.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.event.CommonNetworkCacheEvent.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.event.FirstPersonRenderEvent.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.event.FirstPersonRenderGunEvent.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.event.InventoryEvent.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.event.PlayerEnterWorld.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.event.PlayerHurtByGunEvent.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.event.PreventsHotbarEvent.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.event.RefreshClonePlayerDataEvent.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.event.RenderCrosshairEvent.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.event.TickAnimationEvent.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.event.TooltipEvent.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.gui.GunRefitScreen.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.input.AimKey.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.input.CrawlKey.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.input.FireSelectKey.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.input.InspectKey.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.input.InteractKey.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.input.MeleeKey.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.input.RefitKey.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.input.ReloadKey.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.input.ShootKey.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.input.ZoomKey.class);
        MinecraftForge.EVENT_BUS.register(com.tacz.guns.client.sound.SoundPlayManager.class);
    }
}
