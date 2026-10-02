package com.tacz.guns.platform.forge.event;

import com.tacz.guns.platform.forge.eventbus.api.Event;
import com.tacz.guns.platform.forge.eventbus.api.Cancelable;
import com.tacz.guns.platform.forge.fml.LogicalSide;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

public class TickEvent extends Event {
    public enum Type {
        LEVEL, PLAYER, CLIENT, SERVER, RENDER
    }

    public enum Phase {
        START, END
    }

    public final Type type;
    public final LogicalSide side;
    public final Phase phase;

    public TickEvent(Type type, LogicalSide side, Phase phase) {
        this.type = type;
        this.side = side;
        this.phase = phase;
    }

    public static class ClientTickEvent extends TickEvent {
        public ClientTickEvent(Phase phase) {
            super(Type.CLIENT, LogicalSide.CLIENT, phase);
        }
    }

    public static class ServerTickEvent extends TickEvent {
        public final MinecraftServer server;

        public ServerTickEvent(Phase phase, MinecraftServer server) {
            super(Type.SERVER, LogicalSide.SERVER, phase);
            this.server = server;
        }

        public MinecraftServer getServer() {
            return server;
        }
    }

    public static class PlayerTickEvent extends TickEvent {
        public final Player player;

        public PlayerTickEvent(Phase phase, Player player) {
            super(Type.PLAYER, player.level().isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER, phase);
            this.player = player;
        }
    }

    public static class RenderTickEvent extends TickEvent {
        public final float renderTickTime;

        public RenderTickEvent(Phase phase, float renderTickTime) {
            super(Type.RENDER, LogicalSide.CLIENT, phase);
            this.renderTickTime = renderTickTime;
        }
    }
}
