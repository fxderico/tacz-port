package com.tacz.guns.util;

import com.tacz.guns.platform.forge.server.ServerLifecycleHooks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Registry access for code that used to call ItemStack#save / ItemStack#of without one.
 * Uses the running server, then the client level (set by GunModClient), then the static registries.
 */
public final class TaczRegistries {
    private static Supplier<HolderLookup.Provider> clientRegistries = () -> null;

    private TaczRegistries() {
    }

    public static void setClientRegistries(Supplier<HolderLookup.Provider> supplier) {
        clientRegistries = supplier;
    }

    public static HolderLookup.Provider registries() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.registryAccess();
        }
        HolderLookup.Provider client = clientRegistries.get();
        if (client != null) {
            return client;
        }
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    public static RegistryOps<Tag> nbtOps() {
        return registries().createSerializationContext(NbtOps.INSTANCE);
    }

    public static CompoundTag saveStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return new CompoundTag();
        }
        Tag tag = ItemStack.CODEC.encodeStart(nbtOps(), stack).result().orElse(null);
        return tag instanceof CompoundTag compound ? compound : new CompoundTag();
    }

    public static ItemStack loadStack(@Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return ItemStack.CODEC.parse(nbtOps(), tag).result().orElse(ItemStack.EMPTY);
    }
}
