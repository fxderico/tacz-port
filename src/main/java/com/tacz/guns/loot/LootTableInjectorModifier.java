package com.tacz.guns.loot;

import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.pojo.data.loot.LootTableInjection;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

/**
 * Adds gun pack "tacz_loot_injectors" drops to the targeted loot tables.
 * Fabric: the drop modification event. NeoForge: a global loot modifier, like the Forge version
 * (data/neoforge/loot_modifiers/global_loot_modifiers.json, registered as tacz:loot_table_injector).
 */
public final class LootTableInjectorModifier {
    private LootTableInjectorModifier() {
    }

    public static void register() {
        //? if fabric {
        net.fabricmc.fabric.api.loot.v3.LootTableEvents.MODIFY_DROPS.register((table, context, drops) ->
                apply(table.unwrapKey().map(key -> key.identifier()).orElse(null), context, drops));
        //?}
    }

    static void apply(Identifier lootTableId, LootContext context, List<ItemStack> drops) {
        CommonAssetsManager manager = CommonAssetsManager.getInstance();
        if (manager == null || lootTableId == null) {
            return;
        }
        List<LootTableInjection> injections = manager.getLootTableInjections(lootTableId);
        for (LootTableInjection injection : injections) {
            drops.addAll(injection.createStacks(context));
        }
    }

    //? if neoforge {
    /*public static final class Glm extends net.neoforged.neoforge.common.loot.LootModifier {
        public static final com.mojang.serialization.MapCodec<Glm> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(
                instance -> codecStart(instance).apply(instance, Glm::new));

        Glm(net.minecraft.world.level.storage.loot.predicates.LootItemCondition[] conditions) {
            super(conditions);
        }

        @Override
        protected it.unimi.dsi.fastutil.objects.ObjectArrayList<ItemStack> doApply(it.unimi.dsi.fastutil.objects.ObjectArrayList<ItemStack> loot, LootContext context) {
            LootTableInjectorModifier.apply(context.getQueriedLootTableId(), context, loot);
            return loot;
        }

        @Override
        public com.mojang.serialization.MapCodec<? extends net.neoforged.neoforge.common.loot.IGlobalLootModifier> codec() {
            return CODEC;
        }
    }
    *///?}
}
