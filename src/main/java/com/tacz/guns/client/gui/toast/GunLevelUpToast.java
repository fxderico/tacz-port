package com.tacz.guns.client.gui.toast;

import com.tacz.guns.platform.client.GuiCompat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@Environment(EnvType.CLIENT)
public class GunLevelUpToast implements Toast {
    private final Component title;
    private final Component subTitle;
    private final ItemStack icon;

    public GunLevelUpToast(ItemStack icon, Component titleComponent, @Nullable Component subtitle) {
        this.icon = icon;
        this.title = titleComponent;
        this.subTitle = subtitle;
    }

    private Visibility visibility = Visibility.SHOW;

    @Override
    public Visibility getWantedVisibility() {
        return visibility;
    }

    @Override
    public void update(ToastManager toastManager, long timeSinceLastVisible) {
        this.visibility = timeSinceLastVisible >= 5000L ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public void /*? if >=26.1 {*//*extractRenderState*//*?} else {*/render/*?}*/(GuiGraphics gui, Font font, long timeSinceLastVisible) {
        // todo 这个类没有实际使用，先不管了
        gui.drawString(font, this.title, 30, 7, 0xFFFFFF00, false);
        if (this.subTitle != null) {
            gui.drawString(font, this.subTitle, 30, 18, -1, false);
        }
        gui.renderFakeItem(this.icon, 8, 8);
    }
}
