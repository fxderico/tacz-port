package com.tacz.guns.platform.forge.client.event;

import com.tacz.guns.platform.forge.eventbus.api.Event;
import com.tacz.guns.platform.forge.eventbus.api.Cancelable;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;

public abstract class ViewportEvent extends Event {
    private final GameRenderer renderer;
    private final Camera camera;
    private final double partialTick;

    public ViewportEvent(GameRenderer renderer, Camera camera, double partialTick) {
        this.renderer = renderer;
        this.camera = camera;
        this.partialTick = partialTick;
    }

    public GameRenderer getRenderer() {
        return renderer;
    }

    public Camera getCamera() {
        return camera;
    }

    public double getPartialTick() {
        return partialTick;
    }

    public static class ComputeCameraAngles extends ViewportEvent {
        private float yaw, pitch, roll;

        public ComputeCameraAngles(GameRenderer renderer, Camera camera, double partialTick, float yaw, float pitch, float roll) {
            super(renderer, camera, partialTick);
            this.yaw = yaw;
            this.pitch = pitch;
            this.roll = roll;
        }

        public float getYaw() {
            return yaw;
        }

        public void setYaw(float yaw) {
            this.yaw = yaw;
        }

        public float getPitch() {
            return pitch;
        }

        public void setPitch(float pitch) {
            this.pitch = pitch;
        }

        public float getRoll() {
            return roll;
        }

        public void setRoll(float roll) {
            this.roll = roll;
        }
    }

    public static class ComputeFov extends ViewportEvent {
        private final boolean usedConfiguredFov;
        private double fov;

        public ComputeFov(GameRenderer renderer, Camera camera, double partialTick, double fov, boolean usedConfiguredFov) {
            super(renderer, camera, partialTick);
            this.usedConfiguredFov = usedConfiguredFov;
            this.fov = fov;
        }

        public double getFOV() {
            return fov;
        }

        public void setFOV(double fov) {
            this.fov = fov;
        }

        public boolean usedConfiguredFov() {
            return usedConfiguredFov;
        }
    }
}
