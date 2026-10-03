package com.tacz.guns.platform.client;

// item model definitions came with 1.21.4, see compat.LegacyItemModels before that
//? if >=1.21.4 {
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
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Bridges the 1.21.4+ data driven item models back to TACZ's BEWLR style renderers.
 * Every TACZ item uses {"type": "minecraft:special", "model": {"type": "tacz:item"}}; the renderer
 * draws straight into the main buffer source, which vanilla flushes right after the submit phase
 * (both for in-world items and for the GUI item atlas).
 */
@Environment(EnvType.CLIENT)
public final class TaczItemSpecialRenderer implements SpecialModelRenderer<ItemStack> {
    /**
     * The item JSONs select on minecraft:display_context and bake one renderer per context, 26.1 stopped passing it.
     */
    private final ItemDisplayContext bakedContext;

    private TaczItemSpecialRenderer(ItemDisplayContext bakedContext) {
        this.bakedContext = bakedContext;
    }

    @Override
    //? if >=26.1 {
    /*public void submit(@Nullable ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector,
                       int light, int overlay, boolean foil, int outlineColor) {
        ItemDisplayContext displayContext = this.bakedContext;
    *///?} else if >=1.21.9 {
    public void submit(@Nullable ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, SubmitNodeCollector collector,
                       int light, int overlay, boolean foil, int outlineColor) {
    //?} else {
    /*public void render(@Nullable ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer,
                       int light, int overlay, boolean foil) {
        SubmitNodeCollector collector = new SubmitNodeCollector(buffer);
    *///?}
        if (stack == null || stack.isEmpty()) {
            return;
        }
        BlockEntityWithoutLevelRenderer renderer = IClientItemExtensions.of(stack).getCustomRenderer();
        if (renderer == null) {
            return;
        }
        //? if >=1.21.9 {
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        //?} else {
        /*MultiBufferSource bufferSource = buffer;
        *///?}
        // the old ItemRenderer translated by -0.5 before handing over to a BEWLR, ItemTransform#apply does that now
        SubmitScope.run(collector, () -> renderer.renderByItem(stack, displayContext, poseStack, bufferSource, light, overlay));
    }

    //? if >=1.21.11 {
    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        consumer.accept(new Vector3f(-0.5F, -0.5F, -0.5F));
        consumer.accept(new Vector3f(1.5F, 1.5F, 1.5F));
    }
    //?} else if >=1.21.6 {
    /*@Override
    public void getExtents(java.util.Set<Vector3f> extents) {
        extents.add(new Vector3f(-0.5F, -0.5F, -0.5F));
        extents.add(new Vector3f(1.5F, 1.5F, 1.5F));
    }
    *///?}

    @Override
    public ItemStack extractArgument(ItemStack stack) {
        return stack;
    }

    public record Unbaked(ItemDisplayContext displayContext) implements SpecialModelRenderer.Unbaked/*? if >=26.1 {*//*<ItemStack>*//*?}*/ {
        public static final MapCodec<Unbaked> MAP_CODEC = ItemDisplayContext.CODEC
                .optionalFieldOf("display_context", ItemDisplayContext.NONE)
                .xmap(Unbaked::new, Unbaked::displayContext);

        @Override
        public SpecialModelRenderer<ItemStack> bake(/*? if >=1.21.9 {*/SpecialModelRenderer.BakingContext/*?} else {*//*net.minecraft.client.model.geom.EntityModelSet*//*?}*/ context) {
            return new TaczItemSpecialRenderer(this.displayContext);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
//?}
