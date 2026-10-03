package com.tacz.guns;

import com.tacz.guns.api.resource.ResourceManager;
import com.tacz.guns.command.RootCommand;
import com.tacz.guns.config.ClientConfig;
import com.tacz.guns.config.CommonConfig;
import com.tacz.guns.config.ServerConfig;
import com.tacz.guns.entity.sync.ModSyncedEntityData;
import com.tacz.guns.event.LoadingConfigEvent;
import com.tacz.guns.init.*;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.loot.LootTableInjectorModifier;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.platform.EventSubscribers;
import com.tacz.guns.platform.forge.fml.loading.FMLLoader;
import com.tacz.guns.resource.GunPackLoader;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.api.item.gun.GunItemManager;
import com.tacz.guns.network.message.handshake.ServerMessageSyncedEntityDataMapping;
import net.minecraft.server.packs.PackType;
import net.minecraft.resources.Identifier;
import com.tacz.guns.resource.CommonAssetsManager;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tacz.guns.platform.forge.registries.DeferredRegister;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class GunMod /*? if fabric {*/implements net.fabricmc.api.ModInitializer/*?}*/ {
    public static final String MOD_ID = "tacz";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    /**
     * 默认模型包文件夹
     */
    public static final String DEFAULT_GUN_PACK_NAME = "tacz_default_gun";

    /**
     * Registers, in the order Forge fired its registry events. Fabric calls this right away,
     * NeoForge from its RegisterEvent (see platform.neoforge.GunModNeoForge).
     */
    public static List<DeferredRegister<?>> registers() {
        return List.of(
            ModSounds.SOUNDS,
            ModParticles.PARTICLE_TYPES,
            ModAttributes.ATTRIBUTES,
            ModBlocks.BLOCKS,
            ModBlocks.TILE_ENTITIES,
            ModItems.ITEMS,
            ModEntities.ENTITY_TYPES,
            ModContainer.CONTAINER_TYPE,
            ModRecipe.RECIPE_SERIALIZERS,
            ModRecipe.RECIPE_TYPES,
            ModCreativeTabs.TABS
        );
    }

    //? if fabric {
    @Override
    public void onInitialize() {
        fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.COMMON, CommonConfig.init());
        fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.SERVER, ServerConfig.init());
        fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.CLIENT, ClientConfig.init());
        fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents.loading(MOD_ID).register(LoadingConfigEvent::onLoadingConfig);
        fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents.reloading(MOD_ID).register(LoadingConfigEvent::onReloadingConfig);

        registers().forEach(DeferredRegister::register);
        commonInit();

        // gun pack data (indexes, gun data, scripts...) reloads with the server data packs
        //? if >=1.21.9 {
        net.fabricmc.fabric.api.resource.v1.ResourceLoader.get(PackType.SERVER_DATA).registerReloader(Identifier.fromNamespaceAndPath(MOD_ID, "common_assets"), CommonAssetsManager.RELOAD_LISTENER);
        // the smith table screen needs the recipes on the client
        net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization.synchronizeRecipeSerializer(ModRecipe.GUN_SMITH_TABLE_RECIPE_SERIALIZER.get());
        //?} else {
        /*com.tacz.guns.platform.FabricReloadListeners.registerServer(Identifier.fromNamespaceAndPath(MOD_ID, "common_assets"), CommonAssetsManager.RELOAD_LISTENER);
        // the smith table screen needs the recipes on the client, sent on join and after /reload
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) ->
                com.tacz.guns.network.NetworkHandler.sendToClientPlayer(com.tacz.guns.network.message.ServerMessageSyncRecipes.of(player.level().getServer()), player));
        *///?}

        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> RootCommand.register(dispatcher));
        LootTableInjectorModifier.register();

        // the synced entity data handshake, Forge did this during login
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerJoin(handler.player));

        com.tacz.guns.platform.FabricEventBridge.registerCommon();
    }
    //?}

    /**
     * Loader independent setup, after the configs are registered.
     */
    public static void commonInit() {
        GunItemManager.registerGunItem(ModernKineticGunItem.TYPE_NAME, ModItems.MODERN_KINETIC_GUN);

        registerDefaultExtraGunPack();
        AttachmentPropertyManager.registerModifier();

        NetworkHandler.init();
        ModSyncedEntityData.init();

        EventSubscribers.registerCommon();
    }

    /**
     * The synced entity data handshake, Forge did this during login.
     */
    public static void onPlayerJoin(ServerPlayer player) {
        NetworkHandler.sendToClientPlayer(new ServerMessageSyncedEntityDataMapping(), player);
    }

    private static void registerDefaultExtraGunPack() {
        String jarDefaultPackPath = String.format("/assets/%s/custom/%s", GunMod.MOD_ID, DEFAULT_GUN_PACK_NAME);
        ResourceManager.registerExportResource(GunMod.class, jarDefaultPackPath);
    }
}
