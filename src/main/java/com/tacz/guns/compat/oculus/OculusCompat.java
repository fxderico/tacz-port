package com.tacz.guns.compat.oculus;

import net.minecraft.client.renderer.MultiBufferSource;

// shader pack compat is not ported, everything here is a no-op
public final class OculusCompat {
    public static boolean isRenderShadow() { return false; }
    public static boolean isUsingRenderPack() { return false; }
    public static boolean endBatch(MultiBufferSource.BufferSource bufferSource) { return false; }
}
