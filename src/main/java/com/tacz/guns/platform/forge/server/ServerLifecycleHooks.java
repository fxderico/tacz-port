package com.tacz.guns.platform.forge.server;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public final class ServerLifecycleHooks {
    private static MinecraftServer currentServer;

    @Nullable
    public static MinecraftServer getCurrentServer() {
        return currentServer;
    }

    public static void setCurrentServer(@Nullable MinecraftServer server) {
        currentServer = server;
    }
}
