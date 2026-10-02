package com.tacz.guns.client.gui.components.refit;

import com.tacz.guns.platform.client.GuiCompat;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tacz.guns.client.gui.GunRefitScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class RefitUnloadButton extends Button implements IComponentTooltip {
    public RefitUnloadButton(int pX, int pY, Button.OnPress pOnPress) {
        super(pX, pY, 8, 8, Component.empty(), pOnPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {

        int x = getX(), y = getY();
        if (isHoveredOrFocused()) {
            GuiCompat.blit(graphics, GunRefitScreen.UNLOAD_TEXTURE, x, y, width, height, 0, 0, 80, 80, 160, 80);
        } else {
            GuiCompat.blit(graphics, GunRefitScreen.UNLOAD_TEXTURE, x, y, width, height, 80, 0, 80, 80, 160, 80);
        }

    }

    @Override
    public void renderTooltip(Consumer<List<Component>> consumer) {
        if (this.isHoveredOrFocused()) {
            consumer.accept(Collections.singletonList(Component.translatable("tooltip.tacz.refit.unload")));
        }
    }
}
