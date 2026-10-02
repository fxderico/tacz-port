package com.tacz.guns.platform.forge.network;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public final class NetworkEvent {
    public static class Context {
        private final NetworkDirection direction;
        private final ServerPlayer sender;
        private final Runnable[] executor;
        private boolean handled;

        public Context(NetworkDirection direction, @Nullable ServerPlayer sender) {
            this.direction = direction;
            this.sender = sender;
            this.executor = null;
        }

        public NetworkDirection getDirection() {
            return direction;
        }

        @Nullable
        public ServerPlayer getSender() {
            return sender;
        }

        /**
         * Fabric already runs payload handlers on the main thread, so the work runs right away.
         */
        public void enqueueWork(Runnable runnable) {
            runnable.run();
        }

        public void setPacketHandled(boolean handled) {
            this.handled = handled;
        }

        public boolean getPacketHandled() {
            return handled;
        }
    }
}
