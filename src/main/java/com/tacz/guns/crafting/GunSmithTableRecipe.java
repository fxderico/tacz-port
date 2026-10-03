package com.tacz.guns.crafting;

import com.tacz.guns.crafting.result.GunSmithTableResult;
import com.tacz.guns.init.ModRecipe;
import com.tacz.guns.resource.pojo.data.recipe.TableRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Gun smith table recipes are never matched against a crafting grid, the menu checks the player inventory itself.
 * Recipes don't carry their id since 1.21.2, {@link TaczRecipes} fills it in from the holder.
 */
public class GunSmithTableRecipe implements Recipe<RecipeInput> {
    private Identifier id;
    private final GunSmithTableResult result;
    private final List<GunSmithTableIngredient> inputs;

    public GunSmithTableRecipe(Identifier id, GunSmithTableResult result, List<GunSmithTableIngredient> inputs) {
        this.id = id;
        this.result = result;
        this.inputs = inputs;
    }

    public GunSmithTableRecipe(Identifier id, TableRecipe tableRecipe) {
        this(id, tableRecipe.getResult(), tableRecipe.getMaterials());
    }

    @Override
    @Deprecated
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    @Override
    @Deprecated
    //? if >=26.1 {
    /*public ItemStack assemble(RecipeInput input) {
    *///?} else {
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
    //?}
        return ItemStack.EMPTY;
    }

    public String group() {
        return "";
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    public Identifier getId() {
        return this.id;
    }

    public void setId(Identifier id) {
        this.id = id;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<GunSmithTableRecipe> getSerializer() {
        return (RecipeSerializer<GunSmithTableRecipe>) ModRecipe.GUN_SMITH_TABLE_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<GunSmithTableRecipe> getType() {
        return ModRecipe.GUN_SMITH_TABLE_CRAFTING.get();
    }

    //? if >=1.21.2 {
    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
    //?} else {
    /*// the recipe book API before 1.21.2
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return getOutput();
    }
    *///?}

    public ItemStack getOutput() {
        return result.getResult();
    }

    public List<GunSmithTableIngredient> getInputs() {
        return inputs;
    }

    public GunSmithTableResult getResult() {
        return result;
    }

    public void init() {
        result.init();
    }

    public Identifier getTab() {
        return result.getGroup();
    }
}
