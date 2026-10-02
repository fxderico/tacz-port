package com.tacz.guns.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Render states (1.21.2+) don't keep their entity, TACZ's third person poses and offhand gun still need it.
 * Implemented on LivingEntityRenderState by LivingEntityRendererMixin.
 */
@Environment(EnvType.CLIENT)
public interface RenderStateEntity {
    @Nullable
    LivingEntity tacz$getEntity();

    void tacz$setEntity(@Nullable LivingEntity entity);
}
