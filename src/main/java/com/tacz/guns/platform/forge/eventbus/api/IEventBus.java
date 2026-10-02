package com.tacz.guns.platform.forge.eventbus.api;

import java.util.function.Consumer;

public interface IEventBus {
    void register(Object target);

    <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Class<T> type, Consumer<T> listener);

    default <T extends Event> void addListener(Class<T> type, Consumer<T> listener) {
        addListener(EventPriority.NORMAL, false, type, listener);
    }

    /**
     * @return true if the event was canceled
     */
    boolean post(Event event);
}
