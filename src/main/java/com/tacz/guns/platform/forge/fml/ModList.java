package com.tacz.guns.platform.forge.fml;

import net.fabricmc.loader.api.FabricLoader;

public final class ModList {
    private static final ModList INSTANCE = new ModList();

    public static ModList get() {
        return INSTANCE;
    }

    public boolean isLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
