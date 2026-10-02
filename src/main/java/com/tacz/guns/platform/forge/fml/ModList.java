package com.tacz.guns.platform.forge.fml;

import com.tacz.guns.platform.Loader;

public final class ModList {
    private static final ModList INSTANCE = new ModList();

    public static ModList get() {
        return INSTANCE;
    }

    public boolean isLoaded(String modId) {
        return Loader.isModLoaded(modId);
    }
}
