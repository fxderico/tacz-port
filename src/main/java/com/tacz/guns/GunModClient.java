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
import com.tacz.guns.platform.forge.client.gui.overlay.IGuiOverlay;
import com.tacz.guns.platform.forge.network.simple.SimpleChannelClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
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

import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class GunModClient /*? if fabric {*/implements net.fabricmc.api.ClientModInitializer/*?}*/ {
    //? if fabric {
    @Override
    public void onInitializeClient() {
        // item renderers: every TACZ item model is "minecraft:special" -> "tacz:item", which dispatches to the old BEWLRs
        SpecialModelRenderers.ID_MAPPER.put(id("item"), TaczItemSpecialRenderer.Unbaked.MAP_CODEC);
        com.tacz.guns.client.resource.AmmoBoxModelProperties.register();
        collectItemExtensions();

        // 注册键位
        for (KeyMapping key : keyMappings()) {
            net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(key);
        }

        // 注册文本提示
        net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback.EVENT.register(GunModClient::tooltip);

        // 注册 HUD
        net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry.addLast(id("tac_gun_hud_overlay"), hud(new GunHudOverlay()));
        net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry.addLast(id("tac_heat_bar"), hud(new HeatBarOverlay()));
        net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry.addLast(id("tac_kill_amount_overlay"), hud(new KillAmountOverlay()));
        net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry.attachElementAfter(net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.CROSSHAIR, id("tac_interact_key_overlay"), hud(new InteractKeyTextOverlay()));

        MenuScreens.register(GunSmithTableMenu.TYPE, GunSmithTableScreen::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(EntityKineticBullet.TYPE, EntityBulletRenderer::new);
        BlockEntityRenderers.register(GunSmithTableBlockEntity.TYPE, GunSmithTableRenderer::new);
        BlockEntityRenderers.register(TargetBlockEntity.TYPE, TargetRenderer::new);
        BlockEntityRenderers.register(StatueBlockEntity.TYPE, StatueRenderer::new);

        net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.getInstance().register(ModParticles.BULLET_HOLE.get(), new BulletHoleParticle.Provider());

        // gun pack display/model/animation loaders, then the old TextureStitchEvent work once the atlas is rebuilt
        net.fabricmc.fabric.api.resource.v1.ResourceLoader clientResources = net.fabricmc.fabric.api.resource.v1.ResourceLoader.get(PackType.CLIENT_RESOURCES);
        clientResources.registerReloader(id("client_assets"), clientAssetsListener());
        clientResources.registerReloader(id("internal_assets"), internalAssetsListener());
        // the internal default models/animations have to be there before the gun packs reference them
        clientResources.addReloaderOrdering(id("internal_assets"), id("client_assets"));

        //? if >=26.2 {
        /*net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry.register(ctx -> new com.tacz.guns.platform.client.ItemPreviewRenderer());
        *///?} else {
        net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry.register(ctx -> new com.tacz.guns.platform.client.ItemPreviewRenderer(ctx.vertexConsumers()));
        //?}

        SimpleChannelClient.registerReceivers(NetworkHandler.CHANNEL);
        commonClientInit();
        com.tacz.guns.platform.FabricClientEventBridge.register();
    }
    //?}

    /**
     * Loader independent client setup.
     */
    public static void commonClientInit() {
        // 注册自己的的硬编码第三人称动画
        ThirdPersonManager.registerDefault();
        TaczRegistries.setClientRegistries(() -> Minecraft.getInstance().level != null ? Minecraft.getInstance().level.registryAccess() : null);
        EventSubscribersClient.registerClient();
    }

    /**
     * Old IClientItemExtensions (custom item renderers), once the items exist.
     */
    public static void collectItemExtensions() {
        for (RegistryObject<Item> item : ModItems.ITEMS.getEntries()) {
            IClientItemExtensions.collect(item.get());
        }
    }

    public static List<KeyMapping> keyMappings() {
        return List.of(InspectKey.INSPECT_KEY, ReloadKey.RELOAD_KEY, ShootKey.SHOOT_KEY, InteractKey.INTERACT_KEY, FireSelectKey.FIRE_SELECT_KEY,
                AimKey.AIM_KEY, CrawlKey.CRAWL_KEY, RefitKey.REFIT_KEY, ZoomKey.ZOOM_KEY, MeleeKey.MELEE_KEY);
    }

    public static @Nullable ClientTooltipComponent tooltip(TooltipComponent data) {
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
    }

    /**
     * One listener running TACZ's loaders in order, displays and indexes depend on what was loaded before them.
     */
    public static net.minecraft.server.packs.resources.PreparableReloadListener clientAssetsListener() {
        return clientAssetsListener(false);
    }

    /**
     * @param withInternalAssets run the internal default assets first, for loaders whose listener ordering would
     *                           wait for a whole listener to finish (NeoForge's addDependency), which deadlocks on the reload barrier
     */
    public static net.minecraft.server.packs.resources.PreparableReloadListener clientAssetsListener(boolean withInternalAssets) {
        java.util.List<net.minecraft.server.packs.resources.PreparableReloadListener> clientListeners = new java.util.ArrayList<>();
        if (withInternalAssets) {
            clientListeners.add(internalAssetsListener());
        }
        ClientAssetsManager.INSTANCE.reloadAndRegister(clientListeners::add);
        return new com.tacz.guns.platform.SequentialReloadListener("TaczClientAssets", () -> clientListeners);
    }

    public static net.minecraft.server.packs.resources.PreparableReloadListener internalAssetsListener() {
        return (ResourceManagerReloadListener) (ResourceManager manager) -> {
            // InternalAssetLoader 需要加载一些默认的动画、模型，需要先于枪包加载。
            InternalAssetLoader.onResourceReload();
            SoundPlayManager.clearSoundResourceCache();
        };
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(GunMod.MOD_ID, path);
    }

    //? if fabric {
    private static net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement hud(IGuiOverlay overlay) {
        return (graphics, deltaTracker) -> {
            Minecraft mc = Minecraft.getInstance();
            overlay.render(mc.gui, graphics, deltaTracker.getGameTimeDeltaPartialTick(false),
                    graphics.guiWidth(), graphics.guiHeight());
        };
    }
    //?}
}
