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
import com.tacz.guns.platform.FabricEventBridge;
import com.tacz.guns.platform.EventSubscribers;
import com.tacz.guns.platform.forge.fml.loading.FMLLoader;
import com.tacz.guns.resource.GunPackLoader;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.api.item.gun.GunItemManager;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import com.tacz.guns.network.message.handshake.ServerMessageSyncedEntityDataMapping;
import net.minecraft.server.packs.PackType;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import com.tacz.guns.resource.CommonAssetsManager;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GunMod implements ModInitializer {
    public static final String MOD_ID = "tacz";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    /**
     * 默认模型包文件夹
     */
    public static final String DEFAULT_GUN_PACK_NAME = "tacz_default_gun";

    @Override
    public void onInitialize() {
        ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.COMMON, CommonConfig.init());
        ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.SERVER, ServerConfig.init());
        ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.CLIENT, ClientConfig.init());
        ModConfigEvents.loading(MOD_ID).register(LoadingConfigEvent::onLoadingConfig);
        ModConfigEvents.reloading(MOD_ID).register(LoadingConfigEvent::onReloadingConfig);

        // same order Forge fires its registry events in
        ModSounds.SOUNDS.register();
        ModParticles.PARTICLE_TYPES.register();
        ModAttributes.ATTRIBUTES.register();
        ModBlocks.BLOCKS.register();
        ModBlocks.TILE_ENTITIES.register();
        ModItems.ITEMS.register();
        ModEntities.ENTITY_TYPES.register();
        ModContainer.CONTAINER_TYPE.register();
        ModRecipe.RECIPE_SERIALIZERS.register();
        ModRecipe.RECIPE_TYPES.register();
        ModCreativeTabs.TABS.register();
        GunItemManager.registerGunItem(ModernKineticGunItem.TYPE_NAME, ModItems.MODERN_KINETIC_GUN);

        registerDefaultExtraGunPack();
        AttachmentPropertyManager.registerModifier();

        // gun pack data (indexes, gun data, scripts...) reloads with the server data packs
        ResourceLoader.get(PackType.SERVER_DATA).registerReloader(Identifier.fromNamespaceAndPath(MOD_ID, "common_assets"), CommonAssetsManager.RELOAD_LISTENER);
        // the smith table screen needs the recipes on the client
        RecipeSynchronization.synchronizeRecipeSerializer(ModRecipe.GUN_SMITH_TABLE_RECIPE_SERIALIZER.get());

        NetworkHandler.init();
        ModSyncedEntityData.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> RootCommand.register(dispatcher));
        LootTableInjectorModifier.register();

        // the synced entity data handshake, Forge did this during login
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                NetworkHandler.sendToClientPlayer(new ServerMessageSyncedEntityDataMapping(), handler.player));

        EventSubscribers.registerCommon();
        FabricEventBridge.registerCommon();
    }

    private static void registerDefaultExtraGunPack() {
        String jarDefaultPackPath = String.format("/assets/%s/custom/%s", GunMod.MOD_ID, DEFAULT_GUN_PACK_NAME);
        ResourceManager.registerExportResource(GunMod.class, jarDefaultPackPath);
    }
}
