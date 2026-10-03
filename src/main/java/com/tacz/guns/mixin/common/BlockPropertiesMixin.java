package com.tacz.guns.mixin.common;

import com.tacz.guns.platform.forge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Same as ItemPropertiesMixin, for blocks.
 */
@Mixin(BlockBehaviour.Properties.class)
public abstract class BlockPropertiesMixin {
    // ids in the properties came with 1.21.2
    //? if >=1.21.2 {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void tacz$setRegistryId(CallbackInfo ci) {
        ResourceKey<Block> key = DeferredRegister.currentKey(Registries.BLOCK);
        if (key != null) {
            ((BlockBehaviour.Properties) (Object) this).setId(key);
        }
    }
    //?}
}
