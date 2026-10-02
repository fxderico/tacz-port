package com.tacz.guns.platform.forge.client.event;

import com.tacz.guns.platform.forge.eventbus.api.Event;
import com.tacz.guns.platform.forge.eventbus.api.Cancelable;
import net.minecraft.world.entity.player.Player;

public class ComputeFovModifierEvent extends Event {
    private final Player player;
    private final float fovModifier;
    private float newFovModifier;

    public ComputeFovModifierEvent(Player player, float fovModifier) {
        this.player = player;
        this.fovModifier = fovModifier;
        this.newFovModifier = fovModifier;
    }

    public Player getPlayer() {
        return player;
    }

    public float getFovModifier() {
        return fovModifier;
    }

    public float getNewFovModifier() {
        return newFovModifier;
    }

    public void setNewFovModifier(float newFovModifier) {
        this.newFovModifier = newFovModifier;
    }
}
