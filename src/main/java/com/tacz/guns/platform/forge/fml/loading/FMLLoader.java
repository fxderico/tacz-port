package com.tacz.guns.platform.forge.fml.loading;

import com.tacz.guns.platform.forge.api.distmarker.Dist;

public final class FMLLoader {
    public static Dist getDist() {
        return FMLEnvironment.dist;
    }

    public static boolean isProduction() {
        return FMLEnvironment.production;
    }
}
