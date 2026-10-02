package com.tacz.guns.mixin.client;

import com.tacz.guns.client.renderer.RenderStateEntity;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements RenderStateEntity {
    @Unique
    private @Nullable LivingEntity tacz$entity;

    @Override
    public @Nullable LivingEntity tacz$getEntity() {
        return tacz$entity;
    }

    @Override
    public void tacz$setEntity(@Nullable LivingEntity entity) {
        this.tacz$entity = entity;
    }
}
