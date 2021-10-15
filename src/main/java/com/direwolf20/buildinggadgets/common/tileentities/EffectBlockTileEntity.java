package com.direwolf20.buildinggadgets.common.tileentities;

import com.direwolf20.buildinggadgets.common.blocks.EffectBlock.Mode;
import com.direwolf20.buildinggadgets.common.tainted.Tainted;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Tainted(reason = "Used blockData and a stupid non-centralised callback system")
public class EffectBlockTileEntity extends BlockEntity implements BlockEntityClientSerializable {

    private BlockData renderedBlock;
    /**
     * A copy of the target block, used for inheriting data for {@link Mode#REPLACE}
     */
    private BlockData sourceBlock;

    private Mode mode = null;

    private int ticks;

    public EffectBlockTileEntity(BlockPos pos, BlockState state) {
        super(OurTileEntities.EFFECT_BLOCK_TILE_ENTITY, pos, state);
    }

    public void initializeData(BlockState curState, @Nullable BlockEntity be, BlockData replacementBlock, Mode mode) {
        // Minecraft will reuse a tile entity object at a location where the block got removed, but the modification is still buffered, and the block got restored again
        // If we don't reset this here, the 2nd phase of REPLACE will simply finish immediately because the tile entity object is reused
        this.ticks = 0;
        // Again we don't check if the data has been set or not because there is a chance that this tile object gets reused
        this.sourceBlock = replacementBlock;

        this.mode = mode;

        if (mode == Mode.REPLACE) {
            this.setRenderedBlock(TileSupport.createBlockData(curState, be));
        } else {
            this.setRenderedBlock(replacementBlock);
        }
    }

    public static void tick(Level level, BlockPos blockPos, BlockState state, EffectBlockTileEntity blockEntity) {
        if (++blockEntity.ticks >= blockEntity.getLifespan()) {
            blockEntity.complete();
        }
    }

    private void complete() {
        if (level == null || level.isClientSide || mode == null || getRenderedBlock() == null) {
            return;
        }

        mode.onBuilderRemoved(this);
    }

    public BlockData getRenderedBlock() {
        return renderedBlock;
    }

    public void setRenderedBlock(BlockData renderedBlock) {
        this.renderedBlock = renderedBlock;

        if (level instanceof ServerLevel) {
            sync();
        }
    }

    public BlockData getSourceBlock() {
        return sourceBlock;
    }

    public Mode getReplacementMode() {
        return mode;
    }

    public int getTicksExisted() {
        return ticks;
    }

    public int getLifespan() {
        return 20;
    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag compound) {
        if (mode != null && getRenderedBlock() != null && sourceBlock != null) {
            compound.putInt(NBTKeys.GADGET_TICKS, ticks);
            compound.putInt(NBTKeys.GADGET_MODE, mode.ordinal());
            compound.put(NBTKeys.GADGET_REPLACEMENT_BLOCK, getRenderedBlock().serialize(true));
            compound.put(NBTKeys.GADGET_SOURCE_BLOCK, sourceBlock.serialize(true));
        }

        return super.save(compound);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        if (nbt.contains(NBTKeys.GADGET_TICKS, NbtType.INT) &&
            nbt.contains(NBTKeys.GADGET_MODE, NbtType.INT) &&
            nbt.contains(NBTKeys.GADGET_SOURCE_BLOCK, NbtType.COMPOUND) &&
            nbt.contains(NBTKeys.GADGET_REPLACEMENT_BLOCK, NbtType.COMPOUND)) {

            ticks = nbt.getInt(NBTKeys.GADGET_TICKS);
            mode = Mode.values()[nbt.getInt(NBTKeys.GADGET_MODE)];
            setRenderedBlock(BlockData.tryDeserialize(nbt.getCompound(NBTKeys.GADGET_REPLACEMENT_BLOCK), true));
            sourceBlock = BlockData.tryDeserialize(nbt.getCompound(NBTKeys.GADGET_SOURCE_BLOCK), true);
        }
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        return save(tag);
    }
}
