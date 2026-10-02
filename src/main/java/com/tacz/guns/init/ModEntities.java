package com.tacz.guns.init;

import com.tacz.guns.GunMod;
import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.world.entity.EntityType;
import com.tacz.guns.platform.forge.registries.DeferredRegister;
import com.tacz.guns.platform.forge.registries.ForgeRegistries;
import com.tacz.guns.platform.forge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, GunMod.MOD_ID);

    public static RegistryObject<EntityType<EntityKineticBullet>> BULLET = ENTITY_TYPES.register("bullet", () -> EntityKineticBullet.TYPE);
}