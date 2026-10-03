package com.tacz.guns.block.entity;

import com.tacz.guns.platform.BlockEntityTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.tacz.guns.util.ItemNbt;
import com.tacz.guns.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import static com.tacz.guns.block.StatueBlock.FACING;

public class StatueBlockEntity extends BlockEntity {
    public static final BlockEntityType<StatueBlockEntity> TYPE = BlockEntityTypes.create(StatueBlockEntity::new, ModBlocks.STATUE.get());
    private static final String ITEM_TAG = "Item";
    private ItemStack gunItem = ItemStack.EMPTY;

    public StatueBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(TYPE, pPos, pBlockState);
    }

    public static void clientTick(Level level, BlockPos blockPos, BlockState state, StatueBlockEntity statueBlockEntity) {
        if (level.getGameTime() % 100 == 0 && !statueBlockEntity.gunItem.isEmpty()) {
            Direction direction = state.getValue(FACING);

            double x = blockPos.getX() + direction.getStepX() * 0.75 + 0.5;
            double z = blockPos.getZ() + direction.getStepZ() * 0.75 + 0.5;

            double dx = -0.02 + level.getRandom().nextDouble() * 0.04;
            double dz = -0.02 + level.getRandom().nextDouble() * 0.04;
            double dy = -0.02 + level.getRandom().nextDouble() * 0.04;

            level.addParticle(ParticleTypes.END_ROD, x, blockPos.getY() + 2.25, z, dx, dy, dz);
        }
    }

    public ItemStack getGunItem() {
        return gunItem;
    }

    public void setGun(ItemStack stack) {
        this.dropItem();
        this.gunItem = stack.copy();
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
        this.setChanged();
    }

    public void dropItem() {
        if (!gunItem.isEmpty() && level != null) {
            Direction direction = getBlockState().getValue(FACING);
            Block.popResource(level, worldPosition.relative(direction).above(), gunItem);
            this.gunItem = ItemStack.EMPTY;
            if (level != null) {
                BlockState state = level.getBlockState(worldPosition);
                level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
            }
            this.setChanged();
        }
    }

    @Override
    protected void loadAdditional(/*? if >=1.21.6 {*/ValueInput input/*?} else {*//*net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries*//*?}*/) {
        //? if >=1.21.6 {
        super.loadAdditional(input);
        //?} else {
        /*super.loadAdditional(tag, registries);
        ValueInput input = new ValueInput(tag, registries);
        *///?}
        this.gunItem = input.read(ITEM_TAG, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(/*? if >=1.21.6 {*/ValueOutput output/*?} else {*//*net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries*//*?}*/) {
        //? if >=1.21.6 {
        super.saveAdditional(output);
        //?} else {
        /*super.saveAdditional(tag, registries);
        ValueOutput output = new ValueOutput(tag, registries);
        *///?}
        output.store(ITEM_TAG, ItemStack.OPTIONAL_CODEC, gunItem);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    /**
     * Forge's onRemove hook moved here in 1.21.5.
     */
    //? if >=1.21.5 {
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        this.dropItem();
        super.preRemoveSideEffects(pos, state);
    }
    //?}

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
