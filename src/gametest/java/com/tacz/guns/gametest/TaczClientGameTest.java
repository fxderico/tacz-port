package com.tacz.guns.gametest;

//? if fabric && >=1.21.5 {

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.client.input.AimKey;
import com.tacz.guns.client.input.ReloadKey;
import com.tacz.guns.client.input.ShootKey;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Smoke test of the gun loop on a real client: hold the gun, shoot a mob, aim, reload, third person.
 * Screenshots end up in build/run/clientGameTest/screenshots.
 */
public class TaczClientGameTest implements FabricClientGameTest {
    private static final Identifier AK47 = Identifier.fromNamespaceAndPath("tacz", "ak47");
    private static final Identifier ACOG = Identifier.fromNamespaceAndPath("tacz", "scope_acog_ta31");

    @Override
    public void runTest(ClientGameTestContext context) {
        context.takeScreenshot("tacz_title");

        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            TestServerContext server = singleplayer.getServer();
            //? if >=26.2 {
            /*singleplayer.getConnection().waitForChunksRender();
            *///?} else {
            singleplayer.getClientWorld().waitForChunksRender();
            //?}

            server.runCommand("difficulty easy");
            server.runCommand("gamemode survival @a");
            server.runCommand("time set noon");
            server.runCommand("gamerule doDaylightCycle false");
            server.runCommand("gamerule doMobSpawning false");
            server.runCommand("tp @a 0 ~ 0 0 0");

            int fullMagazine = server.computeOnServer(TaczClientGameTest::giveLoadout);
            context.waitTicks(40);
            context.takeScreenshot("tacz_first_person");

            // husk 6 blocks south, looking 4 degrees down from the eyes lands on its chest
            server.runCommand("execute as @p at @s run summon minecraft:husk ~ ~ ~6 {NoAI:1b,PersistenceRequired:1b}");
            server.runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 4");
            context.waitTicks(10);

            context.getInput().holdKey(ShootKey.SHOOT_KEY);
            context.waitTicks(20);
            context.takeScreenshot("tacz_shooting");
            context.getInput().releaseKey(ShootKey.SHOOT_KEY);
            context.waitTicks(10);

            int ammoAfterShooting = server.computeOnServer(TaczClientGameTest::heldAmmo);
            if (ammoAfterShooting >= fullMagazine) {
                throw new AssertionError("Holding the shoot key did not use any ammo (" + ammoAfterShooting + "/" + fullMagazine + ")");
            }
            float huskHealth = server.computeOnServer(TaczClientGameTest::huskHealth);
            if (huskHealth >= 20) {
                throw new AssertionError("The husk in front of the gun was not hit, health " + huskHealth + ", "
                        + server.computeOnServer(TaczClientGameTest::describePositions));
            }

            context.getInput().holdKey(AimKey.AIM_KEY);
            context.waitTicks(20);
            context.takeScreenshot("tacz_aiming");
            context.getInput().releaseKey(AimKey.AIM_KEY);
            context.waitTicks(10);

            context.getInput().pressKey(ReloadKey.RELOAD_KEY);
            context.waitTicks(20);
            context.takeScreenshot("tacz_reloading");
            context.waitTicks(80);
            int ammoAfterReload = server.computeOnServer(TaczClientGameTest::heldAmmo);
            if (ammoAfterReload <= ammoAfterShooting) {
                throw new AssertionError("Reload did not refill the magazine (" + ammoAfterShooting + " -> " + ammoAfterReload + ")");
            }

            server.runOnServer(TaczClientGameTest::mountScope);
            context.waitTicks(5);
            Identifier clientScope = context.computeOnClient(mc -> {
                ItemStack held = mc.player.getMainHandItem();
                return ((IGun) held.getItem()).getAttachmentId(held, AttachmentType.SCOPE);
            });
            if (!ACOG.equals(clientScope)) {
                throw new AssertionError("Installed scope reads back as " + clientScope);
            }
            context.waitTicks(20);
            context.takeScreenshot("tacz_scope_hip");
            context.getInput().holdKey(AimKey.AIM_KEY);
            context.waitTicks(20);
            context.takeScreenshot("tacz_scope_aiming");
            context.getInput().releaseKey(AimKey.AIM_KEY);
            context.waitTicks(10);

            context.getInput().pressKey(options -> options.keyTogglePerspective);
            context.waitTicks(20);
            context.takeScreenshot("tacz_third_person");
            context.getInput().pressKey(options -> options.keyTogglePerspective);
            context.waitTicks(20);
            context.takeScreenshot("tacz_third_person_front");
            context.getInput().pressKey(options -> options.keyTogglePerspective);

            context.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
            context.waitTicks(5);
            context.takeScreenshot("tacz_inventory");
            context.setScreen(() -> null);
        }
    }

    private static int giveLoadout(MinecraftServer server) {
        ServerPlayer player = player(server);
        CommonGunIndex index = TimelessAPI.getCommonGunIndex(AK47).orElseThrow(() -> new AssertionError("tacz:ak47 not loaded"));
        int magazine = index.getGunData().getAmmoAmount();
        ItemStack gun = GunItemBuilder.create().setId(AK47).setFireMode(FireMode.AUTO).setAmmoCount(magazine)
                .setAmmoInBarrel(true).build();
        ItemStack ammo = AmmoItemBuilder.create().setId(index.getGunData().getAmmoId()).setCount(120).build();
        player.getInventory().setItem(0, gun);
        player.getInventory().setItem(1, ammo);
        player.getInventory().setSelectedSlot(0);
        return magazine;
    }

    private static void mountScope(MinecraftServer server) {
        ItemStack stack = player(server).getMainHandItem();
        if (stack.getItem() instanceof IGun gun) {
            gun.installAttachment(stack, AttachmentItemBuilder.create().setId(ACOG).build());
        }
    }

    private static int heldAmmo(MinecraftServer server) {
        ItemStack stack = player(server).getMainHandItem();
        if (!(stack.getItem() instanceof IGun gun)) {
            throw new AssertionError("Player is not holding a gun but " + stack);
        }
        return gun.getCurrentAmmoCount(stack);
    }

    private static float huskHealth(MinecraftServer server) {
        List<? extends Husk> husks = server.overworld().getEntities(/*? if >=26.2 {*//*net.minecraft.world.entity.EntityTypes*//*?} else {*/EntityType/*?}*/.HUSK, husk -> true);
        return husks.isEmpty() ? 0 : husks.getFirst().getHealth();
    }

    private static String describePositions(MinecraftServer server) {
        ServerPlayer player = player(server);
        List<? extends Husk> husks = server.overworld().getEntities(/*? if >=26.2 {*//*net.minecraft.world.entity.EntityTypes*//*?} else {*/EntityType/*?}*/.HUSK, husk -> true);
        return "player at " + player.position() + " looking " + player.getXRot() + "/" + player.getYRot()
                + ", husks " + husks.stream().map(husk -> husk.position().toString()).toList();
    }

    private static ServerPlayer player(MinecraftServer server) {
        return server.getPlayerList().getPlayers().getFirst();
    }
}
//?}
