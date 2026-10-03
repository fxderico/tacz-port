package com.tacz.guns.platform.compat;

//? if <1.21.6 {
/*import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/^*
 * The bits of 1.21.6's ValueInput TACZ uses, over the CompoundTag older versions hand out
 * (the import is rewritten to this class below 1.21.6).
 *^/
public final class ValueInput {
    private final CompoundTag tag;
    private final DynamicOps<Tag> ops;

    public ValueInput(CompoundTag tag, @Nullable HolderLookup.Provider registries) {
        this.tag = tag;
        this.ops = registries == null ? NbtOps.INSTANCE : registries.createSerializationContext(NbtOps.INSTANCE);
    }

    public <T> Optional<T> read(String key, Codec<T> codec) {
        Tag value = this.tag.get(key);
        return value == null ? Optional.empty() : codec.parse(this.ops, value).result();
    }

    public Optional<String> getString(String key) {
        return this.read(key, Codec.STRING);
    }
}
*///?}
