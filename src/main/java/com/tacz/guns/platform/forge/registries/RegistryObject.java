package com.tacz.guns.platform.forge.registries;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.Objects;
import java.util.function.Supplier;

public final class RegistryObject<T> implements Supplier<T> {
    private final ResourceKey<T> key;
    private T value;
    private Holder<T> holder;

    @SuppressWarnings("unchecked")
    RegistryObject(ResourceKey<? extends Registry<? super T>> registry, Identifier id) {
        this.key = (ResourceKey<T>) ResourceKey.create((ResourceKey) registry, id);
    }

    void bind(T value, Holder<T> holder) {
        this.value = value;
        this.holder = holder;
    }

    @Override
    public T get() {
        return Objects.requireNonNull(value, () -> "Registry object not present: " + key);
    }

    public boolean isPresent() {
        return value != null;
    }

    public Identifier getId() {
        return key.identifier();
    }

    public ResourceKey<T> getKey() {
        return key;
    }

    public Holder<T> getHolder() {
        return Objects.requireNonNull(holder, () -> "Registry object not present: " + key);
    }
}
