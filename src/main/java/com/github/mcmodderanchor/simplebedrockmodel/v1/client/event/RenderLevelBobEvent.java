package com.github.mcmodderanchor.simplebedrockmodel.v1.client.event;

import com.tacz.guns.platform.forge.eventbus.api.Cancelable;
import com.tacz.guns.platform.forge.eventbus.api.Event;

/**
 * 当第一人称视角触发摇晃时，世界背景的摇晃
 */
public class RenderLevelBobEvent extends Event {
    /**
     * 使用注解也可以，但是热重载会导致游戏崩溃
     */
    @Override
    public boolean isCancelable() {
        return true;
    }

    @Cancelable
    public static class BobHurt extends RenderLevelBobEvent {
        public BobHurt() {
        }
    }

    @Cancelable
    public static class BobView extends RenderLevelBobEvent {
        public BobView() {
        }
    }
}
