package com.tacz.guns.init;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;

import com.tacz.guns.GunMod;
import com.tacz.guns.particles.BulletHoleOption;
import net.minecraft.core.particles.ParticleType;
import com.tacz.guns.platform.forge.registries.DeferredRegister;
import com.tacz.guns.platform.forge.registries.ForgeRegistries;
import com.tacz.guns.platform.forge.registries.RegistryObject;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, GunMod.MOD_ID);

    public static final RegistryObject<ParticleType<BulletHoleOption>> BULLET_HOLE = PARTICLE_TYPES.register("bullet_hole",
            () -> FabricParticleTypes.complex(false, BulletHoleOption.CODEC, BulletHoleOption.STREAM_CODEC));
}
