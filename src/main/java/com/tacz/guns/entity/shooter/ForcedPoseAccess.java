package com.tacz.guns.entity.shooter;

import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.Nullable;

/**
 * Forge's Player#setForcedPose, added to Player by PlayerForcedPoseMixin. Used for crawling.
 */
public interface ForcedPoseAccess {
    void tacz$setForcedPose(@Nullable Pose pose);

    @Nullable
    Pose tacz$getForcedPose();
}
