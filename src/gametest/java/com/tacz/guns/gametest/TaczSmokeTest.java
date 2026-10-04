package com.tacz.guns.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
//? if >=1.21.9 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
//?}
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntSupplier;

/**
 * Smoke test on a real client, for every loader and version: makes a world, shoots a husk, aims, reloads, mounts an
 * ACOG and checks third person. Plain vanilla calls driven from the client tick (Fabric's client gametest locks the
 * client and server together and does not exist before 1.21.4), inputs go through the real keyboard and mouse
 * handlers. Prints TACZ_SMOKE_TEST PASSED / FAILED and quits; screenshots end up in the run's screenshots folder.
 */
public final class TaczSmokeTest {
    public static final TaczSmokeTest INSTANCE = new TaczSmokeTest();

    private static final Logger LOGGER = LogManager.getLogger("TaczSmokeTest");
    private static final Identifier AK47 = Identifier.fromNamespaceAndPath("tacz", "ak47");
    private static final Identifier ACOG = Identifier.fromNamespaceAndPath("tacz", "scope_acog_ta31");

    private final Deque<IntSupplier> steps = new ArrayDeque<>();
    private int wait;
    private int screenshot;
    private int fullMagazine;
    private int ammoAfterShooting;
    private boolean finished;

    private TaczSmokeTest() {
    }

