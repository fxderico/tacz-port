package com.tacz.guns.platform.forge.network.simple;

import com.tacz.guns.platform.forge.network.NetworkDirection;
import com.tacz.guns.platform.forge.network.NetworkEvent;
import com.tacz.guns.platform.forge.network.PacketDistributor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Forge SimpleChannel look-alike. Every message class becomes its own payload type,
 * {@code <channel>_<index>}, wrapping the original encode/decode/handle functions.
 * Fabric registers them right away, NeoForge in RegisterPayloadHandlersEvent ({@link #registerPayloads}).
 */
public final class SimpleChannel {
    private final Identifier name;
    private final Map<Class<?>, Entry<?>> byClass = new HashMap<>();
    private final List<Entry<?>> clientBound = new ArrayList<>();
    private final List<Entry<?>> serverBound = new ArrayList<>();

    public SimpleChannel(Identifier name) {
        this.name = name;
    }

    public record Payload<M>(Entry<M> entry, M message) implements CustomPacketPayload {
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return entry.type;
        }
    }

    public static final class Entry<M> {
        public final CustomPacketPayload.Type<Payload<M>> type;
        public final StreamCodec<RegistryFriendlyByteBuf, Payload<M>> codec;
        public final BiConsumer<M, Supplier<NetworkEvent.Context>> handler;

        Entry(Identifier id, BiConsumer<M, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, M> decoder, BiConsumer<M, Supplier<NetworkEvent.Context>> handler) {
            this.type = new CustomPacketPayload.Type<>(id);
            this.handler = handler;
            this.codec = StreamCodec.of((buf, payload) -> encoder.accept(payload.message(), buf), buf -> new Payload<>(this, decoder.apply(buf)));
        }

        public void handle(Payload<M> payload, NetworkEvent.Context context) {
            handler.accept(payload.message(), () -> context);
        }
    }

    public <M> void registerMessage(int index, Class<M> messageType, BiConsumer<M, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, M> decoder,
                                    BiConsumer<M, Supplier<NetworkEvent.Context>> handler, Optional<NetworkDirection> direction) {
        Identifier id = Identifier.fromNamespaceAndPath(name.getNamespace(), name.getPath() + "_" + index);
        Entry<M> entry = new Entry<>(id, encoder, decoder, handler);
        byClass.put(messageType, entry);
        NetworkDirection dir = direction.orElseThrow(() -> new IllegalArgumentException("Messages need a direction: " + messageType));
        if (dir == NetworkDirection.PLAY_TO_SERVER) {
            serverBound.add(entry);
            //? if fabric {
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(entry.type, entry.codec);
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(entry.type, (payload, ctx) ->
                    entry.handle(payload, new NetworkEvent.Context(NetworkDirection.PLAY_TO_SERVER, ctx.player())));
            //?}
        } else {
            clientBound.add(entry);
            //? if fabric {
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(entry.type, entry.codec);
            //?}
        }
    }

    //? if neoforge {
    /*public void registerPayloads(net.neoforged.neoforge.network.registration.PayloadRegistrar registrar) {
        for (Entry<?> entry : serverBound) {
            registerServerBound(registrar, entry);
        }
        for (Entry<?> entry : clientBound) {
            registerClientBound(registrar, entry);
        }
    }

    private static <M> void registerServerBound(net.neoforged.neoforge.network.registration.PayloadRegistrar registrar, Entry<M> entry) {
        registrar.playToServer(entry.type, entry.codec, (payload, ctx) -> entry.handle(payload,
                new NetworkEvent.Context(NetworkDirection.PLAY_TO_SERVER, (net.minecraft.server.level.ServerPlayer) ctx.player())));
    }

    private static <M> void registerClientBound(net.neoforged.neoforge.network.registration.PayloadRegistrar registrar, Entry<M> entry) {
        registrar.playToClient(entry.type, entry.codec, (payload, ctx) -> entry.handle(payload,
                new NetworkEvent.Context(NetworkDirection.PLAY_TO_CLIENT, null)));
    }
    *///?}

    //? if forge {
    /*private net.minecraftforge.network.Channel<CustomPacketPayload> forgeChannel;

    /^*
     * Builds Forge's payload channel once every message is registered. Forge calls the handlers on the network
     * thread, they are moved to the main thread like Fabric and NeoForge do.
     ^/
    public void buildForgeChannel() {
        net.minecraftforge.network.payload.PayloadFlow<net.minecraft.network.RegistryFriendlyByteBuf, CustomPacketPayload> flow =
                net.minecraftforge.network.ChannelBuilder.named(name).networkProtocolVersion(1).payloadChannel().play().serverbound();
        for (Entry<?> entry : serverBound) {
            addForge(flow, entry, NetworkDirection.PLAY_TO_SERVER);
        }
        flow = flow.clientbound();
        for (Entry<?> entry : clientBound) {
            addForge(flow, entry, NetworkDirection.PLAY_TO_CLIENT);
        }
        forgeChannel = flow.build();
    }

    private static <M> void addForge(net.minecraftforge.network.payload.PayloadFlow<net.minecraft.network.RegistryFriendlyByteBuf, CustomPacketPayload> flow,
                                     Entry<M> entry, NetworkDirection direction) {
        flow.add(entry.type, entry.codec, (payload, ctx) -> {
            net.minecraft.server.level.ServerPlayer sender = direction == NetworkDirection.PLAY_TO_SERVER ? ctx.getSender() : null;
            ctx.enqueueWork(() -> entry.handle(payload, new NetworkEvent.Context(direction, sender)));
            ctx.setPacketHandled(true);
        });
    }

    public void forgeSendToServer(Payload<?> payload) {
        forgeChannel.send(payload, net.minecraftforge.network.PacketDistributor.SERVER.noArg());
    }
    *///?}

    /**
     * Client bound entries, the client entrypoint registers their receivers.
     */
    public List<Entry<?>> getClientBound() {
        return clientBound;
    }

    @SuppressWarnings("unchecked")
    public <M> Payload<M> wrap(M message) {
        Entry<M> entry = (Entry<M>) byClass.get(message.getClass());
        if (entry == null) {
            throw new IllegalArgumentException("Unregistered message " + message.getClass());
        }
        return new Payload<>(entry, message);
    }

    /**
     * The packet carrying a message to clients, also usable inside bundles.
     */
    public <M> net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.common.ClientCommonPacketListener> toClientPacket(M message) {
        //? if neoforge {
        /*return new net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket(wrap(message));
        *///?} else if forge {
        /*return net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT.buildPacket(forgeChannel, wrap(message));
        *///?} else {
        return net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.createS2CPacket(wrap(message));
        //?}
    }

    public <M> void send(PacketDistributor.PacketTarget target, M message) {
        target.send(toClientPacket(message));
    }

    public <M> void sendToServer(M message) {
        SimpleChannelClient.sendToServer(wrap(message));
    }

    public <M> void reply(M message, NetworkEvent.Context context) {
        if (context.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            context.getSender().connection.send(toClientPacket(message));
        } else {
            sendToServer(message);
        }
    }
}
