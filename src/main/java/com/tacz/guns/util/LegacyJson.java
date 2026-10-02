package com.tacz.guns.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

/**
 * Gun packs are written in the 1.20.1 Forge format. These helpers read the old JSON shapes:
 * {"item": ..} / {"tag": ..} ingredients with Forge tags, and item stacks with an "nbt" object.
 */
public final class LegacyJson {
    /**
     * Forge tag -> Fabric convention tag, for the ones whose path changed. Everything else keeps its path under "c".
     */
    private static final Map<String, String> FORGE_TAGS = Map.ofEntries(
            Map.entry("forge:leather", "c:leathers"),
            Map.entry("forge:gunpowder", "c:gunpowders"),
            Map.entry("forge:glass", "c:glass_blocks"),
            Map.entry("forge:glass_panes", "c:glass_panes"),
            Map.entry("forge:string", "c:strings"),
            Map.entry("forge:stone", "c:stones"),
            Map.entry("forge:cobblestone", "c:cobblestones"),
            Map.entry("forge:feathers", "c:feathers"),
            Map.entry("forge:slimeballs", "c:slime_balls"),
            Map.entry("forge:bones", "c:bones"),
            Map.entry("forge:obsidian", "c:obsidians"),
            Map.entry("forge:chests", "c:chests"),
            Map.entry("forge:chests/wooden", "c:chests/wooden"),
            Map.entry("forge:rods/wooden", "c:rods/wooden")
    );

    /**
     * Item lookup of the running data pack reload, tags are only bound after recipes are parsed.
     */
    private static final ThreadLocal<HolderGetter<Item>> ITEM_LOOKUP = new ThreadLocal<>();

    private LegacyJson() {
    }

    public static <T> T withItemLookup(@Nullable HolderGetter<Item> lookup, Supplier<T> action) {
        HolderGetter<Item> previous = ITEM_LOOKUP.get();
        ITEM_LOOKUP.set(lookup);
        try {
            return action.get();
        } finally {
            ITEM_LOOKUP.set(previous);
        }
    }

    private static HolderGetter<Item> items() {
        HolderGetter<Item> lookup = ITEM_LOOKUP.get();
        return lookup != null ? lookup : BuiltInRegistries.ITEM;
    }

    public static Identifier mapTag(String tag) {
        String mapped = FORGE_TAGS.get(tag);
        if (mapped == null && tag.startsWith("forge:")) {
            mapped = "c:" + tag.substring("forge:".length());
        }
        return Identifier.parse(mapped == null ? tag : mapped);
    }

    public static Ingredient ingredient(JsonElement json) {
        List<Item> items = new ArrayList<>();
        List<TagKey<Item>> tags = new ArrayList<>();
        collect(json, items, tags);
        if (tags.size() == 1 && items.isEmpty()) {
            HolderSet.Named<Item> set = items().getOrThrow(tags.get(0));
            return Ingredient.of(set);
        }
        if (!tags.isEmpty()) {
            // 1.21 ingredients are a single holder set, a mix of tags is flattened to their items
            for (TagKey<Item> tag : tags) {
                items().get(tag).ifPresent(set -> set.forEach(holder -> items.add(holder.value())));
            }
        }
        if (items.isEmpty()) {
            throw new JsonSyntaxException("Empty ingredient " + json);
        }
        return Ingredient.of(items.stream());
    }

    private static void collect(JsonElement json, List<Item> items, List<TagKey<Item>> tags) {
        if (json.isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray()) {
                collect(element, items, tags);
            }
            return;
        }
        if (json.isJsonPrimitive()) {
            String value = json.getAsString();
            if (value.startsWith("#")) {
                tags.add(TagKey.create(Registries.ITEM, mapTag(value.substring(1))));
            } else {
                items.add(item(Identifier.parse(value)));
            }
            return;
        }
        JsonObject object = json.getAsJsonObject();
        if (object.has("tag")) {
            tags.add(TagKey.create(Registries.ITEM, mapTag(GsonHelper.getAsString(object, "tag"))));
        } else if (object.has("item")) {
            items.add(item(Identifier.parse(GsonHelper.getAsString(object, "item"))));
        } else {
            throw new JsonSyntaxException("Expected item or tag in " + object);
        }
    }

    private static Item item(Identifier id) {
        return BuiltInRegistries.ITEM.getOptional(id).orElseThrow(() -> new JsonSyntaxException("Unknown item " + id));
    }

    /**
     * Forge's CraftingHelper#getNBT: either an SNBT string or a JSON object.
     */
    public static CompoundTag nbt(JsonElement json) {
        try {
            if (json.isJsonPrimitive()) {
                return TagParser.parseCompoundFully(json.getAsString());
            }
            Tag tag = JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, json);
            if (tag instanceof CompoundTag compound) {
                return compound;
            }
            throw new JsonSyntaxException("Expected an NBT object, got " + json);
        } catch (Exception e) {
            throw new JsonSyntaxException("Invalid NBT " + json + ": " + e.getMessage(), e);
        }
    }

    /**
     * Forge's CraftingHelper#getItemStack. The display name moves to the custom_name component,
     * the rest of the NBT goes into custom_data like every other TACZ item tag.
     */
    public static ItemStack itemStack(JsonObject json) {
        String id = json.has("item") ? GsonHelper.getAsString(json, "item") : GsonHelper.getAsString(json, "id");
        ItemStack stack = new ItemStack(item(Identifier.parse(id)), GsonHelper.getAsInt(json, "count", 1));
        if (json.has("nbt")) {
            CompoundTag tag = nbt(json.get("nbt"));
            CompoundTag display = tag.getCompoundOrEmpty("display");
            String name = display.getStringOr("Name", "");
            if (!name.isEmpty()) {
                ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(name)).result()
                        .ifPresent(component -> stack.set(DataComponents.CUSTOM_NAME, (Component) component));
                display.remove("Name");
                if (display.isEmpty()) {
                    tag.remove("display");
                }
            }
            // entity data for spawn items (paintings) has no simple 1.21 equivalent here
            tag.remove("EntityTag");
            if (!tag.isEmpty()) {
                ItemNbt.set(stack, tag);
            }
        }
        return stack;
    }

    public static JsonArray asArray(JsonElement element) {
        if (element.isJsonArray()) {
            return element.getAsJsonArray();
        }
        JsonArray array = new JsonArray();
        array.add(element);
        return array;
    }
}
