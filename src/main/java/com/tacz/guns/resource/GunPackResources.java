package com.tacz.guns.resource;

import com.tacz.guns.GunMod;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
//? if >=1.21.9
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.InclusiveRange;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * All gun packs served as a single pack. Gun packs are written against the 1.20.1 layout, so the data folders
 * that were renamed in 1.21 are looked up under their old names too ("recipe" also reads "recipes", and so on).
 * Later packs in the list win, like stacked resource packs.
 */
public class GunPackResources implements PackResources {
    /**
     * new 1.21 folder -> old 1.20 folder, data side only
     */
    private static final Map<String, String> LEGACY_DATA_DIRS = Map.ofEntries(
            Map.entry("recipe", "recipes"),
            Map.entry("loot_table", "loot_tables"),
            Map.entry("advancement", "advancements"),
            Map.entry("function", "functions"),
            Map.entry("predicate", "predicates"),
            Map.entry("item_modifier", "item_modifiers"),
            Map.entry("structure", "structures"),
            Map.entry("tags/item", "tags/items"),
            Map.entry("tags/block", "tags/blocks"),
            Map.entry("tags/entity_type", "tags/entity_types"),
            Map.entry("tags/fluid", "tags/fluids"),
            Map.entry("tags/game_event", "tags/game_events"),
            Map.entry("tags/function", "tags/functions")
    );

    private final PackLocationInfo location;
    private final List<Path> roots;
    private final PackType packType;

    public GunPackResources(PackLocationInfo location, List<Path> roots, PackType packType) {
        this.location = location;
        this.roots = roots;
        this.packType = packType;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... paths) {
        if (paths.length == 1 && paths[0].equals("pack.png")) {
            Path logoPath = GunPackLoader.getModIcon(GunMod.MOD_ID);
            if (logoPath != null) {
                return IoSupplier.create(logoPath);
            }
        }
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, Identifier id) {
        for (int i = roots.size() - 1; i >= 0; i--) {
            Path base = roots.get(i).resolve(type.getDirectory()).resolve(id.getNamespace());
            Path file = resolve(base, id.getPath());
            if (file != null && Files.isRegularFile(file)) {
                return IoSupplier.create(file);
            }
            if (type == PackType.SERVER_DATA) {
                String legacy = toLegacy(id.getPath());
                if (legacy != null) {
                    Path legacyFile = resolve(base, legacy);
                    if (legacyFile != null && Files.isRegularFile(legacyFile)) {
                        return IoSupplier.create(legacyFile);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        Map<Identifier, IoSupplier<InputStream>> found = new LinkedHashMap<>();
        for (Path root : roots) {
            Path base = root.resolve(type.getDirectory()).resolve(namespace);
            list(base, path, path, namespace, found);
            if (type == PackType.SERVER_DATA) {
                String legacy = toLegacy(path);
                if (legacy != null) {
                    list(base, legacy, path, namespace, found);
                }
            }
        }
        found.forEach(output);
    }

    /**
     * Lists base/dir and reports every file as namespace:reportedDir/relative, so files found in an old folder
     * show up under the new name.
     */
    private static void list(Path base, String dir, String reportedDir, String namespace, Map<Identifier, IoSupplier<InputStream>> out) {
        Path start = resolve(base, dir);
        if (start == null || !Files.isDirectory(start)) {
            return;
        }
        try (Stream<Path> files = Files.walk(start)) {
            files.filter(Files::isRegularFile).forEach(file -> {
                String relative = start.relativize(file).toString().replace('\\', '/');
                Identifier id = Identifier.tryBuild(namespace, reportedDir + "/" + relative);
                if (id != null) {
                    out.put(id, IoSupplier.create(file));
                }
            });
        } catch (IOException e) {
            GunMod.LOGGER.warn("Failed to list gun pack resources in {}", start, e);
        }
    }

    @Nullable
    private static String toLegacy(String path) {
        for (Map.Entry<String, String> entry : LEGACY_DATA_DIRS.entrySet()) {
            String now = entry.getKey();
            if (path.equals(now) || path.startsWith(now + "/")) {
                return entry.getValue() + path.substring(now.length());
            }
        }
        return null;
    }

    @Nullable
    private static Path resolve(Path base, String path) {
        Path result = base;
        for (String part : path.split("/")) {
            if (part.isEmpty()) {
                continue;
            }
            if (part.equals("..")) {
                return null;
            }
            result = result.resolve(part);
        }
        return result;
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        Set<String> namespaces = new HashSet<>();
        for (Path root : roots) {
            Path dir = root.resolve(type.getDirectory());
            if (!Files.isDirectory(dir)) {
                continue;
            }
            try (Stream<Path> children = Files.list(dir)) {
                children.filter(Files::isDirectory).forEach(child -> {
                    String name = child.getFileName().toString().replace("/", "");
                    if (Identifier.isValidNamespace(name)) {
                        namespaces.add(name);
                    }
                });
            } catch (IOException e) {
                GunMod.LOGGER.warn("Failed to list gun pack namespaces in {}", dir, e);
            }
        }
        return namespaces;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getMetadataSection(MetadataSectionType<T> type) {
        //? if >=1.21.9 {
        if (type == PackMetadataSection.forPackType(packType) || type == PackMetadataSection.FALLBACK_TYPE) {
            PackFormat format = SharedConstants.getCurrentVersion().packVersion(packType);
            return (T) new PackMetadataSection(Component.translatable("tacz.resources.modresources"), new InclusiveRange<>(format));
        }
        //?} else {
        /*if (type == PackMetadataSection.TYPE) {
            int format = SharedConstants.getCurrentVersion().packVersion(packType);
            return (T) new PackMetadataSection(Component.translatable("tacz.resources.modresources"), format, java.util.Optional.empty());
        }
        *///?}
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return location;
    }

    @Override
    public void close() {
        // zip file systems are cached by GunPackLoader and stay open
    }
}
