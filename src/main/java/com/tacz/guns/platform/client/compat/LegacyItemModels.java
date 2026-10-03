package com.tacz.guns.platform.client.compat;

//? if fabric && <1.21.4 {
/*import com.tacz.guns.client.resource.AmmoBoxModelProperties;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.item.AmmoBoxItem;
import com.tacz.guns.platform.client.BlockEntityWithoutLevelRenderer;
import com.tacz.guns.platform.forge.client.extensions.common.IClientItemExtensions;
import com.tacz.guns.platform.forge.registries.RegistryObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.Item;

/^*
 * Item rendering before 1.21.4's item model definitions (see TaczItemSpecialRenderer for the newer path):
 * TACZ's renderers hang off "builtin/entity" item models (src/main/legacy-item-models), the ammo box picks its
 * variant through a model predicate and gets its dye tint from an item color provider.
 ^/
@Environment(EnvType.CLIENT)
public final class LegacyItemModels {
    private LegacyItemModels() {
    }

    public static void register() {
        for (RegistryObject<Item> item : ModItems.ITEMS.getEntries()) {
            BuiltinItemRendererRegistry.INSTANCE.register(item.get(), (stack, displayContext, poseStack, buffer, light, overlay) -> {
                BlockEntityWithoutLevelRenderer renderer = IClientItemExtensions.of(stack).getCustomRenderer();
                if (renderer != null) {
                    renderer.renderByItem(stack, displayContext, poseStack, buffer, light, overlay);
                }
            });
        }
        // predicates are clamped to 0..1, the overrides use the variant index / 8
        ItemProperties.register(ModItems.AMMO_BOX.get(), AmmoBoxModelProperties.STATUE_ID,
                (stack, level, entity, seed) -> AmmoBoxItem.getStatue(stack, level, entity, seed) / 8.0F);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> ARGB.opaque(AmmoBoxItem.getColor(stack, tintIndex)), ModItems.AMMO_BOX.get());
    }
}
*///?}
