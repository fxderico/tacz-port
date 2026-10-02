package com.tacz.guns.platform.forge.client.extensions.common;

import com.tacz.guns.platform.client.BlockEntityWithoutLevelRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Forge's per item client hook, only the custom renderer part is used by TACZ.
 * Items opt in through {@link Provider}, GunModClient collects them on startup.
 */
@Environment(EnvType.CLIENT)
public interface IClientItemExtensions {
    IClientItemExtensions DEFAULT = new IClientItemExtensions() {
    };

    @Nullable
    default BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return null;
    }

    static IClientItemExtensions of(ItemStack stack) {
        return of(stack.getItem());
    }

    static IClientItemExtensions of(Item item) {
        return Registry.EXTENSIONS.getOrDefault(item, DEFAULT);
    }

    static void collect(Item item) {
        if (item instanceof Provider provider) {
            provider.initializeClient(ext -> Registry.EXTENSIONS.put(item, ext));
        }
    }

    final class Registry {
        private static final Map<Item, IClientItemExtensions> EXTENSIONS = new IdentityHashMap<>();
    }

    interface Provider {
        default void initializeClient(Consumer<IClientItemExtensions> consumer) {
        }
    }
}
