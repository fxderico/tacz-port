package com.tacz.guns.crafting;

import com.tacz.guns.init.ModRecipe;
import net.fabricmc.fabric.api.recipe.v1.FabricRecipeManager;
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
 * explicitly synced, TACZ's serializer is registered with Fabric's recipe synchronization for that.
 */
public final class TaczRecipes {
    private TaczRecipes() {
    }

    @SuppressWarnings("unchecked")
    public static List<GunSmithTableRecipe> getAll(Level level) {
        Collection<RecipeHolder<GunSmithTableRecipe>> holders;
        if (level instanceof ServerLevel serverLevel) {
            holders = new ArrayList<>();
            for (RecipeHolder<?> holder : serverLevel.recipeAccess().getRecipes()) {
                if (holder.value() instanceof GunSmithTableRecipe) {
                    holders.add((RecipeHolder<GunSmithTableRecipe>) holder);
                }
            }
        } else {
            holders = ((FabricRecipeManager) level.recipeAccess()).getSynchronizedRecipes().getAllOfType(ModRecipe.GUN_SMITH_TABLE_CRAFTING.get());
        }
        List<GunSmithTableRecipe> recipes = new ArrayList<>(holders.size());
        for (RecipeHolder<GunSmithTableRecipe> holder : holders) {
            GunSmithTableRecipe recipe = holder.value();
            recipe.setId(holder.id().identifier());
            recipes.add(recipe);
        }
        return recipes;
    }

    public static Optional<GunSmithTableRecipe> byId(Level level, Identifier id) {
        ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> key = ResourceKey.create(Registries.RECIPE, id);
        RecipeHolder<?> holder;
        if (level instanceof ServerLevel serverLevel) {
            holder = serverLevel.recipeAccess().byKey(key).orElse(null);
        } else {
            holder = ((FabricRecipeManager) level.recipeAccess()).getSynchronizedRecipes().get(key);
        }
        if (holder != null && holder.value() instanceof GunSmithTableRecipe recipe) {
            recipe.setId(id);
            return Optional.of(recipe);
        }
        return Optional.empty();
    }
}