    private void script() {
        Minecraft mc = Minecraft.getInstance();
        // wait for the title screen, the first resource reload is done then
        // resources loaded; a fresh profile opens the accessibility onboarding screen first, skip it like Fabric's gametest
        steps.add(() -> mc.screen != null && mc.getOverlay() == null ? 20 : -1);
        steps.add(() -> {
            if (!(mc.screen instanceof TitleScreen)) {
                mc.options.onboardAccessibility = false;
                mc.options.save();
                Minecraft.getInstance().setScreen(new TitleScreen());
            }
            return 20;
        });
        steps.add(() -> shot("tacz_title"));
        steps.add(() -> {
            mc.createWorldOpenFlows().createFreshLevel("tacz_test",
                    //? if >=26.1 {
                    /*new LevelSettings("tacz_test", GameType.SURVIVAL, new LevelSettings.DifficultySettings(Difficulty.EASY, false, false), true,
                            WorldDataConfiguration.DEFAULT),
                    *///?} else {
                    new LevelSettings("tacz_test", GameType.SURVIVAL, false, Difficulty.EASY, true,
                            new GameRules(/*? if >=1.21.2 {*/WorldDataConfiguration.DEFAULT.enabledFeatures()/*?}*/), WorldDataConfiguration.DEFAULT),
                    //?}
                    WorldOptions.defaultWithRandomSeed(),
                    lookup -> lookup.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
                    new TitleScreen());
            return 0;
        });
        steps.add(() -> mc.level != null && mc.player != null && mc.screen == null && mc.getSingleplayerServer() != null ? 100 : -1);
        steps.add(() -> {
            command("time set noon");
            command("gamerule doDaylightCycle false");
            command("gamerule doMobSpawning false");
            command("tp @a 0 ~ 0 0 0");
            fullMagazine = onServer(TaczSmokeTest::giveLoadout);
            return 40;
        });
        steps.add(() -> shot("tacz_first_person"));
        steps.add(() -> {
            // husk 6 blocks south, looking 4 degrees down from the eyes lands on its chest
            command("execute as @p at @s run summon minecraft:husk ~ ~ ~6 {NoAI:1b,PersistenceRequired:1b}");
            command("execute as @p at @s run tp @s ~ ~ ~ 0 4");
            return 10;
        });
        steps.add(() -> {
            mouse(InputConstants.MOUSE_BUTTON_LEFT, InputConstants.PRESS);
            return 20;
        });
        steps.add(() -> shot("tacz_shooting"));
        steps.add(() -> {
            mouse(InputConstants.MOUSE_BUTTON_LEFT, InputConstants.RELEASE);
            return 10;
        });
        steps.add(() -> {
            ammoAfterShooting = onServer(TaczSmokeTest::heldAmmo);
            check(ammoAfterShooting < fullMagazine, "Holding the shoot key did not use any ammo (" + ammoAfterShooting + "/" + fullMagazine + ")");
            float health = onServer(TaczSmokeTest::huskHealth);
            check(health < 20, "The husk in front of the gun was not hit, health " + health);
            mouse(InputConstants.MOUSE_BUTTON_RIGHT, InputConstants.PRESS);
            return 20;
        });
        steps.add(() -> shot("tacz_aiming"));
        steps.add(() -> {
            mouse(InputConstants.MOUSE_BUTTON_RIGHT, InputConstants.RELEASE);
            return 10;
        });
        steps.add(() -> {
            key(InputConstants.KEY_R, InputConstants.PRESS);
            key(InputConstants.KEY_R, InputConstants.RELEASE);
            return 20;
        });
        steps.add(() -> shot("tacz_reloading"));
        steps.add(() -> 80);
        steps.add(() -> {
            int ammoAfterReload = onServer(TaczSmokeTest::heldAmmo);
            check(ammoAfterReload > ammoAfterShooting, "Reload did not refill the magazine (" + ammoAfterShooting + " -> " + ammoAfterReload + ")");
            onServer(server -> {
                mountScope(server);
                return 0;
            });
            return 5;
        });
        steps.add(() -> {
            ItemStack held = mc.player.getMainHandItem();
            Identifier scope = ((IGun) held.getItem()).getAttachmentId(held, AttachmentType.SCOPE);
            check(ACOG.equals(scope), "Installed scope reads back as " + scope);
            return 20;
        });
        steps.add(() -> shot("tacz_scope_hip"));
        steps.add(() -> {
            mouse(InputConstants.MOUSE_BUTTON_RIGHT, InputConstants.PRESS);
            return 20;
        });
        steps.add(() -> shot("tacz_scope_aiming"));
        steps.add(() -> 30);
        steps.add(() -> shot("tacz_scope_aiming_late"));
        steps.add(() -> {
            mouse(InputConstants.MOUSE_BUTTON_RIGHT, InputConstants.RELEASE);
            return 10;
        });
        steps.add(() -> {
            key(InputConstants.KEY_F5, InputConstants.PRESS);
            key(InputConstants.KEY_F5, InputConstants.RELEASE);
            return 20;
        });
        steps.add(() -> shot("tacz_third_person"));
        steps.add(() -> {
            key(InputConstants.KEY_F5, InputConstants.PRESS);
            key(InputConstants.KEY_F5, InputConstants.RELEASE);
            return 20;
        });
        steps.add(() -> shot("tacz_third_person_front"));
        steps.add(() -> {
            key(InputConstants.KEY_F5, InputConstants.PRESS);
            key(InputConstants.KEY_F5, InputConstants.RELEASE);
            Minecraft.getInstance().setScreen(new InventoryScreen(mc.player));
            return 5;
        });
        steps.add(() -> shot("tacz_inventory"));
        // ammo box variants: iron, gold and diamond level, filled, dyed and creative
        steps.add(() -> {
            Minecraft.getInstance().setScreen(null);
            onServer(TaczSmokeTest::giveAmmoBoxes);
            return 10;
        });
        steps.add(() -> {
            float empty = com.tacz.guns.item.AmmoBoxItem.getStatue(mc.player.getInventory().getItem(2), mc.level, mc.player, 0);
            float filled = com.tacz.guns.item.AmmoBoxItem.getStatue(mc.player.getInventory().getItem(5), mc.level, mc.player, 0);
            check(empty != filled, "An empty and a filled ammo box use the same model variant " + empty);
            Minecraft.getInstance().setScreen(new InventoryScreen(mc.player));
            return 10;
        });
        steps.add(() -> shot("tacz_ammo_boxes"));
        // the gun smith table, opened the way a player does: a right click on the block
        steps.add(() -> {
            Minecraft.getInstance().setScreen(null);
            command("execute as @p at @s run setblock ~ ~ ~2 tacz:workbench_a");
            return 20;
        });
        // with an empty hand the "by hand" filter is off and every recipe shows
        steps.add(() -> {
            key(InputConstants.KEY_9, InputConstants.PRESS);
            key(InputConstants.KEY_9, InputConstants.RELEASE);
            return 5;
        });
        steps.add(() -> openTable(mc, 0, 2));
        steps.add(() -> {
            if (tableTabs(mc) == 0) {
                throw new AssertionError("The gun smith table shows no recipes with an empty hand (" + tableState(mc) + ")");
            }
            return 2;
        });
        steps.add(() -> shot("tacz_gun_smith_table"));
        // an attachment workbench, holding the gun: the "by hand" filter turns on and only the gun's attachments show
        // (the gun smith table itself only makes guns, so it is empty with a gun in hand, like upstream TACZ)
        steps.add(() -> {
            mc.player.closeContainer();
            command("execute as @p at @s run setblock ~2 ~ ~ tacz:workbench_a{BlockId:\"tacz:attachment_workbench\"}");
            key(InputConstants.KEY_1, InputConstants.PRESS);
            key(InputConstants.KEY_1, InputConstants.RELEASE);
            return 20;
        });
        steps.add(() -> openTable(mc, 2, 0));
        steps.add(() -> {
            if (tableTabs(mc) == 0) {
                throw new AssertionError("Holding the AK-47, the attachment workbench shows none of its attachments (" + tableState(mc) + ")");
            }
            check(((com.tacz.guns.client.gui.components.GunPackList) field(mc.screen, "filterList")).isByHandSelected(),
                    "Holding a gun did not turn on the by hand filter");
            return 2;
        });
        steps.add(() -> shot("tacz_attachment_workbench_by_hand"));
        steps.add(() -> {
            mc.player.closeContainer();
            finish(null);
            return 0;
        });
    }

