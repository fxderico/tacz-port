package com.tacz.guns.mixin.client;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
//? if <1.21.9 && fabric {
/*import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
*///?}

/**
 * Before 1.21.9 a key only drove one key mapping, so TACZ's shoot and aim keys lost the mouse buttons to vanilla's
 * attack and use. This passes presses on to every mapping bound to the key, like 1.21.9 and the Forge version do.
 */
@Mixin(KeyMapping.class)
public abstract class KeyMappingSharedKeyMixin {
    //? if <1.21.9 && fabric {
    /*@Shadow
    @Final
    private static Map<String, KeyMapping> ALL;
    @Shadow
    @Final
    private static Map<InputConstants.Key, KeyMapping> MAP;
    @Shadow
    private InputConstants.Key key;
    @Shadow
    private int clickCount;

    @Inject(method = "click", at = @At("TAIL"))
    private static void tacz$clickShared(InputConstants.Key key, CallbackInfo ci) {
        KeyMapping primary = MAP.get(key);
        for (KeyMapping mapping : ALL.values()) {
            if (mapping != primary && ((KeyMappingSharedKeyMixin) (Object) mapping).key.equals(key)) {
                ((KeyMappingSharedKeyMixin) (Object) mapping).clickCount++;
            }
        }
    }

    @Inject(method = "set", at = @At("TAIL"))
    private static void tacz$setShared(InputConstants.Key key, boolean down, CallbackInfo ci) {
        KeyMapping primary = MAP.get(key);
        for (KeyMapping mapping : ALL.values()) {
            if (mapping != primary && ((KeyMappingSharedKeyMixin) (Object) mapping).key.equals(key)) {
                mapping.setDown(down);
            }
        }
    }
    *///?}
}
