package com.tacz.guns.mixin.common;

import com.tacz.guns.entity.sync.core.DataHolder;
import com.tacz.guns.entity.sync.core.DataHolderAccess;
import com.tacz.guns.entity.sync.core.DataHolderCapabilityProvider;
import com.tacz.guns.entity.sync.core.SyncedEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the Forge "synced_entity_data" capability: every entity can lazily carry a DataHolder,
 * and it is saved with the entity just like the capability was.
 */
@Mixin(Entity.class)
public abstract class EntityDataHolderMixin implements DataHolderAccess {
    @Unique
    private static final String TACZ_DATA_KEY = "tacz_synced_entity_data";

    @Unique
    private DataHolder tacz$dataHolder;

    @Override
    public DataHolder tacz$getDataHolder() {
        if (this.tacz$dataHolder == null) {
            this.tacz$dataHolder = new DataHolder();
        }
        return this.tacz$dataHolder;
    }

    @Inject(method = "saveWithoutId", at = @At("TAIL"))
    private void tacz$saveData(ValueOutput output, CallbackInfo ci) {
        if (this.tacz$dataHolder == null || !SyncedEntityData.instance().hasSyncedDataKey((Entity) (Object) this)) {
            return;
        }
        ListTag list = DataHolderCapabilityProvider.serializeNBT(this.tacz$dataHolder);
        if (!list.isEmpty()) {
            CompoundTag wrapper = new CompoundTag();
            wrapper.put("Data", list);
            output.store(TACZ_DATA_KEY, CompoundTag.CODEC, wrapper);
        }
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void tacz$loadData(ValueInput input, CallbackInfo ci) {
        input.read(TACZ_DATA_KEY, CompoundTag.CODEC).ifPresent(wrapper ->
                DataHolderCapabilityProvider.deserializeNBT(this.tacz$getDataHolder(), wrapper.getListOrEmpty("Data")));
    }
}
