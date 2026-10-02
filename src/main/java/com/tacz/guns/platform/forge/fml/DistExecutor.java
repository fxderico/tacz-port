package com.tacz.guns.platform.forge.fml;

import com.tacz.guns.platform.forge.api.distmarker.Dist;
import com.tacz.guns.platform.forge.fml.loading.FMLEnvironment;

import java.util.function.Supplier;

public final class DistExecutor {
    public static void unsafeRunWhenOn(Dist dist, Supplier<Runnable> toRun) {
        if (FMLEnvironment.dist == dist) {
            toRun.get().run();
        }
    }

    public static void safeRunWhenOn(Dist dist, Supplier<SafeRunnable> toRun) {
        if (FMLEnvironment.dist == dist) {
            toRun.get().run();
        }
    }

    public interface SafeRunnable extends Runnable {
    }
}
