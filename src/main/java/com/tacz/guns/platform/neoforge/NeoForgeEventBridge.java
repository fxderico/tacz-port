package com.tacz.guns.platform.neoforge;

//? if neoforge {
/*import com.tacz.guns.GunMod;
import com.tacz.guns.command.RootCommand;
import com.tacz.guns.init.ModRecipe;
import com.tacz.guns.platform.forge.common.MinecraftForge;
import com.tacz.guns.platform.forge.event.OnDatapackSyncEvent;
import com.tacz.guns.platform.forge.event.TagsUpdatedEvent;
import com.tacz.guns.platform.forge.event.TickEvent;
import com.tacz.guns.platform.forge.event.entity.EntityJoinLevelEvent;
import com.tacz.guns.platform.forge.event.entity.player.PlayerEvent;
import com.tacz.guns.platform.forge.event.entity.player.PlayerInteractEvent;
import com.tacz.guns.platform.forge.event.server.ServerStartedEvent;
import com.tacz.guns.platform.forge.event.server.ServerStoppedEvent;
import com.tacz.guns.platform.forge.server.ServerLifecycleHooks;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.manager.LootInjectionManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/^*
 * NeoForge game bus events turned into the Forge style events TACZ listens to, like FabricEventBridge does with
 * Fabric callbacks. Events with no NeoForge counterpart here are fired by the same mixins as on Fabric.
 *^/
public final class NeoForgeEventBridge {
    private NeoForgeEventBridge() {
    }

    public static void register(IEventBus bus) {
        bus.addListener(ServerStartingEvent.class, event -> ServerLifecycleHooks.setCurrentServer(event.getServer()));
        bus.addListener(net.neoforged.neoforge.event.server.ServerStartedEvent.class, event -> MinecraftForge.EVENT_BUS.post(new ServerStartedEvent(event.getServer())));
        bus.addListener(net.neoforged.neoforge.event.server.ServerStoppedEvent.class, event -> {
            MinecraftForge.EVENT_BUS.post(new ServerStoppedEvent(event.getServer()));
            ServerLifecycleHooks.setCurrentServer(null);
        });

        bus.addListener(ServerTickEvent.Pre.class, event -> MinecraftForge.EVENT_BUS.post(new TickEvent.ServerTickEvent(TickEvent.Phase.START, event.getServer())));
        bus.addListener(ServerTickEvent.Post.class, event -> MinecraftForge.EVENT_BUS.post(new TickEvent.ServerTickEvent(TickEvent.Phase.END, event.getServer())));

        bus.addListener(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                GunMod.onPlayerJoin(player);
                MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedInEvent(player));
            }
        });
        bus.addListener(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedOutEvent(player));
            }
        });
        bus.addListener(net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player && event.getOriginal() instanceof ServerPlayer original) {
                MinecraftForge.EVENT_BUS.post(new PlayerEvent.Clone(player, original, event.isWasDeath()));
            }
        });
        bus.addListener(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerRespawnEvent(player, event.isEndConquered()));
            }
        });
        bus.addListener(net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking.class, event ->
                MinecraftForge.EVENT_BUS.post(new PlayerEvent.StartTracking(event.getEntity(), event.getTarget())));
        bus.addListener(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent.class, event -> {
            if (!event.getLevel().isClientSide()) {
                MinecraftForge.EVENT_BUS.post(new EntityJoinLevelEvent(event.getEntity(), event.getLevel()));
            }
        });

        bus.addListener(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.class, event -> {
            var action = event.getAction();
            if (action != net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action.START
                    && action != net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action.CLIENT_HOLD) {
                return;
            }
            if (event.getFace() != null && MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.LeftClickBlock(event.getEntity(), event.getPos(), event.getFace()))) {
                event.setCanceled(true);
            }
        });

        bus.addListener(net.neoforged.neoforge.event.TagsUpdatedEvent.class, event -> {
            //? if >=26.3 {
            /^boolean fromClientPacket = event instanceof net.neoforged.neoforge.event.TagsUpdatedEvent.ClientPacketReceived;
            ^///?} else {
            boolean fromClientPacket = event.getUpdateCause() == net.neoforged.neoforge.event.TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED;
            //?}
            MinecraftForge.EVENT_BUS.post(new TagsUpdatedEvent(fromClientPacket
                    ? TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED : TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD));
        });
        bus.addListener(net.neoforged.neoforge.event.OnDatapackSyncEvent.class, event -> {
            // 1.21.2+ only syncs the recipe types asked for, the smith table screen needs its recipes on the client
            event.sendRecipes(ModRecipe.GUN_SMITH_TABLE_CRAFTING.get());
            event.getRelevantPlayers().forEach(player -> MinecraftForge.EVENT_BUS.post(new OnDatapackSyncEvent(event.getPlayerList(), player)));
        });

        bus.addListener(RegisterCommandsEvent.class, event -> RootCommand.register(event.getDispatcher()));
        bus.addListener(AddServerReloadListenersEvent.class, event -> {
            //? if >=26.3 {
            /^LootInjectionManager.reloadRegistries = event.getServerResources().fullRegistries().lookup();
            ^///?} else {
            LootInjectionManager.reloadRegistries = event.getRegistryAccess();
            //?}
            // gun pack data (indexes, gun data, scripts...) reloads with the server data packs
            event.addListener(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "common_assets"), CommonAssetsManager.RELOAD_LISTENER);
        });
    }
}
*///?}
