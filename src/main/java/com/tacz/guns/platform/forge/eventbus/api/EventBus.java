package com.tacz.guns.platform.forge.eventbus.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Tiny synchronous event bus. Listeners registered for a type also receive its subclasses, like Forge.
 * Passing a Class registers its static @SubscribeEvent methods, passing an instance registers the instance methods.
 */
public class EventBus implements IEventBus {
    private record Listener(EventPriority priority, boolean receiveCanceled, Consumer<Event> consumer) {
    }

    private final Map<Class<?>, List<Listener>> listeners = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Listener>> resolved = new ConcurrentHashMap<>();

    @Override
    public void register(Object target) {
        boolean isClass = target instanceof Class<?>;
        Class<?> clazz = isClass ? (Class<?>) target : target.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);
            if (annotation == null || Modifier.isStatic(method.getModifiers()) != isClass) {
                continue;
            }
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1 || !Event.class.isAssignableFrom(params[0])) {
                throw new IllegalArgumentException("Bad @SubscribeEvent method " + method);
            }
            try {
                method.setAccessible(true);
                MethodHandle handle = MethodHandles.lookup().unreflect(method);
                if (!isClass) {
                    handle = handle.bindTo(target);
                }
                MethodHandle finalHandle = handle;
                add(params[0], new Listener(annotation.priority(), annotation.receiveCanceled(), event -> {
                    try {
                        finalHandle.invoke(event);
                    } catch (RuntimeException | Error e) {
                        throw e;
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Class<T> type, Consumer<T> listener) {
        add(type, new Listener(priority, receiveCanceled, (Consumer<Event>) listener));
    }

    private void add(Class<?> type, Listener listener) {
        listeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
        resolved.clear();
    }

    private List<Listener> resolve(Class<?> type) {
        return resolved.computeIfAbsent(type, t -> {
            List<Listener> out = new ArrayList<>();
            for (Class<?> c = t; c != null && Event.class.isAssignableFrom(c); c = c.getSuperclass()) {
                out.addAll(listeners.getOrDefault(c, List.of()));
            }
            out.sort(Comparator.comparingInt(l -> l.priority().ordinal()));
            return out;
        });
    }

    @Override
    public boolean post(Event event) {
        for (Listener listener : resolve(event.getClass())) {
            if (event.isCanceled() && !listener.receiveCanceled()) {
                continue;
            }
            listener.consumer().accept(event);
        }
        return event.isCanceled();
    }
}
