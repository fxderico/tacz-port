package com.tacz.guns.platform.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.platform.forge.client.extensions.common.IClientItemExtensions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
//? if >=1.21.4
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Replacement for ItemRenderer#renderStatic. TACZ items are drawn right away through their renderer,
 * anything else goes through the vanilla item render state into the game renderer's submit queue
 * (flushed after the level, block entities and hands).
 */
@Environment(EnvType.CLIENT)
public final class ItemRenderHelper {
    private ItemRenderHelper() {
    }

    public static void renderStatic(ItemStack stack, ItemDisplayContext context, int light, int overlay, PoseStack poseStack,
                                    MultiBufferSource buffer, @Nullable Level level, int seed) {
        if (stack.isEmpty()) {
            return;
        }
        BlockEntityWithoutLevelRenderer renderer = IClientItemExtensions.of(stack).getCustomRenderer();
        if (renderer != null) {
            poseStack.pushPose();
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            renderer.renderByItem(stack, context, poseStack, buffer, light, overlay);
            poseStack.popPose();
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        //? if >=1.21.4 {
        ItemStackRenderState state = new ItemStackRenderState();
        mc.getItemModelResolver().updateForTopItem(state, stack, context, level, null, seed);
        //?}
        //? if >=1.21.9 {
        state.submit(poseStack, SubmitScope.current(), light, overlay, 0);
        //?} else if >=1.21.4 {
        /*state.render(poseStack, buffer, light, overlay);
        *///?} else {
        /*mc.getItemRenderer().renderStatic(stack, context, light, overlay, poseStack, buffer, level, seed);
        *///?}
    }
}
