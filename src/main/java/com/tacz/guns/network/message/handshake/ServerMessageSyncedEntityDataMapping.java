package com.tacz.guns.network.message.handshake;

import com.tacz.guns.GunMod;
import com.tacz.guns.entity.sync.core.SyncedDataKey;
import com.tacz.guns.entity.sync.core.SyncedEntityData;
import com.tacz.guns.platform.forge.network.NetworkEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.*;
import java.util.function.Supplier;

/**
 * Synced entity data key ids, server to client. Forge sent this during login, here it goes out when the player joins.
 */
public class ServerMessageSyncedEntityDataMapping {
    public static final Marker HANDSHAKE = MarkerManager.getMarker("TACZ_HANDSHAKE");
    private final Map<Identifier, List<Pair<Identifier, Integer>>> keyMap;

    public ServerMessageSyncedEntityDataMapping() {
        this.keyMap = Map.of();
    }

    private ServerMessageSyncedEntityDataMapping(Map<Identifier, List<Pair<Identifier, Integer>>> keyMap) {
        this.keyMap = keyMap;
    }

    public static void encode(ServerMessageSyncedEntityDataMapping message, FriendlyByteBuf buffer) {
        Set<SyncedDataKey<?, ?>> keys = SyncedEntityData.instance().getKeys();
        buffer.writeInt(keys.size());
        keys.forEach(key -> {
            int id = SyncedEntityData.instance().getInternalId(key);
            buffer.writeIdentifier(key.classKey().id());
            buffer.writeIdentifier(key.id());
            buffer.writeVarInt(id);
        });
    }

    public static ServerMessageSyncedEntityDataMapping decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        Map<Identifier, List<Pair<Identifier, Integer>>> keyMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            Identifier classId = buffer.readIdentifier();
            Identifier keyId = buffer.readIdentifier();
            int id = buffer.readVarInt();
            keyMap.computeIfAbsent(classId, c -> new ArrayList<>()).add(Pair.of(keyId, id));
        }
        return new ServerMessageSyncedEntityDataMapping(keyMap);
    }

    public static void handle(ServerMessageSyncedEntityDataMapping message, Supplier<NetworkEvent.Context> supplier) {
        GunMod.LOGGER.debug(HANDSHAKE, "Received synced key mappings from server");
        supplier.get().enqueueWork(() -> {
            if (!SyncedEntityData.instance().updateMappings(message)) {
                disconnect();
            }
        });
        supplier.get().setPacketHandled(true);
    }

    @Environment(EnvType.CLIENT)
    private static void disconnect() {
        var connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            connection.getConnection().disconnect(Component.literal("Connection closed - [TacZ] Received unknown synced data keys."));
        }
    }

    public Map<Identifier, List<Pair<Identifier, Integer>>> getKeyMap() {
        return this.keyMap;
    }
}
