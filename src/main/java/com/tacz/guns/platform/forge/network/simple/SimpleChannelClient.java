package com.tacz.guns.platform.forge.network.simple;

import com.tacz.guns.platform.forge.network.NetworkDirection;
import com.tacz.guns.platform.forge.network.NetworkEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class SimpleChannelClient {
    public static void sendToServer(SimpleChannel.Payload<?> payload) {
        //? if forge {
        /*com.tacz.guns.network.NetworkHandler.CHANNEL.forgeSendToServer(payload);
        *///?} else if neoforge {
        /*net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(payload);
        *///?} else {
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(payload);
        //?}
    }

    // NeoForge registers the client bound handlers together with the payload types, see SimpleChannel#registerPayloads
    public static void registerReceivers(SimpleChannel channel) {
        //? if fabric {
        for (SimpleChannel.Entry<?> entry : channel.getClientBound()) {
            register(entry);
        }
        //?}
    }

    private static <M> void register(SimpleChannel.Entry<M> entry) {
        //? if fabric {
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(entry.type, (payload, ctx) ->
                entry.handle(payload, new NetworkEvent.Context(NetworkDirection.PLAY_TO_CLIENT, null)));
        //?}
    }
}
