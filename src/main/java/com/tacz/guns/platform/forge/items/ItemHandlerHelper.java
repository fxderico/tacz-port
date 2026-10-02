package com.tacz.guns.platform.forge.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ItemHandlerHelper {
    public static void giveItemToPlayer(Player player, ItemStack stack) {
        //? if >=26.3 {
        /*player.getInventory().placeItemBackInInventory(stack, net.minecraft.util.Prediction.SERVER_ONLY);
        *///?} else {
        player.getInventory().placeItemBackInInventory(stack);
        //?}
    }

    public static boolean canItemStacksStack(ItemStack a, ItemStack b) {
        return !a.isEmpty() && ItemStack.isSameItemSameComponents(a, b) && a.isStackable();
    }
}
