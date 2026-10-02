package com.tacz.guns.platform.forge.items;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

/**
 * Stand-in for entity.getCapability(ForgeCapabilities.ITEM_HANDLER). Forge only exposed one for players by default.
 */
public final class ItemHandlers {
    public static Optional<IItemHandler> of(Entity entity) {
        if (entity instanceof Player player) {
            return Optional.of(new ContainerItemHandler(player.getInventory()));
        }
        return Optional.empty();
    }
}
