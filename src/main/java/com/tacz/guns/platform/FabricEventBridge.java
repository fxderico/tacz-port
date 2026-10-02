package com.tacz.guns.platform;

import com.tacz.guns.platform.forge.common.MinecraftForge;
import com.tacz.guns.platform.forge.event.OnDatapackSyncEvent;
import com.tacz.guns.platform.forge.event.TagsUpdatedEvent;
import com.tacz.guns.platform.forge.event.TickEvent;
import com.tacz.guns.platform.forge.event.entity.EntityJoinLevelEvent;
import com.tacz.guns.platform.forge.event.entity.player.PlayerEvent;
import com.tacz.guns.platform.forge.event.entity.player.PlayerInteractEvent;
import com.tacz.guns.platform.forge.event.server.ServerStoppedEvent;
import com.tacz.guns.platform.forge.server.ServerLifecycleHooks;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.world.InteractionResult;

/**
 * Turns Fabric callbacks into the Forge style events TACZ listens to.
 * Events with no Fabric callback are fired from mixins instead (see mixin.common.forge).
 */
public final class FabricEventBridge {
    private FabricEventBridge() {
    }

    public static void registerCommon() {
        ServerLifecycleEvents.SERVER_STARTING.register(ServerLifecycleHooks::setCurrentServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            MinecraftForge.EVENT_BUS.post(new ServerStoppedEvent(server));
            ServerLifecycleHooks.setCurrentServer(null);
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> MinecraftForge.EVENT_BUS.post(new TickEvent.ServerTickEvent(TickEvent.Phase.START, server)));
        ServerTickEvents.END_SERVER_TICK.register(server -> MinecraftForge.EVENT_BUS.post(new TickEvent.ServerTickEvent(TickEvent.Phase.END, server)));

        ServerPlayerEvents.JOIN.register(player -> MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedInEvent(player)));
        ServerPlayerEvents.LEAVE.register(player -> MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedOutEvent(player)));
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> MinecraftForge.EVENT_BUS.post(new PlayerEvent.Clone(newPlayer, oldPlayer, !alive)));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerRespawnEvent(newPlayer, alive)));

        EntityTrackingEvents.START_TRACKING.register((entity, player) -> MinecraftForge.EVENT_BUS.post(new PlayerEvent.StartTracking(player, entity)));
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> MinecraftForge.EVENT_BUS.post(new EntityJoinLevelEvent(entity, level)));

        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
            if (MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.LeftClickBlock(player, pos, direction))) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> MinecraftForge.EVENT_BUS.post(
                new TagsUpdatedEvent(client ? TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED : TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD)));
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) ->
                MinecraftForge.EVENT_BUS.post(new OnDatapackSyncEvent(player.level().getServer().getPlayerList(), player)));
    }
}
