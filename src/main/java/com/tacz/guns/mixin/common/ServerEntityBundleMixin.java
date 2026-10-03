package com.tacz.guns.mixin.common;

import net.minecraft.server.level.ServerEntity;
import org.spongepowered.asm.mixin.Mixin;
//? if fabric && <1.21.6 {
/*import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;
*///?}

/**
 * Bullets bundle their spawn data with the vanilla spawn packet, and the pairing data goes out as a bundle itself.
 * Fabric API flattens such nested bundles from 1.21.6, older versions failed with "Sending unknown packet bundle".
 */
@Mixin(ServerEntity.class)
public abstract class ServerEntityBundleMixin {
    //? if fabric && <1.21.6 {
    /*@SuppressWarnings("unchecked")
    @ModifyVariable(method = "sendPairingData", at = @At("HEAD"), argsOnly = true)
    private Consumer<Packet<ClientGamePacketListener>> tacz$flattenBundles(Consumer<Packet<ClientGamePacketListener>> consumer) {
        return packet -> {
            if (packet instanceof ClientboundBundlePacket bundle) {
                bundle.subPackets().forEach(subPacket -> consumer.accept((Packet<ClientGamePacketListener>) subPacket));
            } else {
                consumer.accept(packet);
            }
        };
    }
    *///?}
}
