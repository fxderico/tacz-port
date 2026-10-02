package com.tacz.guns.platform.forge.items;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * IItemHandler view over a vanilla Container, roughly Forge's InvWrapper.
 */
public class ContainerItemHandler implements IItemHandler {
    private final Container container;

    public ContainerItemHandler(Container container) {
        this.container = container;
    }

    @Override
    public int getSlots() {
        return container.getContainerSize();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return container.getItem(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !container.canPlaceItem(slot, stack)) {
            return stack;
        }
        ItemStack existing = container.getItem(slot);
        int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(stack, existing)) {
                return stack;
            }
            limit -= existing.getCount();
        }
        if (limit <= 0) {
            return stack;
        }
        boolean reachedLimit = stack.getCount() > limit;
        if (!simulate) {
            if (existing.isEmpty()) {
                container.setItem(slot, reachedLimit ? stack.copyWithCount(limit) : stack.copy());
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            container.setChanged();
        }
        return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack existing = container.getItem(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int toExtract = Math.min(amount, existing.getMaxStackSize());
        if (simulate) {
            return existing.copyWithCount(Math.min(toExtract, existing.getCount()));
        }
        ItemStack extracted = container.removeItem(slot, toExtract);
        container.setChanged();
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        return container.getMaxStackSize();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return container.canPlaceItem(slot, stack);
    }
}
