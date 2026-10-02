package com.tacz.guns.network.message;

import com.tacz.guns.client.resource.ClientIndexManager;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.network.CommonNetworkCache;
import com.tacz.guns.resource.network.DataType;
import com.tacz.guns.network.BufMaps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.tacz.guns.platform.forge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;


public class ServerMessageSyncGunPack {
    private final Map<DataType, Map<Identifier, String>> cache;

    public ServerMessageSyncGunPack(Map<DataType, Map<Identifier, String>> cache) {
        this.cache = cache;
    }

    public static void encode(ServerMessageSyncGunPack message, FriendlyByteBuf buf) {
        BufMaps.write(buf, message.getCache(), FriendlyByteBuf::writeEnum, (buf1, map) -> {
            BufMaps.write(buf1, map, (b, id) -> b.writeIdentifier(id), (b, str) -> b.writeUtf(str));
        });
    }

    public static ServerMessageSyncGunPack decode(FriendlyByteBuf buf) {
        Map<DataType, Map<Identifier, String>> map = BufMaps.read(buf, buf1 -> buf1.readEnum(DataType.class), buf2 -> {
            return BufMaps.<Identifier, String>read(buf2, b -> b.readIdentifier(), b -> b.readUtf());
        });
        return new ServerMessageSyncGunPack(map);
    }

    public static void handle(ServerMessageSyncGunPack message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            boolean remoteConnection = isRemoteConnection();
            context.enqueueWork(() -> doSync(message, remoteConnection));
        }
        context.setPacketHandled(true);
    }


    @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
    private static boolean isRemoteConnection() {
        var connection = net.minecraft.client.Minecraft.getInstance().getConnection();
        return connection != null && !connection.getConnection().isMemoryConnection();
    }

    public Map<DataType, Map<Identifier, String>> getCache() {
        return cache;
    }

    @Environment(EnvType.CLIENT)
    private static void doSync(ServerMessageSyncGunPack message, boolean remoteConnection) {
        if (remoteConnection) {
            CommonAssetsManager.clearInstance();
        }
        CommonNetworkCache.INSTANCE.fromNetwork(message.cache);
        // 通知客户端重新构建ClientIndex
        ClientIndexManager.reload();
    }
}
