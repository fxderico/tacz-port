package com.tacz.guns.api.item.nbt;

import com.tacz.guns.util.ItemNbt;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.item.IBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public interface BlockItemDataAccessor extends IBlock {
    String BLOCK_ID = "BlockId";

    @Override
    @Nonnull
    default Identifier getBlockId(ItemStack block) {
        CompoundTag nbt = ItemNbt.view(block);
        if (ItemNbt.contains(nbt, BLOCK_ID, Tag.TAG_STRING)) {
            Identifier gunId = Identifier.tryParse(nbt.getStringOr(BLOCK_ID, ""));
            return Objects.requireNonNullElse(gunId, DefaultAssets.EMPTY_BLOCK_ID);
        }
        return DefaultAssets.EMPTY_BLOCK_ID;
    }

    @Override
    default void setBlockId(ItemStack block, @Nullable Identifier blockId) {
        CompoundTag nbt = ItemNbt.copy(block);
        try {
            if (blockId != null) {
                nbt.putString(BLOCK_ID, blockId.toString());
                return;
            }
            nbt.putString(BLOCK_ID, DefaultAssets.EMPTY_BLOCK_ID.toString());
        } finally {
            ItemNbt.set(block, nbt);
        }
    }

}
