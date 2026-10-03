package com.tacz.guns.platform.neoforge;

//? if neoforge {
/*import com.tacz.guns.GunMod;
import com.tacz.guns.config.ClientConfig;
import com.tacz.guns.config.CommonConfig;
import com.tacz.guns.config.ServerConfig;
import com.tacz.guns.event.LoadingConfigEvent;
import com.tacz.guns.loot.LootTableInjectorModifier;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.platform.forge.registries.DeferredRegister;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

/^*
 * NeoForge entry point, does what GunMod#onInitialize does on Fabric.
 *^/
@Mod(GunMod.MOD_ID)
public final class GunModNeoForge {
    public GunModNeoForge(IEventBus modBus, ModContainer container) {
        //? if >=26.3 {
        /^// FML 12 renamed the config types, the old file names stay
        container.registerConfig(ModConfig.Type.LOCAL, CommonConfig.init(), "tacz-common.toml");
        container.registerConfig(ModConfig.Type.SYNCED, ServerConfig.init(), "tacz-server.toml");
        ^///?} else {
        container.registerConfig(ModConfig.Type.COMMON, CommonConfig.init());
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.init());
        //?}
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.init());
        modBus.addListener(ModConfigEvent.Loading.class, event -> {
            if (GunMod.MOD_ID.equals(event.getConfig().getModId())) {
                LoadingConfigEvent.onLoadingConfig(event.getConfig());
            }
        });
        modBus.addListener(ModConfigEvent.Reloading.class, event -> {
            if (GunMod.MOD_ID.equals(event.getConfig().getModId())) {
                LoadingConfigEvent.onReloadingConfig(event.getConfig());
            }
        });

        modBus.addListener(RegisterEvent.class, GunModNeoForge::register);
        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> NetworkHandler.CHANNEL.registerPayloads(event.registrar("1")));

        GunMod.commonInit();
        NeoForgeEventBridge.register(NeoForge.EVENT_BUS);

        if (FMLEnvironment.getDist().isClient()) {
            GunModNeoForgeClient.init(modBus);
        }
    }

    private static void register(RegisterEvent event) {
        for (DeferredRegister<?> register : GunMod.registers()) {
            if (event.getRegistryKey().equals(register.getRegistryKey())) {
                register.register();
            }
        }
        event.register(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
                Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "loot_table_injector"), () -> LootTableInjectorModifier.Glm.CODEC);
    }
}
*///?}
