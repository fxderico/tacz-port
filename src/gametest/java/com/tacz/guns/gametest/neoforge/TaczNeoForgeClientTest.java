package com.tacz.guns.gametest.neoforge;

//? if neoforge {
/*import com.mojang.blaze3d.platform.InputConstants;
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
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntSupplier;

/^*
 * The Fabric client gametest's scenario for NeoForge, which has no client gametest API: makes a world, shoots a
 * husk, aims, reloads, mounts an ACOG and checks third person. Inputs go through the real keyboard and mouse
 * handlers. Prints TACZ_SMOKE_TEST PASSED / FAILED and quits; screenshots end up in run/gametest/screenshots.
 *^/
@Mod(value = "tacz_gametest", dist = net.neoforged.api.distmarker.Dist.CLIENT)
public final class TaczNeoForgeClientTest {
    private static final Logger LOGGER = LogManager.getLogger("TaczSmokeTest");
    private static final Identifier AK47 = Identifier.fromNamespaceAndPath("tacz", "ak47");
    private static final Identifier ACOG = Identifier.fromNamespaceAndPath("tacz", "scope_acog_ta31");

    private final Deque<IntSupplier> steps = new ArrayDeque<>();
    private int wait;
    private int screenshot;
    private int fullMagazine;
    private int ammoAfterShooting;
    private boolean finished;

    public TaczNeoForgeClientTest(IEventBus modBus) {
        // the client does not exist yet while mods are constructed
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> tick());
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
                mc.setScreen(new TitleScreen());
            }
            return 20;
        });
        steps.add(() -> shot("tacz_title"));
        steps.add(() -> {
            mc.createWorldOpenFlows().createFreshLevel("tacz_test",
                    new LevelSettings("tacz_test", GameType.SURVIVAL, false, Difficulty.EASY, true,
                            new GameRules(WorldDataConfiguration.DEFAULT.enabledFeatures()), WorldDataConfiguration.DEFAULT),
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
            fullMagazine = onServer(TaczNeoForgeClientTest::giveLoadout);
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
            ammoAfterShooting = onServer(TaczNeoForgeClientTest::heldAmmo);
            check(ammoAfterShooting < fullMagazine, "Holding the shoot key did not use any ammo (" + ammoAfterShooting + "/" + fullMagazine + ")");
            float health = onServer(TaczNeoForgeClientTest::huskHealth);
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
            int ammoAfterReload = onServer(TaczNeoForgeClientTest::heldAmmo);
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
            mc.setScreen(new InventoryScreen(mc.player));
            return 5;
        });
        steps.add(() -> shot("tacz_inventory"));
        steps.add(() -> {
            mc.setScreen(null);
            finish(null);
            return 0;
        });
    }

    private void tick() {
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
        Screenshot.grab(mc.gameDirectory, file, mc.getMainRenderTarget(), 1, message -> LOGGER.info("Screenshot {}", file));
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
        invoke(mc.mouseHandler, "onButton", new Class<?>[]{long.class, MouseButtonInfo.class, int.class},
                mc.getWindow().handle(), new MouseButtonInfo(button, 0), action);
    }

    private static void key(int key, int action) {
        Minecraft mc = Minecraft.getInstance();
        invoke(mc.keyboardHandler, "keyPress", new Class<?>[]{long.class, int.class, KeyEvent.class},
                mc.getWindow().handle(), action, new KeyEvent(key, 0, 0));
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
        List<? extends Husk> husks = server.overworld().getEntities(EntityType.HUSK, husk -> true);
        return husks.isEmpty() ? 0 : husks.getFirst().getHealth();
    }

    private static ServerPlayer player(MinecraftServer server) {
        return server.getPlayerList().getPlayers().getFirst();
    }
}
*///?}
