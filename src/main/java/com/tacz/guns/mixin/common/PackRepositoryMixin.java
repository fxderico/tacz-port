package com.tacz.guns.mixin.common;

import com.tacz.guns.resource.GunPackLoader;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Arrays;

/**
 * Forge's AddPackFindersEvent: every repository that has a vanilla built-in source (client resources and
 * server data) also gets the gun pack source, for that pack type.
 */
@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static RepositorySource[] tacz$addGunPacks(RepositorySource[] sources) {
        for (RepositorySource source : sources) {
            if (source instanceof BuiltInPackSource builtIn) {
                RepositorySource[] extended = Arrays.copyOf(sources, sources.length + 1);
                extended[sources.length] = GunPackLoader.INSTANCE.forType(((BuiltInPackSourceAccessor) builtIn).tacz$getPackType());
                return extended;
            }
        }
        return sources;
    }
}
