package com.tacz.guns.client.resource;

import com.mojang.serialization.MapCodec;
import com.tacz.guns.GunMod;
import com.tacz.guns.item.AmmoBoxItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Ammo box model variants and dye tint. These were an item property override and an item color
 * handler on Forge, 1.21.4 moved both into the items/ammo_box.json model definition.
 */
@Environment(EnvType.CLIENT)
public final class AmmoBoxModelProperties {
    private AmmoBoxModelProperties() {
    }

    public static final Identifier STATUE_ID = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "ammo_statue");
    public static final Identifier TINT_ID = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "ammo_box_color");

    // NeoForge registers both in its RegisterRangeSelectItemModelPropertyEvent / RegisterColorHandlersEvent.ItemTintSources
    public static void register() {
        //? if fabric {
        RangeSelectItemModelProperties.ID_MAPPER.put(STATUE_ID, Statue.MAP_CODEC);
        ItemTintSources.ID_MAPPER.put(TINT_ID, Tint.MAP_CODEC);
        //?}
    }

    public record Statue() implements RangeSelectItemModelProperty {
        public static final MapCodec<Statue> MAP_CODEC = MapCodec.unit(new Statue());

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
            return AmmoBoxItem.getStatue(stack, level, null, seed);
        }

        @Override
        public MapCodec<Statue> type() {
            return MAP_CODEC;
        }
    }

    public record Tint() implements ItemTintSource {
        public static final MapCodec<Tint> MAP_CODEC = MapCodec.unit(new Tint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            return ARGB.opaque(AmmoBoxItem.getColor(stack, 0));
        }

        @Override
        public MapCodec<Tint> type() {
            return MAP_CODEC;
        }
    }
}
