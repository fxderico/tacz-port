package com.tacz.guns.block.entity;

import com.tacz.guns.platform.BlockEntityTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.tacz.guns.util.ItemNbt;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.init.ModBlocks;
import com.tacz.guns.inventory.GunSmithTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

public class GunSmithTableBlockEntity extends BlockEntity implements /*? if neoforge {*//*net.minecraft.world.MenuProvider*//*?} else {*/net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory<Identifier>/*?}*/ {
    public static final BlockEntityType<GunSmithTableBlockEntity> TYPE = BlockEntityTypes.create(GunSmithTableBlockEntity::new,
            ModBlocks.GUN_SMITH_TABLE.get(),
            ModBlocks.WORKBENCH_111.get(),
            ModBlocks.WORKBENCH_121.get(),
            ModBlocks.WORKBENCH_211.get()
    );

    private static final String ID_TAG = "BlockId";

    @Nullable
    private Identifier id = null;

    public GunSmithTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(TYPE, pos, blockState);
    }

    public void setId(Identifier id) {
        this.id = id;
    }

    @Nullable
    public Identifier getId() {
        return id;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * Sent to the client when the menu opens (Forge did this with NetworkHooks.openScreen).
     */
    //? if fabric {
    @Override
    //?}
    public Identifier getScreenOpeningData(ServerPlayer player) {
        return getId() == null ? DefaultAssets.DEFAULT_BLOCK_ID : getId();
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Gun Smith Table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GunSmithTableMenu(id, inventory, getId());
    }

    @Override
    protected void loadAdditional(/*? if >=1.21.6 {*/ValueInput input/*?} else {*//*net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries*//*?}*/) {
        //? if >=1.21.6 {
        super.loadAdditional(input);
        //?} else {
        /*super.loadAdditional(tag, registries);
        ValueInput input = new ValueInput(tag, registries);
        *///?}
        this.id = input.getString(ID_TAG).map(Identifier::tryParse).orElse(DefaultAssets.DEFAULT_BLOCK_ID);
    }

    @Override
    protected void saveAdditional(/*? if >=1.21.6 {*/ValueOutput output/*?} else {*//*net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries*//*?}*/) {
        //? if >=1.21.6 {
        super.saveAdditional(output);
        //?} else {
        /*super.saveAdditional(tag, registries);
        ValueOutput output = new ValueOutput(tag, registries);
        *///?}
        if (id != null) {
            output.putString(ID_TAG, id.toString());
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
