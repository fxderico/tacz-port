package com.tacz.guns.network.message;

import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.platform.forge.network.NetworkEvent;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

import java.util.function.Supplier;

/**
 * Stand-in for Forge's IEntityAdditionalSpawnData, sent bundled with the bullet's spawn packet.
 */
public class ServerMessageBulletSpawnData {
    private final int entityId;
    private final byte[] data;

    public ServerMessageBulletSpawnData(int entityId, byte[] data) {
        this.entityId = entityId;
        this.data = data;
    }

    public static void encode(ServerMessageBulletSpawnData message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.entityId);
        buf.writeByteArray(message.data);
    }

    public static ServerMessageBulletSpawnData decode(FriendlyByteBuf buf) {
        return new ServerMessageBulletSpawnData(buf.readVarInt(), buf.readByteArray());
    }

    public static void handle(ServerMessageBulletSpawnData message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> apply(message));
        }
        context.setPacketHandled(true);
    }

    @Environment(EnvType.CLIENT)
    private static void apply(ServerMessageBulletSpawnData message) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        Entity entity = Minecraft.getInstance().level.getEntity(message.entityId);
        if (entity instanceof EntityKineticBullet bullet) {
            bullet.readSpawnData(new FriendlyByteBuf(Unpooled.wrappedBuffer(message.data)));
        }
    }
}
