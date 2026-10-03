package com.tacz.guns.client.tooltip;

import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.inventory.tooltip.AmmoBoxTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class ClientAmmoBoxTooltip implements ClientTooltipComponent {
    private final ItemStack ammo;
    private final Component count;
    private final Component ammoName;

    public ClientAmmoBoxTooltip(AmmoBoxTooltip tooltip) {
        this.ammo = tooltip.getAmmo();
        ItemStack ammoBox = tooltip.getAmmoBox();
        if (ammoBox.getItem() instanceof IAmmoBox box && box.isCreative(ammoBox)) {
            this.count = Component.literal("∞");
        } else {
            this.count = Component.translatable("tooltip.tacz.ammo_box.count", tooltip.getCount());
        }
        this.ammoName = this.ammo.getHoverName();
    }

    @Override
    public int getHeight(Font font) {
        return 28;
    }

    @Override
    public int getWidth(Font font) {
        return Math.max(font.width(ammoName), font.width(count)) + 22;
    }

    /*? if >=1.21.6 {*/@Override/*?}*/
    public void renderText(GuiGraphics textGraphics, Font font, int pX, int pY) {
        textGraphics.drawString(font, ammoName, pX + 20, pY + 4, 0xFF000000 | 0xffaa00, false);
        textGraphics.drawString(font, count, pX + 20, pY + 15, 0xFF000000 | 0x666666, false);
    }

    @Override
    public void renderImage(Font pFont, int pX, int pY, int tooltipWidth, int tooltipHeight, GuiGraphics pGraphics) {
        // before 1.21.6 the text pass had no GuiGraphics
        //? if <1.21.6
        /*renderText(pGraphics, pFont, pX, pY);*/
        pGraphics.renderItem(ammo, pX, pY + 5);
    }
}
