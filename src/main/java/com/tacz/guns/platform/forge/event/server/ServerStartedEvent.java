package com.tacz.guns.platform.forge.event.server;

import com.tacz.guns.platform.forge.eventbus.api.Event;
import net.minecraft.server.MinecraftServer;

public class ServerStartedEvent extends Event {
    private final MinecraftServer server;

    public ServerStartedEvent(MinecraftServer server) {
        this.server = server;
    }

    public MinecraftServer getServer() {
        return server;
    }
}
