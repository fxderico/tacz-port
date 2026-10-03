package com.tacz.guns.client.input;

import com.tacz.guns.GunMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public final class TaczKeyCategory {
    // shows up as key.category.tacz.tacz in the lang files
    // NeoForge adds it in RegisterKeyMappingsEvent#registerCategory instead
    //? if >=1.21.9 {
    public static final KeyMapping.Category CATEGORY = /*? if neoforge {*//*new KeyMapping.Category*//*?} else {*/KeyMapping.Category.register/*?}*/(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, GunMod.MOD_ID));
    //?} else {
    /*// categories were plain translation keys before 1.21.9
    public static final String CATEGORY = "key.category.tacz.tacz";
    *///?}
}
