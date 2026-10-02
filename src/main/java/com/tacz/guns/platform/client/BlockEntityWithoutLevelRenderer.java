package com.tacz.guns.platform.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * The old item renderer base class, gone from vanilla since 1.21.4. TaczItemSpecialRenderer calls into it.
 */
@Environment(EnvType.CLIENT)
public abstract class BlockEntityWithoutLevelRenderer implements ResourceManagerReloadListener {
    protected BlockEntityWithoutLevelRenderer() {
    }

    protected BlockEntityWithoutLevelRenderer(Object blockEntityRenderDispatcher, Object entityModelSet) {
    }

    public abstract void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                                      MultiBufferSource bufferSource, int packedLight, int packedOverlay);

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
    }
}
