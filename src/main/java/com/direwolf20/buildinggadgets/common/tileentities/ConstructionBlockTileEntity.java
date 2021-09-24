package com.direwolf20.buildinggadgets.common.tileentities;

import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class ConstructionBlockTileEntity extends BlockEntity {

    private BlockData blockState;

    public ConstructionBlockTileEntity(BlockPos pos, BlockState state) {
        super(OurTileEntities.CONSTRUCTION_BLOCK_TILE_ENTITY, pos, state);
    }

    public void setBlockState(BlockData state) {
        blockState = state;
        markDirtyClient();
    }

    @NotNull
    @Override
    public BlockState getBlockState() {
        return getConstructionBlockData().getState();
    }

    @NotNull
    public BlockData getConstructionBlockData() {
        if (blockState == null)
            return BlockData.AIR;
        return blockState;
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        blockState = BlockData.tryDeserialize(nbt.getCompound(NBTKeys.TE_CONSTRUCTION_STATE), true);
        markDirtyClient();
    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag compound) {
        if (blockState != null) {
            compound.put(NBTKeys.TE_CONSTRUCTION_STATE, blockState.serialize(true));
        }
        return super.save(compound);
    }

    private void markDirtyClient() {
        setChanged();
        if (getLevel() != null) {
            BlockState state = getLevel().getBlockState(getBlockPos());
            getLevel().sendBlockUpdated(getBlockPos(), state, state, 3);
        }
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag updateTag = super.getUpdateTag();
        save(updateTag);
        return updateTag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        CompoundTag nbtTag = new CompoundTag();
        save(nbtTag);
        return new ClientboundBlockEntityDataPacket(getBlockPos(), 1, nbtTag);
    }
}
