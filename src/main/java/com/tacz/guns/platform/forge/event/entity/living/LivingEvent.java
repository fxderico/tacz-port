package com.tacz.guns.platform.forge.event.entity.living;

import com.tacz.guns.platform.forge.event.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;

public class LivingEvent extends EntityEvent {
    private final LivingEntity livingEntity;

    public LivingEvent(LivingEntity entity) {
        super(entity);
        this.livingEntity = entity;
    }

    @Override
    public LivingEntity getEntity() {
        return livingEntity;
    }
}
