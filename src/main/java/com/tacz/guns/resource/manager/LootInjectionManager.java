package com.tacz.guns.resource.manager;

import net.minecraft.core.HolderLookup;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.tacz.guns.GunMod;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.pojo.data.loot.LootTableInjection;
import com.tacz.guns.util.ResourceScanner;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LootInjectionManager extends SimplePreparableReloadListener<Map<Identifier, List<JsonElement>>> {
    private final Map<Identifier, List<LootTableInjection>> injections = Maps.newHashMap();
    private final Gson gson = CommonAssetsManager.GSON;
    private final Marker marker = MarkerManager.getMarker("LootInjection");
    private final FileToIdConverter fileToIdConverter = FileToIdConverter.json("tacz_loot_injectors");

    private HolderLookup.Provider registries;

    /**
     * NeoForge has no registry lookup in the reload state, its AddServerReloadListenersEvent hands it over here instead.
     */
    public static HolderLookup.Provider reloadRegistries;

    //? if fabric {
    @Override
    public void prepareSharedState(SharedState sharedState) {
        this.registries = sharedState.get(net.fabricmc.fabric.api.resource.v1.ResourceLoader.RELOADER_REGISTRY_LOOKUP_KEY);
    }
    //?} else {
    /*@Override
    public void prepareSharedState(SharedState sharedState) {
        this.registries = reloadRegistries;
    }
    *///?}

    @Override
    protected @NotNull Map<Identifier, List<JsonElement>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return ResourceScanner.scanDirectoryAll(resourceManager, fileToIdConverter, gson);
    }

    @Override
    protected void apply(Map<Identifier, List<JsonElement>> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        injections.clear();
        for (Map.Entry<Identifier, List<JsonElement>> entry : object.entrySet()) {
            Identifier id = entry.getKey();
            for (JsonElement element : entry.getValue()) {
                try {
                    LootTableInjection injection = LootTableInjection.fromJson(id, element, registries);
                    for (Identifier lootTable : injection.lootTables()) {
                        injections.computeIfAbsent(lootTable, key -> new java.util.ArrayList<>()).add(injection);
                    }
                } catch (RuntimeException e) {
                    GunMod.LOGGER.error(marker, "Failed to load loot injection {}", id, e);
                }
            }
        }
    }

    public List<LootTableInjection> getInjections(Identifier lootTable) {
        return injections.getOrDefault(lootTable, Collections.emptyList());
    }
}
