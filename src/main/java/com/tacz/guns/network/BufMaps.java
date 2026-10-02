package com.tacz.guns.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * FriendlyByteBuf#readMap / #writeMap, which 26.3 removed in favour of stream codecs. Same wire format: VarInt size, then the entries.
 */
public final class BufMaps {
    private BufMaps() {
    }

    public static <K, V> void write(FriendlyByteBuf buf, Map<K, V> map, BiConsumer<FriendlyByteBuf, K> keyWriter, BiConsumer<FriendlyByteBuf, V> valueWriter) {
        buf.writeVarInt(map.size());
        map.forEach((key, value) -> {
            keyWriter.accept(buf, key);
            valueWriter.accept(buf, value);
        });
    }

    public static <K, V> Map<K, V> read(FriendlyByteBuf buf, Function<FriendlyByteBuf, K> keyReader, Function<FriendlyByteBuf, V> valueReader) {
        int size = buf.readVarInt();
        Map<K, V> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            K key = keyReader.apply(buf);
            map.put(key, valueReader.apply(buf));
        }
        return map;
    }
}
