package com.tacz.guns.platform.forge.fml.loading;

import com.tacz.guns.platform.Loader;

import java.nio.file.Path;
import java.util.function.Supplier;

public enum FMLPaths {
    GAMEDIR(Loader::gameDir),
    CONFIGDIR(Loader::configDir);

    private final Supplier<Path> path;

    FMLPaths(Supplier<Path> path) {
        this.path = path;
    }

    public Path get() {
        return path.get();
    }
}
