package com.tacz.guns.entity.sync.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;

/**
 * Used to be a Forge capability provider. On Fabric the holder lives on the entity itself (see EntityMixin),
 * this class only keeps the NBT (de)serialization.
 */
public final class DataHolderCapabilityProvider {
    private DataHolderCapabilityProvider() {
    }

    public static ListTag serializeNBT(DataHolder holder) {
        ListTag list = new ListTag();
        holder.dataMap.forEach((key, entry) -> {
            if (key.save()) {
                CompoundTag keyTag = new CompoundTag();
                keyTag.putString("ClassKey", key.classKey().id().toString());
                keyTag.putString("DataKey", key.id().toString());
                keyTag.put("Value", entry.writeValue());
                list.add(keyTag);
            }
        });
        return list;
    }

    public static void deserializeNBT(DataHolder holder, ListTag listTag) {
        holder.dataMap.clear();
        listTag.forEach(entryTag -> {
            if (!(entryTag instanceof CompoundTag keyTag)) {
                return;
            }
            Identifier classKey = Identifier.tryParse(keyTag.getStringOr("ClassKey", ""));
            Identifier dataKey = Identifier.tryParse(keyTag.getStringOr("DataKey", ""));
            Tag value = keyTag.get("Value");
            if (classKey == null || dataKey == null || value == null) {
                return;
            }
            SyncedClassKey<?> syncedClassKey = SyncedEntityData.instance().getClassKey(classKey);
            if (syncedClassKey == null) {
                return;
            }
            SyncedDataKey<?, ?> syncedDataKey = SyncedEntityData.instance().getKey(syncedClassKey, dataKey);
            if (syncedDataKey == null || !syncedDataKey.save()) {
                return;
            }
            DataEntry<?, ?> entry = new DataEntry<>(syncedDataKey);
            entry.readValue(value);
            holder.dataMap.put(syncedDataKey, entry);
        });
    }
}
