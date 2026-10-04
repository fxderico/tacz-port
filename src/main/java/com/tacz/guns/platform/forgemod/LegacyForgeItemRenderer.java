package com.tacz.guns.platform.forgemod;

//? if forge && <1.21.4 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.platform.forge.client.extensions.common.IClientItemExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/^*
 * The Forge item renderer of every TACZ item before 1.21.4, handing each stack to TACZ's own renderer for it.
 ^/
public final class LegacyForgeItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final net.minecraftforge.client.extensions.common.IClientItemExtensions EXTENSIONS = new net.minecraftforge.client.extensions.common.IClientItemExtensions() {
        private BlockEntityWithoutLevelRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (renderer == null) {
                renderer = new LegacyForgeItemRenderer();
            }
            return renderer;
        }
    };

    private LegacyForgeItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        com.tacz.guns.platform.client.BlockEntityWithoutLevelRenderer custom = IClientItemExtensions.of(stack).getCustomRenderer();
        if (custom != null) {
            custom.renderByItem(stack, displayContext, poseStack, buffer, light, overlay);
        }
    }
}
*///?}
