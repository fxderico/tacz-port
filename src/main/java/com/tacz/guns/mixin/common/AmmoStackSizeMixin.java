package com.tacz.guns.mixin.common;

import com.tacz.guns.item.AmmoItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Forge's IForgeItem#getMaxStackSize(ItemStack): ammo stack size comes from the gun pack ammo index.
 */
@Mixin(ItemStack.class)
public abstract class AmmoStackSizeMixin {
    //? if >=26.1 {
    /*// ItemStack inherits getMaxStackSize from ItemInstance since 26.1, there is nothing to inject into, override it
    public int getMaxStackSize() {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getItem() instanceof AmmoItem ammoItem) {
            return ammoItem.getMaxStackSize(self);
        }
        return self.getOrDefault(net.minecraft.core.component.DataComponents.MAX_STACK_SIZE, 1);
    }
    *///?} else {
    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void tacz$ammoStackSize(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getItem() instanceof AmmoItem ammoItem) {
            cir.setReturnValue(ammoItem.getMaxStackSize(self));
        }
    }
    //?}
}
