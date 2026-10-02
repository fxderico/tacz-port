package com.tacz.guns.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import com.tacz.guns.mixin.common.CustomDataAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Items lost their NBT tag in 1.20.5. TACZ keeps its data in the minecraft:custom_data component instead,
 * with the same keys as before, so old gun packs and recipes that put NBT on results still line up.
 */
public final class ItemNbt {
    public static final int ANY_NUMERIC = 99;

    private ItemNbt() {
    }

    /**
     * Read only view, never mutate it (it can be shared between stacks).
     */
    public static CompoundTag view(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : ((CustomDataAccessor) (Object) data).tacz$getTag();
    }

    /**
     * Mutable copy, write it back with {@link #set}.
     */
    public static CompoundTag copy(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    @Nullable
    public static CompoundTag get(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.copyTag();
    }

    public static boolean has(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty();
    }

    public static void set(ItemStack stack, @Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    public static void update(ItemStack stack, Consumer<CompoundTag> editor) {
        CompoundTag tag = copy(stack);
        editor.accept(tag);
        set(stack, tag);
    }

    public static void remove(ItemStack stack, String key) {
        if (view(stack).contains(key)) {
            update(stack, tag -> tag.remove(key));
        }
    }

    /**
     * Old {@code CompoundTag#contains(String, int)}, removed in 1.21.5. 99 matches any number like before.
     */
    public static boolean contains(@Nullable CompoundTag tag, String key, int type) {
        if (tag == null) {
            return false;
        }
        Tag value = tag.get(key);
        if (value == null) {
            return false;
        }
        if (type == ANY_NUMERIC) {
            return value instanceof NumericTag;
        }
        return value.getId() == type;
    }
}
