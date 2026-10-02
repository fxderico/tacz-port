package com.tacz.guns.platform.forge.event.entity.living;

import com.tacz.guns.platform.forge.eventbus.api.Cancelable;
import net.minecraft.world.entity.LivingEntity;

@Cancelable
public class LivingKnockBackEvent extends LivingEvent {
    private float strength;
    private double ratioX, ratioZ;

    public LivingKnockBackEvent(LivingEntity target, float strength, double ratioX, double ratioZ) {
        super(target);
        this.strength = strength;
        this.ratioX = ratioX;
        this.ratioZ = ratioZ;
    }

    public float getStrength() {
        return strength;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    public double getRatioX() {
        return ratioX;
    }

    public void setRatioX(double ratioX) {
        this.ratioX = ratioX;
    }

    public double getRatioZ() {
        return ratioZ;
    }

    public void setRatioZ(double ratioZ) {
        this.ratioZ = ratioZ;
    }
}
