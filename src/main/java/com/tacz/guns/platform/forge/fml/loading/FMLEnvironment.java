package com.tacz.guns.platform.forge.fml.loading;

import com.tacz.guns.platform.Loader;
import com.tacz.guns.platform.forge.api.distmarker.Dist;

public final class FMLEnvironment {
    public static final Dist dist = Loader.isClient() ? Dist.CLIENT : Dist.DEDICATED_SERVER;
    public static final boolean production = Loader.isProduction();
}
