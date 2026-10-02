package com.tacz.guns.api.event.common;

import com.tacz.guns.platform.forge.eventbus.api.Event;

// kubejs compat was dropped in the fabric port, these stay as no-ops so the event classes keep compiling
public interface KubeJSGunEventPoster<E extends Event> {
    default void postEventToKubeJS(E event) {
    }

    default void postClientEventToKubeJS(E event) {
    }

    default void postServerEventToKubeJS(E event) {
    }
}