    /**
     * Call at the end of every client tick.
     */
    public void tick() {
        if (finished) {
            return;
        }
        if (steps.isEmpty() && screenshot == 0 && wait == 0) {
            script();
        }
        if (wait > 0) {
            wait--;
            return;
        }
        IntSupplier step = steps.peek();
        if (step == null) {
            return;
        }
        int next;
        try {
            next = step.getAsInt();
        } catch (Throwable t) {
            finish(t);
            return;
        }
        if (next >= 0) {
            steps.poll();
            wait = next;
        }
    }

    private int shot(String name) {
        Minecraft mc = Minecraft.getInstance();
        String file = String.format("%04d_%s.png", screenshot++, name);
        Screenshot.grab(mc.gameDirectory, file, Minecraft.getInstance().getMainRenderTarget(), /*? if >=1.21.6 {*/1, /*?}*/message -> LOGGER.info("Screenshot {}", file));
        return 2;
    }

    private void finish(Throwable failure) {
        finished = true;
        if (failure == null) {
            LOGGER.info("TACZ_SMOKE_TEST PASSED");
        } else {
            LOGGER.error("TACZ_SMOKE_TEST FAILED", failure);
        }
        Minecraft.getInstance().stop();
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void command(String command) {
        onServer(server -> {
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
            return 0;
        });
    }

    private static <T> T onServer(Function<MinecraftServer, T> action) {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        return server.submit(() -> action.apply(server)).join();
    }

    // the private input entry points GLFW (or SDL) calls
    private static void mouse(int button, int action) {
        Minecraft mc = Minecraft.getInstance();
        //? if >=1.21.9 {
        invoke(mc.mouseHandler, "onButton", new Class<?>[]{long.class, MouseButtonInfo.class, int.class},
                mc.getWindow().handle(), new MouseButtonInfo(button, 0), action);
        //?} else {
        /*invoke(mc.mouseHandler, "onPress", new Class<?>[]{long.class, int.class, int.class, int.class},
                mc.getWindow().getWindow(), button, action, 0);
        *///?}
    }

    private static void key(int key, int action) {
        Minecraft mc = Minecraft.getInstance();
        //? if >=1.21.9 {
        invoke(mc.keyboardHandler, "keyPress", new Class<?>[]{long.class, int.class, KeyEvent.class},
                mc.getWindow().handle(), action, new KeyEvent(key, 0, 0));
        //?} else {
        /*invoke(mc.keyboardHandler, "keyPress", new Class<?>[]{long.class, int.class, int.class, int.class, int.class},
                mc.getWindow().getWindow(), key, 0, action, 0);
        *///?}
    }

    private static void invoke(Object target, String name, Class<?>[] types, Object... args) {
        try {
            Method method = target.getClass().getDeclaredMethod(name, types);
            method.setAccessible(true);
            method.invoke(target, args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot send input through " + name, e);
        }
    }

    private static int giveLoadout(MinecraftServer server) {
        ServerPlayer player = player(server);
        CommonGunIndex index = TimelessAPI.getCommonGunIndex(AK47).orElseThrow(() -> new AssertionError("tacz:ak47 not loaded"));
        int magazine = index.getGunData().getAmmoAmount();
        ItemStack gun = GunItemBuilder.create().setId(AK47).setFireMode(FireMode.AUTO).setAmmoCount(magazine).setAmmoInBarrel(true).build();
        ItemStack ammo = AmmoItemBuilder.create().setId(index.getGunData().getAmmoId()).setCount(120).build();
        player.getInventory().setItem(0, gun);
        player.getInventory().setItem(1, ammo);
        player.getInventory()./*? if >=1.21.5 {*/setSelectedSlot(0)/*?} else {*//*selected = 0*//*?}*/;
        return magazine;
    }

    private static int openTable(Minecraft mc, int x, int z) {
        net.minecraft.core.BlockPos table = mc.player.blockPosition().offset(x, 0, z);
        mc.gameMode.useItemOn(mc.player, net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(table), net.minecraft.core.Direction.NORTH, table, false));
        return 40;
    }

    private static int tableTabs(Minecraft mc) {
        check(mc.screen instanceof com.tacz.guns.client.gui.GunSmithTableScreen, "Right clicking the gun smith table opened " + mc.screen);
        return ((java.util.Map<?, ?>) field(mc.screen, "recipeKeys")).size();
    }

    private static String tableState(Minecraft mc) {
        Object list = field(mc.screen, "filterList");
        return "the client knows " + com.tacz.guns.crafting.TaczRecipes.getAll(mc.level).size() + " recipes, by hand "
                + (list == null ? "-" : ((com.tacz.guns.client.gui.components.GunPackList) list).isByHandSelected())
                + ", holding " + mc.player.getMainHandItem();
    }

    private static int giveAmmoBoxes(MinecraftServer server) {
        ServerPlayer player = player(server);
        Identifier ammoId = TimelessAPI.getCommonGunIndex(AK47).orElseThrow().getGunData().getAmmoId();
        com.tacz.guns.api.item.IAmmoBox box = (com.tacz.guns.api.item.IAmmoBox) com.tacz.guns.init.ModItems.AMMO_BOX.get();
        ItemStack base = com.tacz.guns.init.ModItems.AMMO_BOX.get().getDefaultInstance();
        player.getInventory().setItem(2, box.setAmmoLevel(base.copy(), com.tacz.guns.item.AmmoBoxItem.IRON_LEVEL));
        player.getInventory().setItem(3, box.setAmmoLevel(base.copy(), com.tacz.guns.item.AmmoBoxItem.GOLD_LEVEL));
        player.getInventory().setItem(4, box.setAmmoLevel(base.copy(), com.tacz.guns.item.AmmoBoxItem.DIAMOND_LEVEL));
        ItemStack filled = box.setAmmoLevel(base.copy(), com.tacz.guns.item.AmmoBoxItem.IRON_LEVEL);
        box.setAmmoId(filled, ammoId);
        box.setAmmoCount(filled, 60);
        player.getInventory().setItem(5, filled);
        ItemStack dyed = filled.copy();
        dyed.set(net.minecraft.core.component.DataComponents.DYED_COLOR,
                new net.minecraft.world.item.component.DyedItemColor(0xC03030/*? if <1.21.5 {*//*, true*//*?}*/));
        player.getInventory().setItem(6, dyed);
        player.getInventory().setItem(7, box.setCreative(base.copy(), false));
        return 0;
    }

    @SuppressWarnings("unchecked")
    private static <T> T field(Object target, String name) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot read " + name, e);
        }
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
        List<? extends Husk> husks = server.overworld().getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(Husk.class), husk -> true);
        return husks.isEmpty() ? 0 : husks.getFirst().getHealth();
    }

    private static ServerPlayer player(MinecraftServer server) {
        return server.getPlayerList().getPlayers().getFirst();
    }
}
