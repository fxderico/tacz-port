package com.tacz.guns.platform.forge.network.simple;

import com.tacz.guns.platform.forge.network.NetworkDirection;
import com.tacz.guns.platform.forge.network.NetworkEvent;
import com.tacz.guns.platform.forge.network.PacketDistributor;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
 * Forge SimpleChannel look-alike. Every message class becomes its own Fabric payload type,
 * {@code <channel>_<index>}, wrapping the original encode/decode/handle functions.
 */
public final class SimpleChannel {
    private final Identifier name;
    private final Map<Class<?>, Entry<?>> byClass = new HashMap<>();
    private final List<Entry<?>> clientBound = new ArrayList<>();

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
            PayloadTypeRegistry.playC2S().register(entry.type, entry.codec);
            ServerPlayNetworking.registerGlobalReceiver(entry.type, (payload, ctx) ->
                    entry.handle(payload, new NetworkEvent.Context(NetworkDirection.PLAY_TO_SERVER, ctx.player())));
        } else {
            PayloadTypeRegistry.playS2C().register(entry.type, entry.codec);
            clientBound.add(entry);
        }
    }

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

    public <M> void send(PacketDistributor.PacketTarget target, M message) {
        target.send(ServerPlayNetworking.createS2CPacket(wrap(message)));
    }

    public <M> void sendToServer(M message) {
        SimpleChannelClient.sendToServer(wrap(message));
    }

    public <M> void reply(M message, NetworkEvent.Context context) {
        if (context.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ServerPlayNetworking.send(context.getSender(), wrap(message));
        } else {
            sendToServer(message);
        }
    }
}
