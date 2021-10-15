package com.direwolf20.buildinggadgets.common.tainted.building.tilesupport;

import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.template.SerialisationSupport;
import com.google.common.base.MoreObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class TileSupport {
    private TileSupport() {
    }

    public static ITileEntityData createTileData(@Nullable BlockEntity be) {
        return dummyTileEntityData();
    }

    public static ITileEntityData createTileData(BlockGetter world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        return createTileData(be);
    }

    public static BlockData createBlockData(BlockState state, @Nullable BlockEntity be) {
        return new BlockData(Objects.requireNonNull(state), createTileData(be));
    }

    public static BlockData createBlockData(BlockGetter world, BlockPos pos) {
        return new BlockData(world.getBlockState(pos), createTileData(world, pos));
    }

    private static final ITileEntityData DUMMY_TILE_ENTITY_DATA = new ITileEntityData() {
        @Override
        public ITileDataSerializer getSerializer() {
            return SerialisationSupport.dummyDataSerializer();
        }

        @Override
        public boolean placeIn(BuildContext context, BlockState state, BlockPos position) {
            return context.getWorld().setBlock(position, state, 0);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .toString();
        }
    };

    public static ITileEntityData dummyTileEntityData() {
        return DUMMY_TILE_ENTITY_DATA;
    }
}
