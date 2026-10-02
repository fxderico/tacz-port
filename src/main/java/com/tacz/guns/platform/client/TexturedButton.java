package com.tacz.guns.platform.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;

/**
 * The pre 1.20.2 ImageButton: a region of a 256x256 texture, the hovered state sits yDiffTex pixels below.
 */
@Environment(EnvType.CLIENT)
public class TexturedButton extends Button {
    private final Identifier texture;
    private final int xTexStart;
    private final int yTexStart;
    private final int yDiffTex;

    public TexturedButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex, Identifier texture, OnPress onPress) {
        super(x, y, width, height, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
        this.texture = texture;
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
        this.yDiffTex = yDiffTex;
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int v = this.yTexStart + (this.isHoveredOrFocused() ? this.yDiffTex : 0);
        graphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, this.getX(), this.getY(), this.xTexStart, v, this.width, this.height, 256, 256);
    }
}
