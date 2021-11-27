package com.direwolf20.buildinggadgets.common.tainted.building.tilesupport;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.template.SerialisationSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record NBTTileEntityData(@NotNull CompoundTag nbt,
                                @Nullable MaterialList requiredMaterials) implements ITileEntityData {
    public static NBTTileEntityData ofTile(BlockEntity be) {
        CompoundTag nbt = new CompoundTag();
        be.saveWithId();
        return new NBTTileEntityData(nbt, null);
    }

    @Override
    public ITileDataSerializer getSerializer() {
        return SerialisationSupport.nbtTileDataSerializer();
    }

    @Override
    public MaterialList getRequiredItems(BuildContext context, BlockState state, @Nullable HitResult target, @Nullable BlockPos pos) {
        if (requiredMaterials != null)
            return requiredMaterials;
        return ITileEntityData.super.getRequiredItems(context, state, target, pos);
    }

    @Override
    public boolean placeIn(BuildContext context, BlockState state, BlockPos position) {
        BuildingGadgets.LOG.trace("Placing {} with Tile NBT at {}.", state, position);
        context.getWorld().setBlock(position, state, 0);
        BlockEntity be = context.getWorld().getBlockEntity(position);
        if (be != null) {
            try {
                be.load(getNBTModifiable());
            } catch (Exception e) {
                BuildingGadgets.LOG.debug("Failed to apply Tile NBT Data to {} at {} in Context {}", state, position, context, e);
            }
        }
        return true;
    }

    public CompoundTag getNBT() {
        return nbt.copy();
    }

    @Nullable
    public MaterialList getRequiredMaterials() {
        return requiredMaterials;
    }

    private CompoundTag getNBTModifiable() {
        return nbt;
    }
}
