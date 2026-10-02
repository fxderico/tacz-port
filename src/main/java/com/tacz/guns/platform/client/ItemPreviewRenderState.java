package com.tacz.guns.platform.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * A rotating 3D item drawn inside a GUI box (gun smith table preview).
 */
@Environment(EnvType.CLIENT)
public record ItemPreviewRenderState(ItemStack stack, float pitch, float yaw, int x0, int y0, int x1, int y1, float scale,
                                     @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements PictureInPictureRenderState {
    public ItemPreviewRenderState(ItemStack stack, float pitch, float yaw, int x0, int y0, int x1, int y1, float scale) {
        this(stack, pitch, yaw, x0, y0, x1, y1, scale, null, PictureInPictureRenderState.getBounds(x0, y0, x1, y1, null));
    }
}
