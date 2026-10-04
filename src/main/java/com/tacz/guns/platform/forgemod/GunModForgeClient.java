package com.tacz.guns.platform.forgemod;

//? if forge {
/*import com.tacz.guns.GunModClient;
import com.tacz.guns.block.entity.GunSmithTableBlockEntity;
import com.tacz.guns.block.entity.StatueBlockEntity;
import com.tacz.guns.block.entity.TargetBlockEntity;
import com.tacz.guns.client.gui.GunSmithTableScreen;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import com.tacz.guns.client.gui.overlay.HeatBarOverlay;
import com.tacz.guns.client.gui.overlay.InteractKeyTextOverlay;
import com.tacz.guns.client.gui.overlay.KillAmountOverlay;
import com.tacz.guns.client.particle.BulletHoleParticle;
import com.tacz.guns.client.renderer.block.GunSmithTableRenderer;
import com.tacz.guns.client.renderer.block.StatueRenderer;
import com.tacz.guns.client.renderer.block.TargetRenderer;
import com.tacz.guns.client.renderer.entity.EntityBulletRenderer;
import com.tacz.guns.client.resource.AmmoBoxModelProperties;
import com.tacz.guns.client.tooltip.ClientAmmoBoxTooltip;
import com.tacz.guns.client.tooltip.ClientAttachmentItemTooltip;
import com.tacz.guns.client.tooltip.ClientBlockItemTooltip;
import com.tacz.guns.client.tooltip.ClientGunTooltip;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.init.ModParticles;
import com.tacz.guns.inventory.GunSmithTableMenu;
import com.tacz.guns.inventory.tooltip.AmmoBoxTooltip;
import com.tacz.guns.inventory.tooltip.AttachmentItemTooltip;
import com.tacz.guns.inventory.tooltip.BlockItemTooltip;
import com.tacz.guns.inventory.tooltip.GunTooltip;
import com.tacz.guns.platform.forge.client.event.ClientPlayerNetworkEvent;
import com.tacz.guns.platform.forge.client.gui.overlay.IGuiOverlay;
import com.tacz.guns.platform.forge.common.MinecraftForge;
import com.tacz.guns.platform.forge.event.TickEvent;
import com.tacz.guns.platform.forge.event.entity.player.ItemTooltipEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.tacz.guns.GunModClient.id;

/^*
 * Forge side of GunModClient#onInitializeClient: the same registrations through Forge's events.
 ^/
public final class GunModForgeClient {
    private GunModForgeClient() {
    }

    //? if >=1.21.6 {
    public static void init(net.minecraftforge.eventbus.api.bus.BusGroup modBus) {
    //?} else {
    /^public static void init(net.minecraftforge.eventbus.api.IEventBus modBus) {
    ^///?}
        GunModClient.commonClientInit();
        // the vanilla registries Fabric fills directly: item model renderers, ammo box properties and tint
        //? if >=1.21.4
        net.minecraft.client.renderer.special.SpecialModelRenderers.ID_MAPPER.put(id("item"), com.tacz.guns.platform.client.TaczItemSpecialRenderer.Unbaked.MAP_CODEC);
        AmmoBoxModelProperties.register();

        //? if >=1.21.6 {
        FMLClientSetupEvent.getBus(modBus).addListener(GunModForgeClient::clientSetup);
        RegisterKeyMappingsEvent.getBus(modBus).addListener(GunModForgeClient::keys);
        RegisterClientTooltipComponentFactoriesEvent.getBus(modBus).addListener(GunModForgeClient::tooltips);
        EntityRenderersEvent.RegisterRenderers.getBus(modBus).addListener(GunModForgeClient::renderers);
        RegisterParticleProvidersEvent.getBus(modBus).addListener(GunModForgeClient::particles);
        RegisterClientReloadListenersEvent.getBus(modBus).addListener(GunModForgeClient::reloadListeners);
        net.minecraftforge.client.event.AddGuiOverlayLayersEvent.getBus(modBus).addListener(event -> {
            var layers = event.getLayeredDraw();
            layers.add(id("tac_gun_hud_overlay"), layer(new GunHudOverlay()));
            layers.add(id("tac_heat_bar"), layer(new HeatBarOverlay()));
            layers.add(id("tac_kill_amount_overlay"), layer(new KillAmountOverlay()));
            layers.add(id("tac_interact_key_overlay"), layer(new InteractKeyTextOverlay()));
        });
        //? if <26.2 {
        net.minecraftforge.client.event.RegisterPictureInPictureRendererEvent.BUS.addListener(event ->
                event.register(new com.tacz.guns.platform.client.ItemPreviewRenderer(event.getBufferSource())));
        //?} else {
        /^net.minecraftforge.client.event.RegisterPictureInPictureRendererEvent.BUS.addListener(event ->
                event.register(new com.tacz.guns.platform.client.ItemPreviewRenderer()));
        ^///?}

        net.minecraftforge.event.TickEvent.ClientTickEvent.Pre.BUS.addListener(event -> MinecraftForge.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.START)));
        net.minecraftforge.event.TickEvent.ClientTickEvent.Post.BUS.addListener(event -> MinecraftForge.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.END)));
        net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingIn.BUS.addListener(event -> MinecraftForge.EVENT_BUS.post(
                new ClientPlayerNetworkEvent.LoggingIn(event.getMultiPlayerGameMode(), event.getPlayer(), event.getConnection())));
        net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut.BUS.addListener(event -> MinecraftForge.EVENT_BUS.post(
                new ClientPlayerNetworkEvent.LoggingOut(event.getMultiPlayerGameMode(), event.getPlayer(), event.getConnection())));
        net.minecraftforge.event.entity.player.ItemTooltipEvent.BUS.addListener(event -> MinecraftForge.EVENT_BUS.post(
                new ItemTooltipEvent(event.getItemStack(), event.getEntity(), event.getToolTip(), event.getFlags())));
        //?} else {
        /^modBus.addListener(GunModForgeClient::clientSetup);
        modBus.addListener(GunModForgeClient::keys);
        modBus.addListener(GunModForgeClient::tooltips);
        modBus.addListener(GunModForgeClient::renderers);
        modBus.addListener(GunModForgeClient::particles);
        modBus.addListener(GunModForgeClient::reloadListeners);
        //? if <1.21.4 {
        // the ammo box variant from a model predicate (clamped to 0..1, the overrides use index / 8) and its dye from an item color
        modBus.addListener((net.minecraftforge.client.event.RegisterColorHandlersEvent.Item event) -> event.register(
                (stack, tintIndex) -> net.minecraft.util.ARGB.opaque(com.tacz.guns.item.AmmoBoxItem.getColor(stack, tintIndex)), com.tacz.guns.init.ModItems.AMMO_BOX.get()));
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> net.minecraft.client.renderer.item.ItemProperties.register(
                com.tacz.guns.init.ModItems.AMMO_BOX.get(), AmmoBoxModelProperties.STATUE_ID,
                (stack, level, entity, seed) -> com.tacz.guns.item.AmmoBoxItem.getStatue(stack, level, entity, seed) / 8.0F)));
        //?}
        modBus.addListener((net.minecraftforge.client.event.AddGuiOverlayLayersEvent event) -> {
            var layers = event.getLayeredDraw();
            layers.add(id("tac_gun_hud_overlay"), layer(new GunHudOverlay()));
            layers.add(id("tac_heat_bar"), layer(new HeatBarOverlay()));
            layers.add(id("tac_kill_amount_overlay"), layer(new KillAmountOverlay()));
            layers.add(id("tac_interact_key_overlay"), layer(new InteractKeyTextOverlay()));
        });

        net.minecraftforge.eventbus.api.IEventBus bus = net.minecraftforge.common.MinecraftForge.EVENT_BUS;
        bus.addListener((net.minecraftforge.event.TickEvent.ClientTickEvent event) -> MinecraftForge.EVENT_BUS.post(new TickEvent.ClientTickEvent(
                event.phase == net.minecraftforge.event.TickEvent.Phase.START ? TickEvent.Phase.START : TickEvent.Phase.END)));
        bus.addListener((net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingIn event) -> MinecraftForge.EVENT_BUS.post(
                new ClientPlayerNetworkEvent.LoggingIn(event.getMultiPlayerGameMode(), event.getPlayer(), event.getConnection())));
        bus.addListener((net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) -> MinecraftForge.EVENT_BUS.post(
                new ClientPlayerNetworkEvent.LoggingOut(event.getMultiPlayerGameMode(), event.getPlayer(), event.getConnection())));
        bus.addListener((net.minecraftforge.event.entity.player.ItemTooltipEvent event) -> MinecraftForge.EVENT_BUS.post(
                new ItemTooltipEvent(event.getItemStack(), event.getEntity(), event.getToolTip(), event.getFlags())));
        ^///?}
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        GunModClient.collectItemExtensions();
        event.enqueueWork(() -> {
            MenuScreens.register(GunSmithTableMenu.TYPE, GunSmithTableScreen::new);
            //? if <26.2 {
            // scopes mask their eyepiece with the stencil buffer (26.2+ uses shader masks instead)
            Minecraft.getInstance().getMainRenderTarget().enableStencil();
            //?}
        });
    }

    private static void keys(RegisterKeyMappingsEvent event) {
        for (KeyMapping key : GunModClient.keyMappings()) {
            event.register(key);
        }
    }

    private static void tooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(GunTooltip.class, ClientGunTooltip::new);
        event.register(AmmoBoxTooltip.class, ClientAmmoBoxTooltip::new);
        event.register(AttachmentItemTooltip.class, ClientAttachmentItemTooltip::new);
        event.register(BlockItemTooltip.class, ClientBlockItemTooltip::new);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityKineticBullet.TYPE, EntityBulletRenderer::new);
        event.registerBlockEntityRenderer(GunSmithTableBlockEntity.TYPE, GunSmithTableRenderer::new);
        event.registerBlockEntityRenderer(TargetBlockEntity.TYPE, TargetRenderer::new);
        event.registerBlockEntityRenderer(StatueBlockEntity.TYPE, StatueRenderer::new);
    }

    private static void particles(RegisterParticleProvidersEvent event) {
        event.registerSpecial(ModParticles.BULLET_HOLE.get(), new BulletHoleParticle.Provider());
    }

    private static void reloadListeners(RegisterClientReloadListenersEvent event) {
        // the internal default models/animations run first inside the same listener, the gun packs reference them
        event.registerReloadListener (GunModClient.clientAssetsListener(true));
    }

    //? if >=1.21.6 {
    private static net.minecraftforge.client.gui.overlay.ForgeLayer layer(IGuiOverlay overlay) {
        return (graphics, deltaTracker) -> {
            Minecraft mc = Minecraft.getInstance();
            net.minecraft.client.Options options = mc.options;
            if (!options.hideGui) {
                overlay.render(mc.gui, graphics, deltaTracker.getGameTimeDeltaPartialTick(false), graphics.guiWidth(), graphics.guiHeight());
            }
        };
    }
    //?} else {
    /^private static net.minecraft.client.gui.LayeredDraw.Layer layer(IGuiOverlay overlay) {
        return (graphics, deltaTracker) -> {
            Minecraft mc = Minecraft.getInstance();
            net.minecraft.client.Options options = mc.options;
            if (!options.hideGui) {
                overlay.render(mc.gui, graphics, deltaTracker.getGameTimeDeltaPartialTick(false), graphics.guiWidth(), graphics.guiHeight());
            }
        };
    }
    ^///?}
}
*///?}
