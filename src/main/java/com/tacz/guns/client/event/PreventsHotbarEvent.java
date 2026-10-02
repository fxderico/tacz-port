package com.tacz.guns.client.event;

import com.tacz.guns.client.gui.GunRefitScreen;
import com.tacz.guns.client.gui.GunSmithTableScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import com.tacz.guns.platform.forge.api.distmarker.Dist;
import com.tacz.guns.platform.forge.client.event.RenderGuiOverlayEvent;
import com.tacz.guns.platform.forge.eventbus.api.SubscribeEvent;
import com.tacz.guns.platform.forge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class PreventsHotbarEvent {
    @SubscribeEvent
    public static void onRenderHotbarEvent(RenderGuiOverlayEvent.Pre event) {
        // todo 需要测试行为
        Screen screen = Minecraft.getInstance().screen;
        // 枪械合成台界面关闭背景
        if (screen instanceof GunSmithTableScreen) {
            event.setCanceled(true);
            return;
        }
        // 枪械改装界面关闭背景
        if (screen instanceof GunRefitScreen) {
            event.setCanceled(true);
        }
    }
}
