package com.tacz.guns.crafting;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.tacz.guns.crafting.result.GunSmithTableResult;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.pojo.data.recipe.TableRecipe;
import com.tacz.guns.util.LegacyJson;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Gun pack recipes stay in TACZ's own JSON format (parsed with Gson as before). The codec hands the whole
 * recipe object to Gson, with the reload's item lookup so tags resolve.
 */
public final class GunSmithTableSerializer {
    private static final Identifier UNKNOWN_ID = Identifier.fromNamespaceAndPath("tacz", "unknown");

    private static final MapCodec<GunSmithTableRecipe> CODEC = MapCodec.assumeMapUnsafe(Codec.PASSTHROUGH.flatXmap(dynamic -> {
        try {
            JsonElement json = dynamic.convert(JsonOps.INSTANCE).getValue();
            var itemLookup = dynamic.getOps() instanceof RegistryOps<?> registryOps
                    ? registryOps.getter(Registries.ITEM).orElse(null) : null;
            TableRecipe tableRecipe = LegacyJson.withItemLookup(itemLookup, () -> CommonAssetsManager.GSON.fromJson(json, TableRecipe.class));
            if (tableRecipe == null || tableRecipe.getResult() == null || tableRecipe.getMaterials() == null) {
                return DataResult.error(() -> "Invalid gun smith table recipe " + json);
            }
            return DataResult.success(new GunSmithTableRecipe(UNKNOWN_ID, tableRecipe));
        } catch (Exception e) {
            return DataResult.error(() -> "Invalid gun smith table recipe: " + e.getMessage());
        }
    }, recipe -> DataResult.error(() -> "Gun smith table recipes can not be encoded")));

    private static final StreamCodec<RegistryFriendlyByteBuf, GunSmithTableIngredient> INGREDIENT_STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, GunSmithTableIngredient::getIngredient,
            ByteBufCodecs.VAR_INT, GunSmithTableIngredient::getCount,
            GunSmithTableIngredient::new);

    private static final StreamCodec<RegistryFriendlyByteBuf, GunSmithTableRecipe> STREAM_CODEC = StreamCodec.of(
            (buffer, recipe) -> {
                // results that depend on gun pack data are resolved lazily, make sure it happened before syncing
                recipe.init();
                buffer.writeVarInt(recipe.getInputs().size());
                for (GunSmithTableIngredient ingredient : recipe.getInputs()) {
                    INGREDIENT_STREAM_CODEC.encode(buffer, ingredient);
                }
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.getResult().getResult());
                buffer.writeIdentifier(recipe.getResult().getGroup());
            },
            buffer -> {
                int size = buffer.readVarInt();
                List<GunSmithTableIngredient> ingredients = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    ingredients.add(INGREDIENT_STREAM_CODEC.decode(buffer));
                }
                ItemStack resultItem = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
                Identifier group = buffer.readIdentifier();
                return new GunSmithTableRecipe(UNKNOWN_ID, new GunSmithTableResult(resultItem, group), ingredients);
            });

    private GunSmithTableSerializer() {
    }

    public static RecipeSerializer<GunSmithTableRecipe> create() {
        //? if >=26.1 {
        /*return new RecipeSerializer<>(CODEC, STREAM_CODEC);
        *///?} else {
        return new RecipeSerializer<>() {
            @Override
            public MapCodec<GunSmithTableRecipe> codec() {
                return CODEC;
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, GunSmithTableRecipe> streamCodec() {
                return STREAM_CODEC;
            }
        };
        //?}
    }
}
