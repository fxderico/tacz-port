package com.tacz.guns.platform.forge.event.entity;

import com.tacz.guns.platform.forge.eventbus.api.Cancelable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

@Cancelable
public class EntityJoinLevelEvent extends EntityEvent {
    private final Level level;

    public EntityJoinLevelEvent(Entity entity, Level level) {
        super(entity);
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }
}
