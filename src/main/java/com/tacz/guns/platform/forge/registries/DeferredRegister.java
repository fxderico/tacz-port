package com.tacz.guns.platform.forge.registries;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Forge style deferred register on top of vanilla registries. Call {@link #register()} once during mod init.
 * While an entry is being constructed {@link #currentKey()} exposes its key, the Item/Block properties mixins
 * use it to fill in the id that 1.21.2+ requires.
 */
public final class DeferredRegister<T> {
    private static final ThreadLocal<ResourceKey<?>> CURRENT = new ThreadLocal<>();

    private final ResourceKey<? extends Registry<T>> registryKey;
    private final String modId;
    private final Map<RegistryObject<T>, Supplier<? extends T>> entries = new LinkedHashMap<>();
    private boolean registered;

    private DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String modId) {
        this.registryKey = registryKey;
        this.modId = modId;
    }

    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey, String modId) {
        return new DeferredRegister<>(registryKey, modId);
    }

    public static <T> DeferredRegister<T> create(Registry<T> registry, String modId) {
        return new DeferredRegister<>(registry.key(), modId);
    }

    @SuppressWarnings("unchecked")
    public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> supplier) {
        if (registered) {
            throw new IllegalStateException("Cannot add entries after registration: " + name);
        }
        RegistryObject<I> object = new RegistryObject<>(registryKey, Identifier.fromNamespaceAndPath(modId, name));
        entries.put((RegistryObject<T>) object, supplier);
        return object;
    }

    @SuppressWarnings("unchecked")
    public void register() {
        if (registered) {
            return;
        }
        registered = true;
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.getValueOrThrow((ResourceKey) registryKey);
        for (Map.Entry<RegistryObject<T>, Supplier<? extends T>> entry : entries.entrySet()) {
            RegistryObject<T> object = entry.getKey();
            CURRENT.set(object.getKey());
            T value;
            try {
                value = entry.getValue().get();
            } finally {
                CURRENT.remove();
            }
            Holder<T> holder = Registry.registerForHolder(registry, object.getKey(), value);
            object.bind(value, holder);
        }
    }

    public ResourceKey<? extends Registry<T>> getRegistryKey() {
        return registryKey;
    }

    public Iterable<RegistryObject<T>> getEntries() {
        return entries.keySet();
    }

    @SuppressWarnings("unchecked")
    public static <T> ResourceKey<T> currentKey(ResourceKey<? extends Registry<T>> registry) {
        ResourceKey<?> key = CURRENT.get();
        if (key != null && key.isFor(registry)) {
            return (ResourceKey<T>) key;
        }
        return null;
    }
}
