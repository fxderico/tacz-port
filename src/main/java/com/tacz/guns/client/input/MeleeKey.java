package com.tacz.guns.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import com.tacz.guns.platform.forge.api.distmarker.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.tacz.guns.platform.forge.client.event.InputEvent;
import com.tacz.guns.platform.forge.eventbus.api.SubscribeEvent;
import com.tacz.guns.platform.forge.fml.common.Mod;

import static com.tacz.guns.util.InputExtraCheck.isInGame;

@Environment(EnvType.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class MeleeKey {
    public static final KeyMapping MELEE_KEY = new KeyMapping("key.tacz.melee.desc",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_V,
            TaczKeyCategory.CATEGORY);

    @SubscribeEvent
    public static void onMeleeKeyPress(InputEvent.Key event) {
        if (isInGame() && event.getAction() == InputConstants.PRESS && MELEE_KEY.matches(event.getKeyEvent())) {
            doMeleeLogic();
        }
    }

    @SubscribeEvent
    public static void onMeleeMousePress(InputEvent.MouseButton.Post event) {
        if (isInGame() && event.getAction() == InputConstants.PRESS && MELEE_KEY.matchesMouse(event.getMouseButtonEvent())) {
            doMeleeLogic();
        }
    }

    public static boolean onMeleeControllerPress(boolean isPress) {
        if (isInGame() && isPress) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null || player.isSpectator()) {
                return false;
            }
            IClientPlayerGunOperator operator = IClientPlayerGunOperator.fromLocalPlayer(player);
            if (!operator.isAim()) {
                operator.melee();
                return true;
            }
        }
        return false;
    }

    private static void doMeleeLogic() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator()) {
            return;
        }
        IClientPlayerGunOperator operator = IClientPlayerGunOperator.fromLocalPlayer(player);
        if (!operator.isAim()) {
            operator.melee();
        }
    }
}
