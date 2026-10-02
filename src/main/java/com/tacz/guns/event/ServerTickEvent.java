package com.tacz.guns.event;

import com.tacz.guns.util.CycleTaskHelper;
import com.tacz.guns.platform.forge.event.TickEvent;
import com.tacz.guns.platform.forge.eventbus.api.SubscribeEvent;
import com.tacz.guns.platform.forge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ServerTickEvent {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        // 更新 CycleTaskHelper 中的任务
        CycleTaskHelper.tick();
    }
}
