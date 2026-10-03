package com.tacz.guns.crafting;

import com.tacz.guns.init.ModRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Gun smith table recipe lookups for both sides. Since 1.21.2 the client only knows recipes that are
 * explicitly synced: Fabric's recipe synchronization on Fabric (TACZ's own packet before 1.21.9),
 * OnDatapackSyncEvent#sendRecipes on NeoForge.
 */
public final class TaczRecipes {
    //? if >=1.21.2 {
    /**
     * Recipes the server sent, NeoForge hands them over in RecipesReceivedEvent, Fabric before 1.21.9 in ServerMessageSyncRecipes.
     */
    public static net.minecraft.world.item.crafting.RecipeMap clientRecipes = net.minecraft.world.item.crafting.RecipeMap.EMPTY;
    //?}

    private TaczRecipes() {
    }

    //? if >=1.21.2 {
    private static Collection<RecipeHolder<GunSmithTableRecipe>> syncedRecipes(Level level) {
        //? if neoforge || <1.21.9 {
        /*return clientRecipes.byType(ModRecipe.GUN_SMITH_TABLE_CRAFTING.get());
        *///?} else {
        return ((net.fabricmc.fabric.api.recipe.v1.FabricRecipeManager) level.recipeAccess()).getSynchronizedRecipes().getAllOfType(ModRecipe.GUN_SMITH_TABLE_CRAFTING.get());
        //?}
    }

    private static RecipeHolder<?> syncedRecipe(Level level, ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> key) {
        //? if neoforge || <1.21.9 {
        /*return clientRecipes.byKey(key);
        *///?} else {
        return ((net.fabricmc.fabric.api.recipe.v1.FabricRecipeManager) level.recipeAccess()).getSynchronizedRecipes().get(key);
        //?}
    }
    //?}

    @SuppressWarnings("unchecked")
    public static List<GunSmithTableRecipe> getAll(Level level) {
        //? if <1.21.2 {
        /*// before 1.21.2 every recipe reached the client, both sides ask their own recipe manager
        List<GunSmithTableRecipe> all = new ArrayList<>();
        for (RecipeHolder<GunSmithTableRecipe> holder : level.getRecipeManager().getAllRecipesFor(ModRecipe.GUN_SMITH_TABLE_CRAFTING.get())) {
            holder.value().setId(holder.id());
            all.add(holder.value());
        }
        return all;
        *///?} else {
        Collection<RecipeHolder<GunSmithTableRecipe>> holders;
        if (level instanceof ServerLevel serverLevel) {
            holders = new ArrayList<>();
            for (RecipeHolder<?> holder : serverLevel.recipeAccess().getRecipes()) {
                if (holder.value() instanceof GunSmithTableRecipe) {
                    holders.add((RecipeHolder<GunSmithTableRecipe>) holder);
                }
            }
        } else {
            holders = syncedRecipes(level);
        }
        List<GunSmithTableRecipe> recipes = new ArrayList<>(holders.size());
        for (RecipeHolder<GunSmithTableRecipe> holder : holders) {
            GunSmithTableRecipe recipe = holder.value();
            recipe.setId(holder.id().identifier());
            recipes.add(recipe);
        }
        return recipes;
        //?}
    }

    public static Optional<GunSmithTableRecipe> byId(Level level, Identifier id) {
        //? if <1.21.2 {
        /*RecipeHolder<?> holder = level.getRecipeManager().byKey(id).orElse(null);
        *///?} else {
        ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> key = ResourceKey.create(Registries.RECIPE, id);
        RecipeHolder<?> holder;
        if (level instanceof ServerLevel serverLevel) {
            holder = serverLevel.recipeAccess().byKey(key).orElse(null);
        } else {
            holder = syncedRecipe(level, key);
        }
        //?}
        if (holder != null && holder.value() instanceof GunSmithTableRecipe recipe) {
            recipe.setId(id);
            return Optional.of(recipe);
        }
        return Optional.empty();
    }
}
