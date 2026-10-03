package com.tacz.guns.platform.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
//? if >=1.21.6
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

/**
 * 1.21.6 turned GuiGraphics into a deferred 2D renderer: no shader color, no blend toggles, blits take a
 * pipeline and a color, and text with a zero alpha color is skipped. These helpers keep the old call shapes.
 */
@Environment(EnvType.CLIENT)
public final class GuiCompat {
    private static int shaderColor = -1;

    private GuiCompat() {
    }

    /**
     * Old RenderSystem#setShaderColor, applied as the tint of following GuiCompat blits.
     */
    public static void setShaderColor(float r, float g, float b, float a) {
        shaderColor = ARGB.colorFromFloat(Mth.clamp(a, 0, 1), Mth.clamp(r, 0, 1), Mth.clamp(g, 0, 1), Mth.clamp(b, 0, 1));
    }

    public static int shaderColor() {
        return shaderColor;
    }

    /**
     * blit(texture, x, y, u, v, width, height, textureWidth, textureHeight)
     */
    public static void blit(GuiGraphics graphics, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        //? if >=1.21.2 {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, textureWidth, textureHeight, shaderColor);
        //?} else {
        /*tinted(() -> graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight));
        *///?}
    }

    /**
     * blit(texture, x, y, width, height, u, v, uWidth, vHeight, textureWidth, textureHeight)
     */
    public static void blit(GuiGraphics graphics, Identifier texture, int x, int y, int width, int height, float u, float v, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        //? if >=1.21.2 {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, shaderColor);
        //?} else {
        /*tinted(() -> graphics.blit(texture, x, y, width, height, u, v, uWidth, vHeight, textureWidth, textureHeight));
        *///?}
    }

    /**
     * blit(texture, x, y, u, v, width, height) on a 256x256 texture
     */
    public static void blit(GuiGraphics graphics, Identifier texture, int x, int y, int u, int v, int width, int height) {
        //? if >=1.21.2 {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, 256, 256, shaderColor);
        //?} else {
        /*tinted(() -> graphics.blit(texture, x, y, u, v, width, height));
        *///?}
    }

    //? if <1.21.2 {
    /*// blits had no color before 1.21.2, the shader color tinted them, and blending was up to the caller
    private static void tinted(Runnable blit) {
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(ARGB.red(shaderColor) / 255.0F, ARGB.green(shaderColor) / 255.0F,
                ARGB.blue(shaderColor) / 255.0F, ARGB.alpha(shaderColor) / 255.0F);
        blit.run();
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
    }
    *///?}

    private static int opaque(int color) {
        return (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
    }

    public static void drawString(GuiGraphics graphics, Font font, @Nullable String text, float x, float y, int color, boolean shadow) {
        graphics.drawString(font, text, Math.round(x), Math.round(y), opaque(color), shadow);
    }

    public static void drawString(GuiGraphics graphics, Font font, @Nullable String text, float x, float y, int color) {
        drawString(graphics, font, text, x, y, color, true);
    }

    public static void drawString(GuiGraphics graphics, Font font, Component text, float x, float y, int color, boolean shadow) {
        graphics.drawString(font, text, Math.round(x), Math.round(y), opaque(color), shadow);
    }

    public static void drawString(GuiGraphics graphics, Font font, Component text, float x, float y, int color) {
        drawString(graphics, font, text, x, y, color, true);
    }

    public static void drawString(GuiGraphics graphics, Font font, FormattedCharSequence text, float x, float y, int color, boolean shadow) {
        graphics.drawString(font, text, Math.round(x), Math.round(y), opaque(color), shadow);
    }

    public static void drawString(GuiGraphics graphics, Font font, FormattedCharSequence text, float x, float y, int color) {
        drawString(graphics, font, text, x, y, color, true);
    }

    public static void drawCenteredString(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        graphics.drawCenteredString(font, text, x, y, opaque(color));
    }

    public static void drawCenteredString(GuiGraphics graphics, Font font, Component text, int x, int y, int color) {
        graphics.drawCenteredString(font, text, x, y, opaque(color));
    }

    public static void drawCenteredString(GuiGraphics graphics, Font font, FormattedCharSequence text, int x, int y, int color) {
        graphics.drawCenteredString(font, text, x, y, opaque(color));
    }

    public static void drawWordWrap(GuiGraphics graphics, Font font, net.minecraft.network.chat.FormattedText text, int x, int y, int width, int color) {
        graphics.drawWordWrap(font, text, x, y, width, opaque(color));
    }

    /**
     * Old AbstractWidget#renderScrollingString: text clipped to the box, scrolling sideways when it does not fit.
     */
    public static void renderScrollingString(GuiGraphics graphics, Font font, Component text, int minX, int minY, int maxX, int maxY, int color) {
        int textWidth = font.width(text);
        int y = (minY + maxY - 9) / 2 + 1;
        int boxWidth = maxX - minX;
        if (textWidth > boxWidth) {
            int overflow = textWidth - boxWidth;
            double time = net.minecraft.util.Util.getMillis() / 1000.0;
            double period = Math.max(overflow * 0.5, 3.0);
            double phase = Math.sin(Math.PI / 2 * Math.cos(Math.PI * 2 * time / period)) / 2.0 + 0.5;
            double offset = Mth.lerp(phase, 0.0, overflow);
            graphics.enableScissor(minX, minY, maxX, maxY);
            graphics.drawString(font, text, minX - (int) offset, y, opaque(color));
            graphics.disableScissor();
        } else {
            graphics.drawCenteredString(font, text, (minX + maxX) / 2, y, opaque(color));
        }
    }
}
