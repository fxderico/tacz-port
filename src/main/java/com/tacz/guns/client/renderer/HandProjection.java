package com.tacz.guns.client.renderer;

/**
 * The fov the hand and held item are drawn with this frame, recorded by CameraEventMixin (26.1+).
 */
public final class HandProjection {
    public static volatile float hudFov = 70.0F;

    private HandProjection() {
    }
}
