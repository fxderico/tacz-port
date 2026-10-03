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
        //? if fabric && >=1.21.6 {
        net.fabricmc.fabric.api.loot.v3.LootTableEvents.MODIFY_DROPS.register((table, context, drops) ->
                apply(table.unwrapKey().map(key -> key.identifier()).orElse(null), context, drops));
        //?} else if fabric {
        /*// no drop event yet: every table gets a pool whose entry asks the gun packs at roll time
        net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.LOOT_POOL_ENTRY_TYPE,
                Identifier.fromNamespaceAndPath("tacz", "loot_table_injection"), InjectionEntry.TYPE);
        net.fabricmc.fabric.api.loot.v3.LootTableEvents.MODIFY.register((key, builder, source, registries) ->
                builder.withPool(net.minecraft.world.level.storage.loot.LootPool.lootPool().add(InjectionEntry.entry(key.identifier()))));
        *///?}
    }

    //? if fabric && <1.21.6 {
    /*static final class InjectionEntry extends net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer {
        static final com.mojang.serialization.MapCodec<InjectionEntry> CODEC = Identifier.CODEC.fieldOf("table").xmap(InjectionEntry::new, entry -> entry.table);
        static final net.minecraft.world.level.storage.loot.entries.LootPoolEntryType TYPE = new net.minecraft.world.level.storage.loot.entries.LootPoolEntryType(CODEC);

        private final Identifier table;

        private InjectionEntry(Identifier table) {
            super(DEFAULT_WEIGHT, DEFAULT_QUALITY, List.of(), List.of());
            this.table = table;
        }

        static Builder<?> entry(Identifier table) {
            return simpleBuilder((weight, quality, conditions, functions) -> new InjectionEntry(table));
        }

        @Override
        public net.minecraft.world.level.storage.loot.entries.LootPoolEntryType getType() {
            return TYPE;
        }

        @Override
        protected void createItemStack(java.util.function.Consumer<ItemStack> output, LootContext context) {
            List<ItemStack> drops = new java.util.ArrayList<>();
            apply(table, context, drops);
            drops.forEach(output);
        }
    }
    *///?}

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

        //? if >=26.3 {
        /^// one optional condition since 26.3
        Glm(java.util.Optional<net.minecraft.core.Holder<net.minecraft.world.level.storage.loot.predicates.LootItemCondition>> condition, int priority) {
            super(condition, priority);
        }
        ^///?} else if >=26.1 {
        /^Glm(net.minecraft.world.level.storage.loot.predicates.LootItemCondition[] conditions, int priority) {
            super(conditions, priority);
        }
        ^///?} else {
        Glm(net.minecraft.world.level.storage.loot.predicates.LootItemCondition[] conditions) {
            super(conditions);
        }
        //?}

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
