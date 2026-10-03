package com.tacz.guns.platform;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * What TACZ asks the mod loader, answered by Fabric Loader or FML.
 */
public final class Loader {
    private Loader() {
    }

    public static Path gameDir() {
        //? if !fabric {
        /*return net.neoforged.fml.loading.FMLPaths.GAMEDIR.get();
        *///?} else {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir();
        //?}
    }

    public static Path configDir() {
        //? if !fabric {
        /*return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
        *///?} else {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir();
        //?}
    }

    public static boolean isClient() {
        //? if forge {
        /*return net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient();
        *///?} else if neoforge {
        /*return net.neoforged.fml.loading.FMLEnvironment.getDist().isClient();
        *///?} else {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT;
        //?}
    }

    public static boolean isProduction() {
        //? if forge {
        /*return net.minecraftforge.fml.loading.FMLEnvironment.production;
        *///?} else if neoforge {
        /*return net.neoforged.fml.loading.FMLEnvironment.isProduction();
        *///?} else {
        return !net.fabricmc.loader.api.FabricLoader.getInstance().isDevelopmentEnvironment();
        //?}
    }

    public static boolean isModLoaded(String modId) {
        //? if !fabric {
        /*return net.neoforged.fml.ModList.get().isLoaded(modId);
        *///?} else {
        return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(modId);
        //?}
    }

    /**
     * Version of a loaded mod as written by its author.
     */
    public static Optional<String> modVersion(String modId) {
        //? if !fabric {
        /*return net.neoforged.fml.ModList.get().getModContainerById(modId).map(mod -> mod.getModInfo().getVersion().toString());
        *///?} else {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(modId).map(mod -> mod.getMetadata().getVersion().getFriendlyString());
        //?}
    }

    /**
     * major.minor.patch of a loaded mod, for gun pack version ranges.
     */
    public static Optional<String> modSemanticVersion(String modId) {
        //? if !fabric {
        /*return net.neoforged.fml.ModList.get().getModContainerById(modId).map(mod -> {
            org.apache.maven.artifact.versioning.ArtifactVersion version = mod.getModInfo().getVersion();
            return version.getMajorVersion() + "." + version.getMinorVersion() + "." + version.getIncrementalVersion();
        });
        *///?} else {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(modId).map(mod -> {
            net.fabricmc.loader.api.Version version = mod.getMetadata().getVersion();
            return version instanceof net.fabricmc.loader.api.SemanticVersion semver
                    ? semver.getVersionComponent(0) + "." + semver.getVersionComponent(1) + "." + semver.getVersionComponent(2)
                    : version.getFriendlyString();
        });
        //?}
    }

    /**
     * A file at the root of a mod's jar, or null.
     */
    public static @Nullable Path modFile(String modId, String name) {
        //? if !fabric {
        /*//? if neoforge && >=1.21.9 {
        return net.neoforged.fml.ModList.get().getModContainerById(modId)
                .flatMap(mod -> mod.getModInfo().getOwningFile().getFile().getContents().getContentRoots().stream()
                        .map(root -> root.resolve(name))
                        .filter(Files::exists)
                        .findFirst())
                .orElse(null);
        //?} else {
        /^return net.neoforged.fml.ModList.get().getModContainerById(modId)
                .map(mod -> mod.getModInfo().getOwningFile().getFile().findResource(name))
                .filter(Files::exists)
                .orElse(null);
        ^///?}
        *///?} else {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(modId)
                .flatMap(container -> container.findPath(name))
                .filter(Files::exists)
                .orElse(null);
        //?}
    }

    /**
     * A file or folder inside any loaded mod, or null.
     */
    public static @Nullable Path findInMods(String path) {
        //? if !fabric {
        /*//? if neoforge && >=1.21.9 {
        return net.neoforged.fml.ModList.get().getModFiles().stream()
                .flatMap(file -> file.getFile().getContents().getContentRoots().stream())
                .map(root -> root.resolve(path))
                .filter(Files::exists)
                .findFirst()
                .orElse(null);
        //?} else {
        /^return net.neoforged.fml.ModList.get().getModFiles().stream()
                .map(file -> file.getFile().findResource(path))
                .filter(Files::exists)
                .findFirst()
                .orElse(null);
        ^///?}
        *///?} else {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getAllMods().stream()
                .map(mod -> mod.findPath(path))
                .flatMap(Optional::stream)
                .filter(Files::exists)
                .findFirst()
                .orElse(null);
        //?}
    }
}
