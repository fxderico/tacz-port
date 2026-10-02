package com.tacz.guns.platform.forge.network;

import com.tacz.guns.platform.forge.server.ServerLifecycleHooks;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class PacketDistributor<T> {
    public static final PacketDistributor<ServerPlayer> PLAYER = new PacketDistributor<>(player -> packet -> player.connection.send(packet));
    public static final PacketDistributor<Entity> TRACKING_ENTITY = new PacketDistributor<>(entity -> packet -> {
        if (entity.level() instanceof ServerLevel level) {
            level.getChunkSource().sendToTrackingPlayers(entity, cast(packet));
        }
    });
    public static final PacketDistributor<Entity> TRACKING_ENTITY_AND_SELF = new PacketDistributor<>(entity -> packet -> {
        if (entity.level() instanceof ServerLevel level) {
            level.getChunkSource().sendToTrackingPlayersAndSelf(entity, cast(packet));
        }
    });
    public static final PacketDistributor<ResourceKey<Level>> DIMENSION = new PacketDistributor<>(dimension -> packet -> {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.getPlayerList().broadcastAll(packet, dimension);
        }
    });
    public static final PacketDistributor<Void> ALL = new PacketDistributor<>(ignored -> packet -> {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.getPlayerList().broadcastAll(packet);
        }
    });

    private final Function<T, Consumer<Packet<?>>> factory;

    @SuppressWarnings("unchecked")
    private static Packet<? super net.minecraft.network.protocol.game.ClientGamePacketListener> cast(Packet<?> packet) {
        return (Packet<? super net.minecraft.network.protocol.game.ClientGamePacketListener>) packet;
    }

    private PacketDistributor(Function<T, Consumer<Packet<?>>> factory) {
        this.factory = factory;
    }

    public PacketTarget with(Supplier<? extends T> input) {
        return new PacketTarget(factory.apply(input.get()));
    }

    public PacketTarget noArg() {
        return new PacketTarget(factory.apply(null));
    }

    public record PacketTarget(Consumer<Packet<?>> sender) {
        public void send(Packet<?> packet) {
            sender.accept(packet);
        }
    }
}
