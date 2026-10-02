package com.tacz.guns.item;

import com.tacz.guns.util.ItemNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public enum GunTooltipPart {
    DESCRIPTION,
    AMMO_INFO,
    BASE_INFO,
    EXTRA_DAMAGE_INFO,
    UPGRADES_TIP,
    PACK_INFO;

    private final int mask = 1 << this.ordinal();

    public int getMask() {
        return this.mask;
    }

    public static int getHideFlags(ItemStack stack) {
        CompoundTag tag = ItemNbt.view(stack);
        if (tag != null && ItemNbt.contains(tag, "HideFlags", ItemNbt.ANY_NUMERIC)) {
            return tag.getIntOr("HideFlags", 0);
        }
        return 0;
    }

    public static void setHideFlags(ItemStack stack, int mask) {
        ItemNbt.update(stack, tag -> tag.putInt("HideFlags", mask));
    }
}
