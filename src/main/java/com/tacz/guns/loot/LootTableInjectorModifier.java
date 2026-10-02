package com.tacz.guns.loot;

import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.pojo.data.loot.LootTableInjection;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

/**
 * Adds gun pack "tacz_loot_injectors" drops to the targeted loot tables.
 * On Forge this was a global loot modifier, Fabric's drop modification event does the same job.
 */
public final class LootTableInjectorModifier {
    private LootTableInjectorModifier() {
    }

    public static void register() {
        LootTableEvents.MODIFY_DROPS.register(LootTableInjectorModifier::apply);
    }

    private static void apply(Holder<LootTable> table, LootContext context, List<ItemStack> drops) {
        CommonAssetsManager manager = CommonAssetsManager.getInstance();
        if (manager == null) {
            return;
        }
        Identifier lootTableId = table.unwrapKey().map(key -> key.identifier()).orElse(null);
        if (lootTableId == null) {
            return;
        }
        List<LootTableInjection> injections = manager.getLootTableInjections(lootTableId);
        for (LootTableInjection injection : injections) {
            drops.addAll(injection.createStacks(context));
        }
    }
}
