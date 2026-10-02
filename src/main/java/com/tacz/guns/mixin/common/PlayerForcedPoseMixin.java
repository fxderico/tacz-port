package com.tacz.guns.mixin.common;

import com.tacz.guns.entity.shooter.ForcedPoseAccess;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerForcedPoseMixin implements ForcedPoseAccess {
    @Unique
    private @Nullable Pose tacz$forcedPose;

    @Override
    public void tacz$setForcedPose(@Nullable Pose pose) {
        this.tacz$forcedPose = pose;
    }

    @Override
    public @Nullable Pose tacz$getForcedPose() {
        return this.tacz$forcedPose;
    }

    @Inject(method = "updatePlayerPose", at = @At("HEAD"), cancellable = true)
    private void tacz$applyForcedPose(CallbackInfo ci) {
        if (this.tacz$forcedPose != null) {
            ((Player) (Object) this).setPose(this.tacz$forcedPose);
            ci.cancel();
        }
    }
}
