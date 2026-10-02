package com.tacz.guns.util.block;

import com.tacz.guns.config.common.AmmoConfig;
import com.tacz.guns.util.HitboxHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * TACZ's own explosion entity damage: exposure is raytraced against 15 points of the target's (fixed) hitbox,
 * damage and knockback scale with the ammo's explosion damage instead of the radius.
 * <p>
 * Explosion became an interface in 1.21.2, so block breaking, particles and sound are left to a vanilla
 * explosion (see ExplodeUtil) and this only does the entity part.
 */
public final class ProjectileExplosion {
    private ProjectileExplosion() {
    }

    public static void hurtEntities(ServerLevel level, Entity owner, Entity exploder, DamageSource damageSource,
                                    double x, double y, double z, float power, float radius, boolean knockback) {
        int minX = Mth.floor(x - (double) radius - 1.0D);
        int maxX = Mth.floor(x + (double) radius + 1.0D);
        int minY = Mth.floor(y - (double) radius - 1.0D);
        int maxY = Mth.floor(y + (double) radius + 1.0D);
        int minZ = Mth.floor(z - (double) radius - 1.0D);
        int maxZ = Mth.floor(z + (double) radius + 1.0D);
        radius *= 2;
        List<Entity> entities = level.getEntities(exploder, new AABB(minX, minY, minZ, maxX, maxY, maxZ));
        Vec3 explosionPos = new Vec3(x, y, z);

        for (Entity entity : entities) {
            if (entity.isSpectator()) {
                continue;
            }

            AABB boundingBox = HitboxHelper.getFixedBoundingBox(entity, owner);
            BlockHitResult result;
            double strength;
            double deltaX;
            double deltaY;
            double deltaZ;
            double minDistance = radius;

            Vec3[] d = new Vec3[15];

            if (!(entity instanceof LivingEntity)) {
                strength = Math.sqrt(entity.distanceToSqr(explosionPos)) * 2 / radius;
                deltaX = entity.getX() - x;
                deltaY = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - y;
                deltaZ = entity.getZ() - z;
            } else {
                deltaX = (boundingBox.maxX + boundingBox.minX) / 2;
                deltaY = (boundingBox.maxY + boundingBox.minY) / 2;
                deltaZ = (boundingBox.maxZ + boundingBox.minZ) / 2;
                d[0] = new Vec3(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
                d[1] = new Vec3(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
                d[2] = new Vec3(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
                d[3] = new Vec3(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
                d[4] = new Vec3(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
                d[5] = new Vec3(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
                d[6] = new Vec3(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
                d[7] = new Vec3(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
                d[8] = new Vec3(boundingBox.minX, deltaY, deltaZ);
                d[9] = new Vec3(boundingBox.maxX, deltaY, deltaZ);
                d[10] = new Vec3(deltaX, boundingBox.minY, deltaZ);
                d[11] = new Vec3(deltaX, boundingBox.maxY, deltaZ);
                d[12] = new Vec3(deltaX, deltaY, boundingBox.minZ);
                d[13] = new Vec3(deltaX, deltaY, boundingBox.maxZ);
                d[14] = new Vec3(deltaX, deltaY, deltaZ);
                for (int s = 0; s < 15; s++) {
                    result = BlockRayTrace.rayTraceBlocks(level, new ClipContext(explosionPos, d[s], ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity) null));
                    minDistance = (result.getType() != HitResult.Type.BLOCK) ? Math.min(minDistance, explosionPos.distanceTo(d[s])) : minDistance;
                }
                strength = minDistance * 2 / radius;
                deltaX -= x;
                deltaY -= y;
                deltaZ -= z;
            }

            if (strength > 1.0D) {
                continue;
            }

            double distanceToExplosion = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

            if (distanceToExplosion != 0.0D) {
                deltaX /= distanceToExplosion;
                deltaY /= distanceToExplosion;
                deltaZ /= distanceToExplosion;
            }

            double damage = 1.0D - strength;
            entity.hurtServer(level, damageSource, (float) damage * power);

            if (entity instanceof LivingEntity livingEntity) {
                // ProtectionEnchantment#getExplosionKnockbackAfterDampener became this attribute
                damage *= 1.0D - livingEntity.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE);
            }

            float multiplier = power * radius / 500;
            // 启用击退效果
            if (AmmoConfig.EXPLOSIVE_AMMO_KNOCK_BACK.get() && knockback) {
                entity.push(deltaX * damage * multiplier, deltaY * damage * multiplier, deltaZ * damage * multiplier);
                entity.hurtMarked = true;
            }
        }
    }
}
