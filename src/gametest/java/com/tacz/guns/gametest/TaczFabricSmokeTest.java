package com.tacz.guns.gametest;

//? if fabric {
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Runs {@link TaczSmokeTest} on Fabric (./gradlew :<version>:runSmokeTest), only when -Dtacz.smoketest=true.
 */
public final class TaczFabricSmokeTest implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (Boolean.getBoolean("tacz.smoketest")) {
            ClientTickEvents.END_CLIENT_TICK.register(client -> TaczSmokeTest.INSTANCE.tick());
        }
    }
}
//?}
