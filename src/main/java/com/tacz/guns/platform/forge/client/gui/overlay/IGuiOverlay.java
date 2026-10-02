package com.tacz.guns.platform.forge.client.gui.overlay;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;

@FunctionalInterface
public interface IGuiOverlay {
    void render(Gui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight);
}
