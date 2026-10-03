package com.tacz.guns.gametest.forge;

//? if forge {
/*import com.tacz.guns.gametest.TaczSmokeTest;
import net.minecraftforge.fml.common.Mod;

/^*
 * Runs {@link TaczSmokeTest} on Forge (./gradlew :<version>-forge:runClient -Psmoke).
 ^/
@Mod("tacz_gametest")
public final class TaczForgeClientTest {
    public TaczForgeClientTest() {
        if (!Boolean.getBoolean("tacz.smokeTest") || !net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient()) {
            return;
        }
        org.slf4j.LoggerFactory.getLogger("TaczSmokeTest").info("Forge smoke test armed");
        //? if >=1.21.6 {
        net.minecraftforge.event.TickEvent.ClientTickEvent.Post.BUS.addListener(event -> TaczSmokeTest.INSTANCE.tick());
        //?} else {
        /^net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.TickEvent.ClientTickEvent event) -> {
            if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) {
                TaczSmokeTest.INSTANCE.tick();
            }
        });
        ^///?}
    }
}
*///?}
