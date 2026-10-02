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
public class InspectKey {
    public static final KeyMapping INSPECT_KEY = new KeyMapping("key.tacz.inspect.desc",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_H,
            TaczKeyCategory.CATEGORY);

    @SubscribeEvent
    public static void onInspectPress(InputEvent.Key event) {
        if (isInGame() && event.getAction() == InputConstants.PRESS && INSPECT_KEY.matches(event.getKeyEvent())) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null || player.isSpectator()) {
                return;
            }
            IClientPlayerGunOperator.fromLocalPlayer(player).inspect();
        }
    }

    public static boolean onInspectControllerPress(boolean isPress) {
        if (isInGame() && isPress) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null || player.isSpectator()) {
                return false;
            }
            IClientPlayerGunOperator.fromLocalPlayer(player).inspect();
            return true;
        }
        return false;
    }
}
