package com.tacz.guns.api.util;

import com.tacz.guns.util.ItemNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * 一个简单的NBT包装，用于在Lua中访问NBT数据。<br/>
 * 暂时只支持基本数据类型的读写，不支持数组等复杂数据类型。
 * <p>
 * Items have no live NBT tag anymore, so an accessor made from a stack keeps a copy of its custom data
 * and writes the whole thing back after every put.
 */
@SuppressWarnings("unused")
public final class LuaNbtAccessor {
    private final CompoundTag nbt;
    private final @Nullable ItemStack owner;
    private final CompoundTag root;

    public LuaNbtAccessor(CompoundTag nbt) {
        this(nbt, null, nbt);
    }

    private LuaNbtAccessor(CompoundTag nbt, @Nullable ItemStack owner, CompoundTag root) {
        this.nbt = nbt;
        this.owner = owner;
        this.root = root;
    }

    public static LuaNbtAccessor from(ItemStack stack) {
        CompoundTag copy = ItemNbt.copy(stack);
        return new LuaNbtAccessor(copy, stack, copy);
    }

    public static LuaNbtAccessor from(CompoundTag nbt) {
        return new LuaNbtAccessor(nbt);
    }

    private void flush() {
        if (owner != null) {
            ItemNbt.set(owner, root.copy());
        }
    }

    public boolean contains(String key) {
        return nbt.contains(key);
    }

    public boolean contains(String key, int type) {
        return ItemNbt.contains(nbt, key, type);
    }

    public LuaNbtAccessor newCompoundTag() {
        return new LuaNbtAccessor(new CompoundTag());
    }

    public int getInt(String key) {
        return nbt.getIntOr(key, 0);
    }

    public double getDouble(String key) {
        return nbt.getDoubleOr(key, 0d);
    }

    public float getFloat(String key) {
        return nbt.getFloatOr(key, 0f);
    }

    public long getLong(String key) {
        return nbt.getLongOr(key, 0L);
    }

    public String getString(String key) {
        return nbt.getStringOr(key, "");
    }

    public boolean getBoolean(CompoundTag nbt, String key) {
        return nbt.getBooleanOr(key, false);
    }

    public LuaNbtAccessor getCompound(String key) {
        if (!ItemNbt.contains(nbt, key, Tag.TAG_COMPOUND)) {
            return null;
        }
        return new LuaNbtAccessor(nbt.getCompoundOrEmpty(key), owner, root);
    }

    public void putInt(String key, int value) {
        nbt.putInt(key, value);
        flush();
    }

    public void putDouble(String key, double value) {
        nbt.putDouble(key, value);
        flush();
    }

    public void putFloat(String key, float value) {
        nbt.putFloat(key, value);
        flush();
    }

    public void putLong(String key, long value) {
        nbt.putLong(key, value);
        flush();
    }

    public void putString(String key, String value) {
        nbt.putString(key, value);
        flush();
    }

    public void putBoolean(String key, boolean value) {
        nbt.putBoolean(key, value);
        flush();
    }

    /**
     * 向当前的NbtCompound中添加一个新的Compound
     *
     * @param key   键
     * @param value 在脚本中请使用{@link LuaNbtAccessor#newCompoundTag()}创建一个新的LuaNbtAccessor对象
     */
    public void putCompound(String key, LuaNbtAccessor value) {
        if (value != null) {
            nbt.put(key, value.nbt());
            flush();
        }
    }

    @ApiStatus.Internal
    public CompoundTag nbt() {
        return nbt;
    }
}
