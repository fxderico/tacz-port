package com.tacz.guns.mixin.client.forge;

import com.tacz.guns.platform.forge.client.event.RenderGuiOverlayEvent;
import com.tacz.guns.platform.forge.client.gui.overlay.VanillaGuiOverlay;
import com.tacz.guns.platform.forge.common.MinecraftForge;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The two overlay hooks TACZ needs: the crosshair, and the whole HUD (hidden while its screens are open).
 */
// 26.2 moved the HUD into its own class
@Mixin(/*? if >=26.2 {*//*net.minecraft.client.gui.Hud.class*//*?} else {*/Gui.class/*?}*/)
public abstract class GuiEventMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void tacz$crosshair(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new RenderGuiOverlayEvent.Pre(Minecraft.getInstance().getWindow(), graphics,
                deltaTracker.getGameTimeDeltaPartialTick(false), VanillaGuiOverlay.CROSSHAIR.type()))) {
            ci.cancel();
        }
    }

    @Inject(method = /*? if >=26.1 {*//*"extractRenderState"*//*?} else {*/"render"/*?}*/, at = @At("HEAD"), cancellable = true)
    private void tacz$hud(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new RenderGuiOverlayEvent.Pre(Minecraft.getInstance().getWindow(), graphics,
                deltaTracker.getGameTimeDeltaPartialTick(false), VanillaGuiOverlay.HOTBAR.type()))) {
            ci.cancel();
        }
    }
}
