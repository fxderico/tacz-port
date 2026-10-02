package com.tacz.guns.util;

import com.tacz.guns.config.common.AmmoConfig;
import com.tacz.guns.util.block.ProjectileExplosion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ExplodeUtil {
    /**
     * Vanilla explosion with entity damage switched off, it only breaks blocks and plays the effects.
     */
    private static final ExplosionDamageCalculator BLOCKS_ONLY = new ExplosionDamageCalculator() {
        @Override
        public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
            return false;
        }

        @Override
        public float getKnockbackMultiplier(Entity entity) {
            return 0;
        }
    };

    public static void createExplosion(Entity owner, Entity exploder, float damage, float radius, boolean knockback, boolean destroy, Vec3 hitPos) {
        // 客户端不执行
        if (!(exploder.level() instanceof ServerLevel level)) {
            return;
        }
        DamageSource source = level.damageSources().explosion(exploder, owner);
        // 依据配置文件读取方块破坏方式
        Level.ExplosionInteraction interaction = destroy ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE;
        level.explode(exploder, source, BLOCKS_ONLY, hitPos.x(), hitPos.y(), hitPos.z(), radius, AmmoConfig.EXPLOSIVE_AMMO_FIRE.get(), interaction);
        // TACZ's own exposure based damage and knockback
        ProjectileExplosion.hurtEntities(level, owner, exploder, source, hitPos.x(), hitPos.y(), hitPos.z(), damage, radius, knockback);
    }
}
