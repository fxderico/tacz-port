package com.tacz.guns.compat.playeranimator;

import com.tacz.guns.client.resource.GunDisplayInstance;
import net.minecraft.world.entity.LivingEntity;

import java.io.File;
import java.util.zip.ZipFile;

// player animator compat is not ported, the vanilla third person poses are used instead
public final class PlayerAnimatorCompat {
    public static boolean loadAnimationFromZip(ZipFile zipFile, String zipPath) { return false; }
    public static void loadAnimationFromFile(File file) {}
    public static void clearAllAnimationCache() {}
    public static boolean hasPlayerAnimator3rd(LivingEntity livingEntity, GunDisplayInstance display) { return false; }
    public static void stopAllAnimation(LivingEntity livingEntity) {}
    public static void stopAllAnimation(LivingEntity livingEntity, int fadeTime) {}
    public static void playAnimation(LivingEntity livingEntity, GunDisplayInstance display, float limbSwingAmount) {}
    public static boolean isInstalled() { return false; }
}
