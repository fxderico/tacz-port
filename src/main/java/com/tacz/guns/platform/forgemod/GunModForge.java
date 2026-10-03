package com.tacz.guns.platform.forgemod;

//? if forge {
/*import com.tacz.guns.GunMod;
import com.tacz.guns.config.ClientConfig;
import com.tacz.guns.config.CommonConfig;
import com.tacz.guns.config.ServerConfig;
import com.tacz.guns.event.LoadingConfigEvent;
import com.tacz.guns.loot.LootTableInjectorModifier;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.platform.forge.registries.DeferredRegister;
import net.minecraft.resources.Identifier;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/^*
 * Forge entry point, does what GunMod#onInitialize does on Fabric.
 ^/
@Mod(GunMod.MOD_ID)
public final class GunModForge {
    public GunModForge(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, CommonConfig.init());
        context.registerConfig(ModConfig.Type.SERVER, ServerConfig.init());
        context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.init());

        //? if >=1.21.6 {
        var modBus = context.getModBusGroup();
        ModConfigEvent.Loading.getBus(modBus).addListener(event -> {
            if (GunMod.MOD_ID.equals(event.getConfig().getModId())) {
                LoadingConfigEvent.onLoadingConfig(event.getConfig());
            }
        });
        ModConfigEvent.Reloading.getBus(modBus).addListener(event -> {
            if (GunMod.MOD_ID.equals(event.getConfig().getModId())) {
                LoadingConfigEvent.onReloadingConfig(event.getConfig());
            }
        });
        RegisterEvent.getBus(modBus).addListener(GunModForge::register);
        //?} else {
        /^var modBus = context.getModEventBus();
        modBus.addListener((ModConfigEvent.Loading event) -> {
            if (GunMod.MOD_ID.equals(event.getConfig().getModId())) {
                LoadingConfigEvent.onLoadingConfig(event.getConfig());
            }
        });
        modBus.addListener((ModConfigEvent.Reloading event) -> {
            if (GunMod.MOD_ID.equals(event.getConfig().getModId())) {
                LoadingConfigEvent.onReloadingConfig(event.getConfig());
            }
        });
        modBus.addListener(GunModForge::register);
        ^///?}

        GunMod.commonInit();
        // every message is registered by now (NetworkHandler's static init), Forge wants the channel built at once
        NetworkHandler.CHANNEL.buildForgeChannel();
        ForgeEventBridge.register();

        if (net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient()) {
            GunModForgeClient.init(modBus);
        }
    }

    private static void register(RegisterEvent event) {
        for (DeferredRegister<?> register : GunMod.registers()) {
            if (event.getRegistryKey().equals(register.getRegistryKey())) {
                register.register();
            }
        }
        event.register(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
                Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "loot_table_injector"), () -> LootTableInjectorModifier.Glm.CODEC);
    }
}
*///?}
