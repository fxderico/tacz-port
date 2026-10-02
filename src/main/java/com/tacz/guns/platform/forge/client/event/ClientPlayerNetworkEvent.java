package com.tacz.guns.platform.forge.client.event;

import com.tacz.guns.platform.forge.eventbus.api.Event;
import com.tacz.guns.platform.forge.eventbus.api.Cancelable;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.Nullable;

public abstract class ClientPlayerNetworkEvent extends Event {
    private final MultiPlayerGameMode multiPlayerGameMode;
    private final LocalPlayer player;
    private final Connection connection;

    protected ClientPlayerNetworkEvent(@Nullable MultiPlayerGameMode multiPlayerGameMode, @Nullable LocalPlayer player, @Nullable Connection connection) {
        this.multiPlayerGameMode = multiPlayerGameMode;
        this.player = player;
        this.connection = connection;
    }

    @Nullable
    public MultiPlayerGameMode getMultiPlayerGameMode() {
        return multiPlayerGameMode;
    }

    @Nullable
    public LocalPlayer getPlayer() {
        return player;
    }

    @Nullable
    public Connection getConnection() {
        return connection;
    }

    public static class LoggingIn extends ClientPlayerNetworkEvent {
        public LoggingIn(MultiPlayerGameMode gameMode, LocalPlayer player, Connection connection) {
            super(gameMode, player, connection);
        }
    }

    public static class LoggingOut extends ClientPlayerNetworkEvent {
        public LoggingOut(@Nullable MultiPlayerGameMode gameMode, @Nullable LocalPlayer player, @Nullable Connection connection) {
            super(gameMode, player, connection);
        }
    }

    public static class Clone extends ClientPlayerNetworkEvent {
        private final LocalPlayer oldPlayer;

        public Clone(MultiPlayerGameMode gameMode, LocalPlayer oldPlayer, LocalPlayer newPlayer, Connection connection) {
            super(gameMode, newPlayer, connection);
            this.oldPlayer = oldPlayer;
        }

        public LocalPlayer getOldPlayer() {
            return oldPlayer;
        }

        public LocalPlayer getNewPlayer() {
            return getPlayer();
        }
    }
}
