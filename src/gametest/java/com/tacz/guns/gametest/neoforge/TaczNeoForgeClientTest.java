package com.tacz.guns.gametest.neoforge;

//? if neoforge {
/*import com.tacz.guns.gametest.TaczSmokeTest;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/^*
 * Runs {@link TaczSmokeTest} on NeoForge (./gradlew :<version>-neoforge:runGametest).
 *^/
@Mod(value = "tacz_gametest", dist = net.neoforged.api.distmarker.Dist.CLIENT)
public final class TaczNeoForgeClientTest {
    public TaczNeoForgeClientTest() {
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> TaczSmokeTest.INSTANCE.tick());
    }
}
*///?}
