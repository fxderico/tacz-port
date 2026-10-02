package com.tacz.guns.platform.forge.fml.common;

import com.tacz.guns.platform.forge.api.distmarker.Dist;

import java.lang.annotation.*;

/**
 * Kept only so the @Mod.EventBusSubscriber markers still compile; the classes are registered by hand in EventSubscribers.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod {
    String value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface EventBusSubscriber {
        Dist[] value() default {Dist.CLIENT, Dist.DEDICATED_SERVER};

        String modid() default "";

        Bus bus() default Bus.FORGE;

        enum Bus {
            FORGE, MOD
        }
    }
}
