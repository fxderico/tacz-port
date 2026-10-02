package com.tacz.guns.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity types without a builder: Fabric API has one, NeoForge opens up the vanilla constructor.
 */
public final class BlockEntityTypes {
    private BlockEntityTypes() {
    }

    @FunctionalInterface
    public interface Factory<T extends BlockEntity> {
        T create(BlockPos pos, BlockState state);
    }

    public static <T extends BlockEntity> BlockEntityType<T> create(Factory<T> factory, Block... blocks) {
        //? if neoforge {
        /*return new BlockEntityType<>(factory::create, java.util.Set.of(blocks));
        *///?} else {
        return net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(factory::create, blocks).build();
        //?}
    }
}
