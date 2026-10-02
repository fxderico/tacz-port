package com.tacz.guns.platform.forge.network.simple;

import com.tacz.guns.platform.forge.network.NetworkDirection;
import com.tacz.guns.platform.forge.network.NetworkEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public final class SimpleChannelClient {
    public static void sendToServer(SimpleChannel.Payload<?> payload) {
        ClientPlayNetworking.send(payload);
    }

    public static void registerReceivers(SimpleChannel channel) {
        for (SimpleChannel.Entry<?> entry : channel.getClientBound()) {
            register(entry);
        }
    }

    private static <M> void register(SimpleChannel.Entry<M> entry) {
        ClientPlayNetworking.registerGlobalReceiver(entry.type, (payload, ctx) ->
                entry.handle(payload, new NetworkEvent.Context(NetworkDirection.PLAY_TO_CLIENT, null)));
    }
}
