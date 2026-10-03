package com.tacz.guns.platform.forgemod;

//? if forge {
/*import com.tacz.guns.GunMod;
import com.tacz.guns.command.RootCommand;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;

/^*
 * Forge game bus events turned into the Forge style events TACZ listens to (its own bus, see MinecraftForge),
 * like FabricEventBridge does with Fabric callbacks. Events with no Forge counterpart here are fired by the same
 * mixins as on Fabric.
 ^/
public final class ForgeEventBridge {
    private ForgeEventBridge() {
    }

    public static void register() {
        //? if >=1.21.6 {
        ServerStartingEvent.BUS.addListener(event -> ServerLifecycleHooks.setCurrentServer(event.getServer()));
        net.minecraftforge.event.server.ServerStartedEvent.BUS.addListener(event -> MinecraftForge.EVENT_BUS.post(new ServerStartedEvent(event.getServer())));
        net.minecraftforge.event.server.ServerStoppedEvent.BUS.addListener(event -> {
            MinecraftForge.EVENT_BUS.post(new ServerStoppedEvent(event.getServer()));
            ServerLifecycleHooks.setCurrentServer(null);
        });

        net.minecraftforge.event.TickEvent.ServerTickEvent.Pre.BUS.addListener(event -> MinecraftForge.EVENT_BUS.post(new TickEvent.ServerTickEvent(TickEvent.Phase.START, event.server())));
        net.minecraftforge.event.TickEvent.ServerTickEvent.Post.BUS.addListener(event -> MinecraftForge.EVENT_BUS.post(new TickEvent.ServerTickEvent(TickEvent.Phase.END, event.server())));

        net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent.BUS.addListener(event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                GunMod.onPlayerJoin(player);
                MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedInEvent(player));
            }
        });
        net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent.BUS.addListener(event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedOutEvent(player));
            }
        });
        net.minecraftforge.event.entity.player.PlayerEvent.Clone.BUS.addListener(event -> {
            if (event.getEntity() instanceof ServerPlayer player && event.getOriginal() instanceof ServerPlayer original) {
                MinecraftForge.EVENT_BUS.post(new PlayerEvent.Clone(player, original, event.isWasDeath()));
            }
        });
        net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent.BUS.addListener(event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerRespawnEvent(player, event.isEndConquered()));
            }
        });
        net.minecraftforge.event.entity.player.PlayerEvent.StartTracking.BUS.addListener(event ->
                MinecraftForge.EVENT_BUS.post(new PlayerEvent.StartTracking(event.getEntity(), event.getTarget())));
        net.minecraftforge.event.entity.EntityJoinLevelEvent.BUS.addListener((java.util.function.Consumer<net.minecraftforge.event.entity.EntityJoinLevelEvent>) event -> {
            if (!event.getLevel().isClientSide()) {
                MinecraftForge.EVENT_BUS.post(new EntityJoinLevelEvent(event.getEntity(), event.getLevel()));
            }
        });

        net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock>) event ->
                event.getAction() == net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action.START && event.getFace() != null
                        && MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.LeftClickBlock(event.getEntity(), event.getPos(), event.getFace())));

        net.minecraftforge.event.TagsUpdatedEvent.BUS.addListener(event -> MinecraftForge.EVENT_BUS.post(new TagsUpdatedEvent(
                event.getUpdateCause() == net.minecraftforge.event.TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED
                        ? TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED : TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD)));
        net.minecraftforge.event.OnDatapackSyncEvent.BUS.addListener(event -> event.getPlayers().forEach(player -> {
            //? if >=1.21.2 {
            // no recipe sync since 1.21.2, the smith table screen needs its recipes on the client
            com.tacz.guns.network.NetworkHandler.sendToClientPlayer(com.tacz.guns.network.message.ServerMessageSyncRecipes.of(event.getPlayerList().getServer()), player);
            //?}
            MinecraftForge.EVENT_BUS.post(new OnDatapackSyncEvent(event.getPlayerList(), player));
        }));

        RegisterCommandsEvent.BUS.addListener(event -> RootCommand.register(event.getDispatcher()));
        AddReloadListenerEvent.BUS.addListener(event -> {
            LootInjectionManager.reloadRegistries = event.getRegistries();
            // gun pack data (indexes, gun data, scripts...) reloads with the server data packs
            event.addListener(CommonAssetsManager.RELOAD_LISTENER);
        });
        //?} else {
        /^net.minecraftforge.eventbus.api.IEventBus bus = net.minecraftforge.common.MinecraftForge.EVENT_BUS;
        bus.addListener((ServerStartingEvent event) -> ServerLifecycleHooks.setCurrentServer(event.getServer()));
        bus.addListener((net.minecraftforge.event.server.ServerStartedEvent event) -> MinecraftForge.EVENT_BUS.post(new ServerStartedEvent(event.getServer())));
        bus.addListener((net.minecraftforge.event.server.ServerStoppedEvent event) -> {
            MinecraftForge.EVENT_BUS.post(new ServerStoppedEvent(event.getServer()));
            ServerLifecycleHooks.setCurrentServer(null);
        });

        bus.addListener((net.minecraftforge.event.TickEvent.ServerTickEvent event) -> MinecraftForge.EVENT_BUS.post(new TickEvent.ServerTickEvent(
                event.phase == net.minecraftforge.event.TickEvent.Phase.START ? TickEvent.Phase.START : TickEvent.Phase.END, event.getServer())));

        bus.addListener((net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                GunMod.onPlayerJoin(player);
                MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedInEvent(player));
            }
        });
        bus.addListener((net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedOutEvent(player));
            }
        });
        bus.addListener((net.minecraftforge.event.entity.player.PlayerEvent.Clone event) -> {
            if (event.getEntity() instanceof ServerPlayer player && event.getOriginal() instanceof ServerPlayer original) {
                MinecraftForge.EVENT_BUS.post(new PlayerEvent.Clone(player, original, event.isWasDeath()));
            }
        });
        bus.addListener((net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerRespawnEvent(player, event.isEndConquered()));
            }
        });
        bus.addListener((net.minecraftforge.event.entity.player.PlayerEvent.StartTracking event) ->
                MinecraftForge.EVENT_BUS.post(new PlayerEvent.StartTracking(event.getEntity(), event.getTarget())));
        bus.addListener((net.minecraftforge.event.entity.EntityJoinLevelEvent event) -> {
            if (!event.getLevel().isClientSide()) {
                MinecraftForge.EVENT_BUS.post(new EntityJoinLevelEvent(event.getEntity(), event.getLevel()));
            }
        });

        bus.addListener((net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event) -> {
            if (event.getAction() == net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action.START && event.getFace() != null
                    && MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.LeftClickBlock(event.getEntity(), event.getPos(), event.getFace()))) {
                event.setCanceled(true);
            }
        });

        bus.addListener((net.minecraftforge.event.TagsUpdatedEvent event) -> MinecraftForge.EVENT_BUS.post(new TagsUpdatedEvent(
                event.getUpdateCause() == net.minecraftforge.event.TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED
                        ? TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED : TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD)));
        bus.addListener((net.minecraftforge.event.OnDatapackSyncEvent event) -> event.getPlayers().forEach(player -> {
            //? if >=1.21.2 {
            com.tacz.guns.network.NetworkHandler.sendToClientPlayer(com.tacz.guns.network.message.ServerMessageSyncRecipes.of(event.getPlayerList().getServer()), player);
            //?}
            MinecraftForge.EVENT_BUS.post(new OnDatapackSyncEvent(event.getPlayerList(), player));
        }));

        bus.addListener((RegisterCommandsEvent event) -> RootCommand.register(event.getDispatcher()));
        bus.addListener((AddReloadListenerEvent event) -> {
            LootInjectionManager.reloadRegistries = event.getRegistryAccess();
            event.addListener(CommonAssetsManager.RELOAD_LISTENER);
        });
        ^///?}
    }
}
*///?}
