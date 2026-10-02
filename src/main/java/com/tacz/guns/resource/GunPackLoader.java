package com.tacz.guns.resource;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.resource.ResourceManager;
import com.tacz.guns.config.PreLoadConfig;
import com.tacz.guns.util.GetJarResources;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Finds gun packs in .minecraft/tacz and exposes all of them as one built-in pack ("tacz_resources"),
 * for client resources and for server data. See {@link GunPackResources} for the old folder name remapping.
 */
public enum GunPackLoader {
    INSTANCE;
    private static final Marker MARKER = MarkerManager.getMarker("GunPackFinder");
    /**
     * Zip gun packs are opened once as a zip file system and reused across reloads.
     */
    private static final Map<Path, FileSystem> ZIP_FILE_SYSTEMS = new ConcurrentHashMap<>();
    public PackType packType;
    private boolean firstLoad = true;

    public RepositorySource forType(PackType type) {
        return consumer -> {
            Pack extensionsPack = discoverExtensions(type);
            if (extensionsPack != null) {
                consumer.accept(extensionsPack);
            }
        };
    }

    @Nullable
    public synchronized Pack discoverExtensions(PackType type) {
        Path resourcePacksPath = FabricLoader.getInstance().getGameDir().resolve("tacz");
        File folder = resourcePacksPath.toFile();
        if (!folder.isDirectory()) {
            try {
                Files.createDirectories(folder.toPath());
            } catch (Exception e) {
                GunMod.LOGGER.warn(MARKER, "Failed to init tacz resource directory...", e);
                return null;
            }
        }

        // 确保配置文件加载，这个阶段将比标准的forge配置文件加载早
        PreLoadConfig.load(resourcePacksPath);

        // 仅在第一次加载时复制默认资源包
        if (firstLoad) {
            if (!PreLoadConfig.override.get()) {
                for (ResourceManager.ExtraEntry entry : ResourceManager.EXTRA_ENTRIES) {
                    GetJarResources.copyModDirectory(entry.modMainClass(), entry.srcPath(), resourcePacksPath, entry.extraDirName());
                }
            }
            firstLoad = false;
        }

        GunMod.LOGGER.info(MARKER, "Start scanning for gun packs in {}", resourcePacksPath);
        List<GunPack> gunPacks = scanExtensions(resourcePacksPath);
        GunMod.LOGGER.info(MARKER, "Found {} possible gunpack(s) and added them to resource set.", gunPacks.size());

        List<Path> roots = new ArrayList<>();
        for (GunPack gunPack : gunPacks) {
            Path root = openRoot(gunPack.path);
            if (root != null) {
                roots.add(root);
            }
        }

        PackLocationInfo info = new PackLocationInfo("tacz_resources", Component.literal("TACZ Resources"), PackSource.BUILT_IN, Optional.empty());
        Pack.ResourcesSupplier supplier = new Pack.ResourcesSupplier() {
            //? if >=26.3 {
            /*@Override
            public net.minecraft.server.packs.PackMetadataResources openMetadata(PackLocationInfo location) {
                return new GunPackResources(location, roots, type);
            }

            @Override
            public java.util.stream.Stream<PackResources> openResources(PackLocationInfo location, Pack.Metadata metadata) {
                return java.util.stream.Stream.of(new GunPackResources(location, roots, type));
            }
            *///?} else {
            @Override
            public PackResources openPrimary(PackLocationInfo location) {
                return new GunPackResources(location, roots, type);
            }

            @Override
            public PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
                return new GunPackResources(location, roots, type);
            }
            //?}
        };
        return Pack.readMetaAndCreate(info, supplier, type, new PackSelectionConfig(true, Pack.Position.BOTTOM, false));
    }

    @Nullable
    private static Path openRoot(Path packPath) {
        if (Files.isDirectory(packPath)) {
            return packPath;
        }
        try {
            FileSystem fs = ZIP_FILE_SYSTEMS.get(packPath);
            if (fs == null || !fs.isOpen()) {
                fs = FileSystems.newFileSystem(packPath);
                ZIP_FILE_SYSTEMS.put(packPath, fs);
            }
            return fs.getPath("/");
        } catch (IOException e) {
            GunMod.LOGGER.error(MARKER, "Failed to open gun pack zip {}", packPath, e);
            return null;
        }
    }

    public static @Nullable Path getModIcon(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .flatMap(container -> container.findPath("icon.png"))
                .filter(Files::exists)
                .orElse(null);
    }

    private static GunPack fromDirPath(Path path) throws IOException {
        Path packInfoFilePath = path.resolve("gunpack.meta.json");
        try (InputStream stream = Files.newInputStream(packInfoFilePath)) {
            PackMeta info = CommonAssetsManager.GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), PackMeta.class);

            if (info == null) {
                GunMod.LOGGER.warn(MARKER, "Failed to read info json: {}", packInfoFilePath.getFileName());
                return null;
            }

            if (info.getDependencies() != null && !modVersionAllMatch(info)) {
                GunMod.LOGGER.warn(MARKER, "Mod version mismatch: {}", packInfoFilePath.getFileName());
                return null;
            }

            return new GunPack(path, info.getName());
        } catch (IOException | JsonSyntaxException | JsonIOException | InvalidVersionSpecificationException exception) {
            GunMod.LOGGER.warn(MARKER, "Failed to read info json: {}", packInfoFilePath.getFileName());
            GunMod.LOGGER.warn(exception.getMessage());
        }
        return null;
    }

    private static GunPack fromZipPath(Path path) {
        try (ZipFile zipFile = new ZipFile(path.toFile())) {
            ZipEntry extDescriptorEntry = zipFile.getEntry("gunpack.meta.json");
            if (extDescriptorEntry == null) {
                GunMod.LOGGER.error(MARKER, "Failed to load extension from ZIP {}. Error: {}", path.getFileName(), "No gunpack.meta.json found");
                return null;
            }

            try (InputStream stream = zipFile.getInputStream(extDescriptorEntry)) {
                PackMeta info = CommonAssetsManager.GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), PackMeta.class);

                if (info == null) {
                    GunMod.LOGGER.warn(MARKER, "Failed to read info json: {}", path.getFileName());
                    return null;
                }

                if (info.getDependencies() != null && !modVersionAllMatch(info)) {
                    GunMod.LOGGER.warn(MARKER, "Mod version mismatch: {}", path.getFileName());
                    return null;
                }

                return new GunPack(path, info.getName());
            } catch (IOException | JsonSyntaxException | JsonIOException | InvalidVersionSpecificationException e) {
                GunMod.LOGGER.error(MARKER, "Failed to load extension from ZIP {}. Error: {}", path.getFileName(), e);
                return null;
            }
        } catch (IOException e) {
            GunMod.LOGGER.error(MARKER, "Failed to load extension from ZIP {}. Error: {}", path.getFileName(), e);
            return null;
        }
    }

    private static List<GunPack> scanExtensions(Path extensionsPath) {
        List<GunPack> gunPacks = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(extensionsPath)) {
            for (Path entry : stream) {
                GunPack gunPack = null;
                if (Files.isDirectory(entry)) {
                    gunPack = fromDirPath(entry);
                } else if (entry.toString().endsWith(".zip")) {
                    gunPack = fromZipPath(entry);
                }
                if (gunPack != null) {
                    GunMod.LOGGER.info(MARKER, "- {}, Main namespace: {}", gunPack.path.getFileName(), gunPack.name);
                    gunPacks.add(gunPack);
                }
            }
        } catch (IOException e) {
            GunMod.LOGGER.error(MARKER, "Failed to scan extensions from {}. Error: {}", extensionsPath, e);
        }

        return gunPacks;
    }

    private static boolean modVersionAllMatch(PackMeta info) throws InvalidVersionSpecificationException {
        HashMap<String, String> dependencies = info.getDependencies();
        for (String modId : dependencies.keySet()) {
            if (!modVersionMatch(modId, dependencies.get(modId))) {
                return false;
            }
        }
        return true;
    }

    private static boolean modVersionMatch(String modId, String version) throws InvalidVersionSpecificationException {
        // gun packs written for Forge often pin "forge", there is nothing to check against on Fabric
        if ("forge".equals(modId) || "neoforge".equals(modId)) {
            return true;
        }
        VersionRange versionRange = VersionRange.createFromVersionSpec(version);
        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(modId);
        return container.map(mod -> {
            Version modVersion = mod.getMetadata().getVersion();
            String friendly = modVersion instanceof SemanticVersion semver
                    ? semver.getVersionComponent(0) + "." + semver.getVersionComponent(1) + "." + semver.getVersionComponent(2)
                    : modVersion.getFriendlyString();
            return versionRange.containsVersion(new DefaultArtifactVersion(friendly));
        }).orElse(false);
    }

    public record GunPack(Path path, String name) {
    }
}
