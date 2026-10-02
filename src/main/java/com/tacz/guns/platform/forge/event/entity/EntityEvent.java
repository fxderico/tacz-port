package com.tacz.guns.platform.forge.event.entity;

import com.tacz.guns.platform.forge.eventbus.api.Event;
import com.tacz.guns.platform.forge.eventbus.api.Cancelable;
import net.minecraft.world.entity.Entity;

public class EntityEvent extends Event {
    private final Entity entity;

    public EntityEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
