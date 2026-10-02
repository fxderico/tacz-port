package com.tacz.guns.platform.forge.event.entity;

import com.tacz.guns.platform.forge.eventbus.api.Cancelable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

@Cancelable
public class EntityTravelToDimensionEvent extends EntityEvent {
    private final ResourceKey<Level> dimension;

    public EntityTravelToDimensionEvent(Entity entity, ResourceKey<Level> dimension) {
        super(entity);
        this.dimension = dimension;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }
}
