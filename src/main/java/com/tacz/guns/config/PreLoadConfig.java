package com.tacz.guns.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import java.nio.file.Path;

/**
 * Read before the normal configs, straight from .minecraft/tacz/tacz-pre.toml, because the gun pack scan needs it.
 * Plain night-config: the config specs only take files their loader opened.
 */
public class PreLoadConfig {
    private static final String KEY = "gunpack.DefaultPackDebug";
    private static final String COMMENT = "When enabled, the mod will not try to overwrite the default pack under .minecraft/tacz\n" +
            "Since 1.0.4, the overwriting will only run when you start client or a dedicated server";
    private static CommentedFileConfig config;

    public static final Setting override = new Setting();

    public static synchronized void load(Path configBasePath) {
        if (config != null) {
            return;
        }
        CommentedFileConfig configData = CommentedFileConfig.builder(configBasePath.resolve("tacz-pre.toml")).sync().preserveInsertionOrder().build();
        configData.load();
        if (!(configData.get(KEY) instanceof Boolean)) {
            configData.set(KEY, false);
        }
        configData.setComment(KEY, COMMENT);
        configData.save();
        config = configData;
    }

    public static final class Setting {
        private Setting() {
        }

        public boolean get() {
            return config != null && config.<Boolean>getOrElse(KEY, false);
        }

        public synchronized void set(boolean value) {
            if (config != null) {
                config.set(KEY, value);
                config.save();
            }
        }
    }
}
