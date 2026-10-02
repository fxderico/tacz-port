package com.tacz.guns.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;

/**
 * Read before the normal configs, straight from .minecraft/tacz/tacz-pre.toml, because the gun pack scan needs it.
 */
public class PreLoadConfig {
    private static final ForgeConfigSpec spec;
    public static ForgeConfigSpec.BooleanValue override;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("gunpack");
        builder.comment("When enabled, the mod will not try to overwrite the default pack under .minecraft/tacz\n" +
                "Since 1.0.4, the overwriting will only run when you start client or a dedicated server");
        override = builder.define("DefaultPackDebug", false);
        builder.pop();
        spec = builder.build();
    }

    public static void load(Path configBasePath) {
        if (spec.isLoaded()) return;
        CommentedFileConfig configData = CommentedFileConfig.builder(configBasePath.resolve("tacz-pre.toml")).sync().preserveInsertionOrder().build();
        configData.load();
        if (!spec.isCorrect(configData)) {
            spec.correct(configData);
        }
        spec.acceptConfig(configData);
        configData.save();
        configData.close();
    }
}
