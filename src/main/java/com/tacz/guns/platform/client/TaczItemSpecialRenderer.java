package com.tacz.guns.platform.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.tacz.guns.platform.forge.client.extensions.common.IClientItemExtensions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Bridges the 1.21.4+ data driven item models back to TACZ's BEWLR style renderers.
 * Every TACZ item uses {"type": "minecraft:special", "model": {"type": "tacz:item"}}; the renderer
 * draws straight into the main buffer source, which vanilla flushes right after the submit phase
 * (both for in-world items and for the GUI item atlas).
 */
@Environment(EnvType.CLIENT)
public final class TaczItemSpecialRenderer implements SpecialModelRenderer<ItemStack> {
    public static final TaczItemSpecialRenderer INSTANCE = new TaczItemSpecialRenderer();

    @Override
    public void submit(@Nullable ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, SubmitNodeCollector collector,
                       int light, int overlay, boolean foil, int outlineColor) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        BlockEntityWithoutLevelRenderer renderer = IClientItemExtensions.of(stack).getCustomRenderer();
        if (renderer == null) {
            return;
        }
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        poseStack.pushPose();
        // old ItemRenderer did this before handing over to a BEWLR
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        renderer.renderByItem(stack, displayContext, poseStack, bufferSource, light, overlay);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        consumer.accept(new Vector3f(-0.5F, -0.5F, -0.5F));
        consumer.accept(new Vector3f(1.5F, 1.5F, 1.5F));
    }

    @Override
    public ItemStack extractArgument(ItemStack stack) {
        return stack;
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext context) {
            return INSTANCE;
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
