package com.tacz.guns.platform.neoforge;

//? if neoforge {
/*import com.tacz.guns.GunModClient;
import com.tacz.guns.block.entity.GunSmithTableBlockEntity;
import com.tacz.guns.block.entity.StatueBlockEntity;
import com.tacz.guns.block.entity.TargetBlockEntity;
import com.tacz.guns.client.gui.GunSmithTableScreen;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import com.tacz.guns.client.gui.overlay.HeatBarOverlay;
import com.tacz.guns.client.gui.overlay.InteractKeyTextOverlay;
import com.tacz.guns.client.gui.overlay.KillAmountOverlay;
import com.tacz.guns.client.input.TaczKeyCategory;
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
import com.tacz.guns.crafting.TaczRecipes;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.init.ModParticles;
import com.tacz.guns.inventory.GunSmithTableMenu;
import com.tacz.guns.inventory.tooltip.AmmoBoxTooltip;
import com.tacz.guns.inventory.tooltip.AttachmentItemTooltip;
import com.tacz.guns.inventory.tooltip.BlockItemTooltip;
import com.tacz.guns.inventory.tooltip.GunTooltip;
//? if >=1.21.6 {
import com.tacz.guns.platform.client.ItemPreviewRenderState;
import com.tacz.guns.platform.client.ItemPreviewRenderer;
//?}
//? if >=1.21.4
import com.tacz.guns.platform.client.TaczItemSpecialRenderer;
import com.tacz.guns.platform.forge.client.event.ClientPlayerNetworkEvent;
import com.tacz.guns.platform.forge.client.gui.overlay.IGuiOverlay;
import com.tacz.guns.platform.forge.common.MinecraftForge;
import com.tacz.guns.platform.forge.event.TickEvent;
import com.tacz.guns.platform.forge.event.entity.player.ItemTooltipEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
//? if >=1.21.4
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
//? if >=1.21.4
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
//? if >=1.21.6
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
//? if >=1.21.4
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
//? if >=1.21.4
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

import static com.tacz.guns.GunModClient.id;

/^*
 * NeoForge side of GunModClient#onInitializeClient: the same registrations through NeoForge's events.
 *^/
public final class GunModNeoForgeClient {
    private GunModNeoForgeClient() {
    }

    public static void init(IEventBus modBus) {
        GunModClient.commonClientInit();

        modBus.addListener(FMLClientSetupEvent.class, event -> GunModClient.collectItemExtensions());
        // scopes mask their eyepiece with the stencil buffer (26.2+ uses shader masks instead)
        //? if >=1.21.4 && <26.2 {
        modBus.addListener(net.neoforged.neoforge.client.event.ConfigureMainRenderTargetEvent.class,
                net.neoforged.neoforge.client.event.ConfigureMainRenderTargetEvent::enableStencil);
        //?} else if <1.21.2 {
        /^modBus.addListener(FMLClientSetupEvent.class, event -> event.enqueueWork(() -> Minecraft.getInstance().getMainRenderTarget().enableStencil()));
        ^///?}
        modBus.addListener(RegisterKeyMappingsEvent.class, event -> {
            //? if >=1.21.9
            event.registerCategory(TaczKeyCategory.CATEGORY);
            for (KeyMapping key : GunModClient.keyMappings()) {
                event.register(key);
            }
        });
        modBus.addListener(RegisterClientTooltipComponentFactoriesEvent.class, event -> {
            event.register(GunTooltip.class, ClientGunTooltip::new);
            event.register(AmmoBoxTooltip.class, ClientAmmoBoxTooltip::new);
            event.register(AttachmentItemTooltip.class, ClientAttachmentItemTooltip::new);
            event.register(BlockItemTooltip.class, ClientBlockItemTooltip::new);
        });
        modBus.addListener(RegisterGuiLayersEvent.class, event -> {
            event.registerAboveAll(id("tac_gun_hud_overlay"), layer(new GunHudOverlay()));
            event.registerAboveAll(id("tac_heat_bar"), layer(new HeatBarOverlay()));
            event.registerAboveAll(id("tac_kill_amount_overlay"), layer(new KillAmountOverlay()));
            event.registerAbove(VanillaGuiLayers.CROSSHAIR, id("tac_interact_key_overlay"), layer(new InteractKeyTextOverlay()));
        });
        modBus.addListener(RegisterMenuScreensEvent.class, event -> event.register(GunSmithTableMenu.TYPE, GunSmithTableScreen::new));
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> {
            event.registerEntityRenderer(EntityKineticBullet.TYPE, EntityBulletRenderer::new);
            event.registerBlockEntityRenderer(GunSmithTableBlockEntity.TYPE, GunSmithTableRenderer::new);
            event.registerBlockEntityRenderer(TargetBlockEntity.TYPE, TargetRenderer::new);
            event.registerBlockEntityRenderer(StatueBlockEntity.TYPE, StatueRenderer::new);
        });
        modBus.addListener(RegisterParticleProvidersEvent.class, event -> event.registerSpecial(ModParticles.BULLET_HOLE.get(), new BulletHoleParticle.Provider()));
        //? if >=1.21.4 {
        modBus.addListener(AddClientReloadListenersEvent.class, event -> {
            // the internal default models/animations have to be there before the gun packs reference them; one listener,
            // addDependency would hold client_assets back until internal_assets is done, which only happens after every
            // listener reached the reload barrier
            event.addListener(id("client_assets"), GunModClient.clientAssetsListener(true));
        });
        //?} else {
        /^modBus.addListener(net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent.class,
                event -> event.registerReloadListener (GunModClient.clientAssetsListener(true)));
        ^///?}
        //? if >=1.21.6
        modBus.addListener(RegisterPictureInPictureRenderersEvent.class, event -> event.register(ItemPreviewRenderState.class, ItemPreviewRenderer::new));
        //? if >=1.21.4 {
        // item renderers: every TACZ item model is "minecraft:special" -> "tacz:item", which dispatches to the old BEWLRs
        modBus.addListener(RegisterSpecialModelRendererEvent.class, event -> event.register(id("item"), TaczItemSpecialRenderer.Unbaked.MAP_CODEC));
        modBus.addListener(RegisterRangeSelectItemModelPropertyEvent.class, event -> event.register(AmmoBoxModelProperties.STATUE_ID, AmmoBoxModelProperties.Statue.MAP_CODEC));
        modBus.addListener(RegisterColorHandlersEvent.ItemTintSources.class, event -> event.register(AmmoBoxModelProperties.TINT_ID, AmmoBoxModelProperties.Tint.MAP_CODEC));
        //?} else {
        /^// before 1.21.4's item model definitions: "builtin/entity" models (src/main/legacy-item-models) drawn by the old BEWLRs,
        // the ammo box variant from a model predicate (clamped to 0..1, the overrides use index / 8) and its dye from an item color
        modBus.addListener(net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent.class, event -> {
            net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer renderer = new net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer(
                    Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()) {
                @Override
                public void renderByItem(net.minecraft.world.item.ItemStack stack, net.minecraft.world.item.ItemDisplayContext displayContext,
                                         com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource buffer, int light, int overlay) {
                    com.tacz.guns.platform.client.BlockEntityWithoutLevelRenderer custom =
                            com.tacz.guns.platform.forge.client.extensions.common.IClientItemExtensions.of(stack).getCustomRenderer();
                    if (custom != null) {
                        custom.renderByItem(stack, displayContext, poseStack, buffer, light, overlay);
                    }
                }
            };
            net.neoforged.neoforge.client.extensions.common.IClientItemExtensions extensions = new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
                @Override
                public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    return renderer;
                }
            };
            for (com.tacz.guns.platform.forge.registries.RegistryObject<net.minecraft.world.item.Item> item : com.tacz.guns.init.ModItems.ITEMS.getEntries()) {
                event.registerItem(extensions, item.get());
            }
        });
        modBus.addListener(FMLClientSetupEvent.class, event -> event.enqueueWork(() -> net.minecraft.client.renderer.item.ItemProperties.register(
                com.tacz.guns.init.ModItems.AMMO_BOX.get(), AmmoBoxModelProperties.STATUE_ID,
                (stack, level, entity, seed) -> com.tacz.guns.item.AmmoBoxItem.getStatue(stack, level, entity, seed) / 8.0F)));
        modBus.addListener(RegisterColorHandlersEvent.Item.class, event -> event.register(
                (stack, tintIndex) -> net.minecraft.util.ARGB.opaque(com.tacz.guns.item.AmmoBoxItem.getColor(stack, tintIndex)), com.tacz.guns.init.ModItems.AMMO_BOX.get()));
        ^///?}

        IEventBus bus = NeoForge.EVENT_BUS;
        bus.addListener(ClientTickEvent.Pre.class, event -> MinecraftForge.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.START)));
        bus.addListener(ClientTickEvent.Post.class, event -> MinecraftForge.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.END)));
        bus.addListener(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn.class, event -> MinecraftForge.EVENT_BUS.post(
                new ClientPlayerNetworkEvent.LoggingIn(event.getMultiPlayerGameMode(), event.getPlayer(), event.getConnection())));
        bus.addListener(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut.class, event -> MinecraftForge.EVENT_BUS.post(
                new ClientPlayerNetworkEvent.LoggingOut(event.getMultiPlayerGameMode(), event.getPlayer(), event.getConnection())));
        bus.addListener(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent.class, event -> MinecraftForge.EVENT_BUS.post(
                new ItemTooltipEvent(event.getItemStack(), event.getEntity(), event.getToolTip(), event.getFlags())));
        //? if >=1.21.4
        bus.addListener(RecipesReceivedEvent.class, event -> TaczRecipes.clientRecipes = event.getRecipeMap());
    }

    private static GuiLayer layer(IGuiOverlay overlay) {
        return (graphics, deltaTracker) -> {
            Minecraft mc = Minecraft.getInstance();
            overlay.render(mc.gui, graphics, deltaTracker.getGameTimeDeltaPartialTick(false), graphics.guiWidth(), graphics.guiHeight());
        };
    }
}
*///?}
