package com.tacz.guns.platform.compat;

//? if <1.21.6 {
/*import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

/^*
 * The bits of 1.21.6's ValueOutput TACZ uses, writing into the CompoundTag older versions hand out
 * (the import is rewritten to this class below 1.21.6).
 *^/
public final class ValueOutput {
    private final CompoundTag tag;
    private final DynamicOps<Tag> ops;

    public ValueOutput(CompoundTag tag, @Nullable HolderLookup.Provider registries) {
        this.tag = tag;
        this.ops = registries == null ? NbtOps.INSTANCE : registries.createSerializationContext(NbtOps.INSTANCE);
    }

    public <T> void store(String key, Codec<T> codec, T value) {
        codec.encodeStart(this.ops, value).result().ifPresent(encoded -> this.tag.put(key, encoded));
    }

    public <T> void storeNullable(String key, Codec<T> codec, @Nullable T value) {
        if (value != null) {
            this.store(key, codec, value);
        }
    }

    public void putString(String key, String value) {
        this.tag.putString(key, value);
    }
}
*///?}
