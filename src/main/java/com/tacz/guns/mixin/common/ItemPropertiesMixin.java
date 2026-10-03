package com.tacz.guns.mixin.common;

import com.tacz.guns.platform.forge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.21.2+ items need their id in the properties before construction. TACZ builds its properties inside the
 * registry suppliers, so the id is taken from the entry DeferredRegister is creating right now.
 */
@Mixin(Item.Properties.class)
public abstract class ItemPropertiesMixin {
    // ids in the properties came with 1.21.2
    //? if >=1.21.2 {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void tacz$setRegistryId(CallbackInfo ci) {
        ResourceKey<Item> key = DeferredRegister.currentKey(Registries.ITEM);
        if (key != null) {
            ((Item.Properties) (Object) this).setId(key);
        }
    }
    //?}
}
