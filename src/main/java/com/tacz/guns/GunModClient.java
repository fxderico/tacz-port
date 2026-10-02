package com.tacz.guns;

import com.tacz.guns.api.client.other.ThirdPersonManager;
import com.tacz.guns.block.entity.GunSmithTableBlockEntity;
import com.tacz.guns.block.entity.StatueBlockEntity;
import com.tacz.guns.block.entity.TargetBlockEntity;
import com.tacz.guns.client.gui.GunSmithTableScreen;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import com.tacz.guns.client.gui.overlay.HeatBarOverlay;
import com.tacz.guns.client.gui.overlay.InteractKeyTextOverlay;
import com.tacz.guns.client.gui.overlay.KillAmountOverlay;
import com.tacz.guns.client.input.*;
import com.tacz.guns.client.particle.BulletHoleParticle;
import com.tacz.guns.client.renderer.block.GunSmithTableRenderer;
import com.tacz.guns.client.renderer.block.StatueRenderer;
import com.tacz.guns.client.renderer.block.TargetRenderer;
import com.tacz.guns.client.renderer.entity.EntityBulletRenderer;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.resource.InternalAssetLoader;
import com.tacz.guns.client.sound.SoundPlayManager;
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
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.platform.EventSubscribersClient;
import com.tacz.guns.platform.FabricClientEventBridge;
import com.tacz.guns.platform.forge.client.gui.overlay.IGuiOverlay;
import com.tacz.guns.platform.forge.network.simple.SimpleChannelClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;
import com.tacz.guns.util.TaczRegistries;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.world.item.Item;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.platform.client.TaczItemSpecialRenderer;
import com.tacz.guns.platform.forge.client.extensions.common.IClientItemExtensions;
import com.tacz.guns.platform.forge.registries.RegistryObject;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.util.concurrent.atomic.AtomicInteger;

@Environment(EnvType.CLIENT)
public class GunModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // item renderers: every TACZ item model is "minecraft:special" -> "tacz:item", which dispatches to the old BEWLRs
        SpecialModelRenderers.ID_MAPPER.put(id("item"), TaczItemSpecialRenderer.Unbaked.MAP_CODEC);
        com.tacz.guns.client.resource.AmmoBoxModelProperties.register();
        for (RegistryObject<Item> item : ModItems.ITEMS.getEntries()) {
            IClientItemExtensions.collect(item.get());
        }

        // 注册键位
        KeyBindingHelper.registerKeyBinding(InspectKey.INSPECT_KEY);
        KeyBindingHelper.registerKeyBinding(ReloadKey.RELOAD_KEY);
        KeyBindingHelper.registerKeyBinding(ShootKey.SHOOT_KEY);
        KeyBindingHelper.registerKeyBinding(InteractKey.INTERACT_KEY);
        KeyBindingHelper.registerKeyBinding(FireSelectKey.FIRE_SELECT_KEY);
        KeyBindingHelper.registerKeyBinding(AimKey.AIM_KEY);
        KeyBindingHelper.registerKeyBinding(CrawlKey.CRAWL_KEY);
        KeyBindingHelper.registerKeyBinding(RefitKey.REFIT_KEY);
        KeyBindingHelper.registerKeyBinding(ZoomKey.ZOOM_KEY);
        KeyBindingHelper.registerKeyBinding(MeleeKey.MELEE_KEY);

        // 注册文本提示
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof GunTooltip tooltip) {
                return new ClientGunTooltip(tooltip);
            }
            if (data instanceof AmmoBoxTooltip tooltip) {
                return new ClientAmmoBoxTooltip(tooltip);
            }
            if (data instanceof AttachmentItemTooltip tooltip) {
                return new ClientAttachmentItemTooltip(tooltip);
            }
            if (data instanceof BlockItemTooltip tooltip) {
                return new ClientBlockItemTooltip(tooltip);
            }
            return null;
        });

        // 注册 HUD
        HudElementRegistry.addLast(id("tac_gun_hud_overlay"), hud(new GunHudOverlay()));
        HudElementRegistry.addLast(id("tac_heat_bar"), hud(new HeatBarOverlay()));
        HudElementRegistry.addLast(id("tac_kill_amount_overlay"), hud(new KillAmountOverlay()));
        HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR, id("tac_interact_key_overlay"), hud(new InteractKeyTextOverlay()));

        // 注册自己的的硬编码第三人称动画
        ThirdPersonManager.registerDefault();

        MenuScreens.register(GunSmithTableMenu.TYPE, GunSmithTableScreen::new);

        EntityRendererRegistry.register(EntityKineticBullet.TYPE, EntityBulletRenderer::new);
        BlockEntityRenderers.register(GunSmithTableBlockEntity.TYPE, GunSmithTableRenderer::new);
        BlockEntityRenderers.register(TargetBlockEntity.TYPE, TargetRenderer::new);
        BlockEntityRenderers.register(StatueBlockEntity.TYPE, StatueRenderer::new);

        ParticleFactoryRegistry.getInstance().register(ModParticles.BULLET_HOLE.get(), new BulletHoleParticle.Provider());

        // gun pack display/model/animation loaders, then the old TextureStitchEvent work once the atlas is rebuilt
        ResourceLoader clientResources = ResourceLoader.get(PackType.CLIENT_RESOURCES);
        // one listener running TACZ's loaders in order, displays and indexes depend on what was loaded before them
        java.util.List<net.minecraft.server.packs.resources.PreparableReloadListener> clientListeners = new java.util.ArrayList<>();
        ClientAssetsManager.INSTANCE.reloadAndRegister(clientListeners::add);
        clientResources.registerReloader(id("client_assets"), new com.tacz.guns.platform.SequentialReloadListener("TaczClientAssets", () -> clientListeners));
        clientResources.registerReloader(id("internal_assets"), (ResourceManagerReloadListener) (ResourceManager manager) -> {
            // InternalAssetLoader 需要加载一些默认的动画、模型，需要先于枪包加载。
            InternalAssetLoader.onResourceReload();
            SoundPlayManager.clearSoundResourceCache();
        });
        // the internal default models/animations have to be there before the gun packs reference them
        clientResources.addReloaderOrdering(id("internal_assets"), id("client_assets"));

        //? if >=26.2 {
        /*SpecialGuiElementRegistry.register(ctx -> new com.tacz.guns.platform.client.ItemPreviewRenderer());
        *///?} else {
        SpecialGuiElementRegistry.register(ctx -> new com.tacz.guns.platform.client.ItemPreviewRenderer(ctx.vertexConsumers()));
        //?}

        SimpleChannelClient.registerReceivers(NetworkHandler.CHANNEL);
        TaczRegistries.setClientRegistries(() -> Minecraft.getInstance().level != null ? Minecraft.getInstance().level.registryAccess() : null);

        EventSubscribersClient.registerClient();
        FabricClientEventBridge.register();
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(GunMod.MOD_ID, path);
    }

    private static net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement hud(IGuiOverlay overlay) {
        return (graphics, deltaTracker) -> {
            Minecraft mc = Minecraft.getInstance();
            overlay.render(mc.gui, graphics, deltaTracker.getGameTimeDeltaPartialTick(false),
                    graphics.guiWidth(), graphics.guiHeight());
        };
    }
}
