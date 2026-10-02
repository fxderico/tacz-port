package com.tacz.guns.platform.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;

/**
 * Where TACZ's buffer based renderers end up. Up to 26.1 they draw into the main buffer source directly, since 26.2
 * their batches are submitted to the collector of the render they run in (see compat.MultiBufferSource).
 */
@Environment(EnvType.CLIENT)
public final class SubmitScope {
    private SubmitScope() {
    }

    public static void run(SubmitNodeCollector collector, Runnable action) {
        //? if >=26.2 {
        /*com.tacz.guns.platform.client.compat.MultiBufferSource.withCollector(collector, action);
        *///?} else {
        action.run();
        //?}
    }

    /**
     * The collector vanilla style submissions (player arms, item models) should go to right now.
     */
    public static SubmitNodeCollector current() {
        //? if >=26.2 {
        /*SubmitNodeCollector collector = com.tacz.guns.platform.client.compat.MultiBufferSource.currentCollector();
        if (collector == null) {
            throw new IllegalStateException("No TACZ submit scope active");
        }
        return collector;
        *///?} else {
        return Minecraft.getInstance().gameRenderer.getSubmitNodeStorage();
        //?}
    }
}
