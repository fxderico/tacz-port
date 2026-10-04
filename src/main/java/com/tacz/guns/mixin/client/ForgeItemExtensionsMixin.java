package com.tacz.guns.mixin.client;

import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Forge before 1.21.4: TACZ's items draw through "builtin/entity" models (src/main/legacy-item-models), Forge asks
 * each item for its custom renderer in Item#initializeClient. TACZ's own hook has its own name on Forge
 * (see the initializeClient rename in stonecutter.gradle.kts), so the Forge one is answered here.
 */
@Mixin(Item.class)
public abstract class ForgeItemExtensionsMixin {
    //? if forge && <1.21.4 {
    /*@org.spongepowered.asm.mixin.injection.Inject(method = "initializeClient", at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true, remap = false)
    private void tacz$legacyRenderer(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer,
                                     org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if ((Object) this instanceof com.tacz.guns.platform.forge.client.extensions.common.IClientItemExtensions.Provider) {
            consumer.accept(com.tacz.guns.platform.forgemod.LegacyForgeItemRenderer.EXTENSIONS);
            ci.cancel();
        }
    }
    *///?}
}
