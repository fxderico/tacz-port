package com.tacz.guns.platform;

import com.tacz.guns.platform.forge.client.event.ClientPlayerNetworkEvent;
import com.tacz.guns.platform.forge.common.MinecraftForge;
import com.tacz.guns.platform.forge.event.TickEvent;
import com.tacz.guns.platform.forge.event.entity.player.ItemTooltipEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;

/**
 * Client side half of {@link FabricEventBridge}. Input, camera, hand and HUD events come from mixin.client.forge.
 */
@Environment(EnvType.CLIENT)
public final class FabricClientEventBridge {
    private FabricClientEventBridge() {
    }

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> MinecraftForge.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.START)));
        ClientTickEvents.END_CLIENT_TICK.register(client -> MinecraftForge.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.END)));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                MinecraftForge.EVENT_BUS.post(new ClientPlayerNetworkEvent.LoggingIn(client.gameMode, client.player, handler.getConnection())));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                MinecraftForge.EVENT_BUS.post(new ClientPlayerNetworkEvent.LoggingOut(client.gameMode, client.player, handler.getConnection())));

        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) ->
                MinecraftForge.EVENT_BUS.post(new ItemTooltipEvent(stack, Minecraft.getInstance().player, lines, flag)));
    }
}
