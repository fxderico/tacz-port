package com.tacz.guns.client.input;

import com.tacz.guns.GunMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public final class TaczKeyCategory {
    // shows up as key.category.tacz.tacz in the lang files
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, GunMod.MOD_ID));
}
