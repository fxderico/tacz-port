package com.tacz.guns.mixin.client.forge;

import com.tacz.guns.platform.forge.client.event.ClientPlayerNetworkEvent;
import com.tacz.guns.platform.forge.common.MinecraftForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerEventMixin {
    @Unique
    private LocalPlayer tacz$oldPlayer;

    @Inject(method = "handleRespawn", at = @At("HEAD"))
    private void tacz$captureOldPlayer(ClientboundRespawnPacket packet, CallbackInfo ci) {
        this.tacz$oldPlayer = Minecraft.getInstance().player;
    }

    @Inject(method = "handleRespawn", at = @At("TAIL"))
    private void tacz$clonePlayer(ClientboundRespawnPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (this.tacz$oldPlayer != null && mc.player != null && mc.player != this.tacz$oldPlayer) {
            MinecraftForge.EVENT_BUS.post(new ClientPlayerNetworkEvent.Clone(mc.gameMode, this.tacz$oldPlayer, mc.player,
                    ((ClientPacketListener) (Object) this).getConnection()));
        }
        this.tacz$oldPlayer = null;
    }
}
