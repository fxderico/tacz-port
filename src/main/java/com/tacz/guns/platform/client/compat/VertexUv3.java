package com.tacz.guns.platform.client.compat;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * VertexConsumer#setUv3 only exists since 26.3; older versions have no such attribute and drop it.
 */
public final class VertexUv3 {
    private VertexUv3() {
    }

    public static void set(VertexConsumer target, float u, float v) {
        //? if >=26.3 {
        /*target.setUv3(u, v);
        *///?}
    }
}
