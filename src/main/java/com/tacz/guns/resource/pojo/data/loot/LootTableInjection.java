package com.tacz.guns.resource.pojo.data.loot;

import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;

public record LootTableInjection(List<Identifier> lootTables, LootTable lootTable) {
    public static LootTableInjection fromJson(Identifier fileId, JsonElement element, HolderLookup.Provider registries) {
        JsonObject object = GsonHelper.convertToJsonObject(element, "loot injection");
        List<Identifier> lootTables = readLootTables(fileId, object);
        if (!object.has("pools")) {
            throw new JsonParseException("Loot injection " + fileId + " must define pools");
        }
        JsonObject table = object.deepCopy();
        table.remove("loot_tables");
        table.remove("loot_table");
        upgradeFunctions(table);
        LootTable lootTable = LootTable.DIRECT_CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), table)
                .getOrThrow(error -> new JsonParseException("Invalid loot injection " + fileId + ": " + error));
        return new LootTableInjection(lootTables, lootTable);
    }

    /**
     * 1.20 set_nbt became set_custom_data, TACZ item data lives in custom_data so the tag carries over as is.
     */
    private static void upgradeFunctions(JsonElement element) {
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(LootTableInjection::upgradeFunctions);
        } else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("function")) {
                String function = GsonHelper.getAsString(object, "function");
                if (function.equals("set_nbt") || function.equals("minecraft:set_nbt")) {
                    object.addProperty("function", "minecraft:set_custom_data");
                }
            }
            object.entrySet().forEach(entry -> upgradeFunctions(entry.getValue()));
        }
    }

    private static List<Identifier> readLootTables(Identifier fileId, JsonObject object) {
        List<Identifier> lootTables = new ArrayList<>();
        if (object.has("loot_tables")) {
            for (JsonElement table : GsonHelper.getAsJsonArray(object, "loot_tables")) {
                lootTables.add(Identifier.parse(GsonHelper.convertToString(table, "loot table")));
            }
        } else if (object.has("loot_table")) {
            lootTables.add(Identifier.parse(GsonHelper.getAsString(object, "loot_table")));
        } else {
            throw new JsonParseException("Loot injection " + fileId + " must define loot_table or loot_tables");
        }
        return List.copyOf(lootTables);
    }

    public List<ItemStack> createStacks(LootContext context) {
        List<ItemStack> stacks = new ArrayList<>();
        lootTable.getRandomItemsRaw(context, stacks::add);
        return stacks;
    }
}
