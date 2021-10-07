package com.direwolf20.buildinggadgets.common.tainted.inventory;

import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.util.CommonUtils;
import com.google.common.collect.ImmutableSet;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/*
 * @MichaelHillcox
 *  This entire class could do with some refactoring and cleaning :grin:
 */
public class InventoryHelper {

    private static final Set<Property<?>> UNSAFE_PROPERTIES =
            ImmutableSet.<Property<?>>builder()
                    .add(BlockStateProperties.SOUTH)
                    .add(BlockStateProperties.EAST)
                    .add(BlockStateProperties.WEST)
                    .add(BlockStateProperties.NORTH)
                    .add(BlockStateProperties.UP)
                    .add(BlockStateProperties.DOWN)
                    .build();

    private static final Set<Property<?>> BASE_UNSAFE_PROPERTIES =
            ImmutableSet.<Property<?>>builder()
                    .add(CropBlock.AGE)
                    .add(DoublePlantBlock.HALF)
                    .add(BlockStateProperties.WATERLOGGED)
                    .build();

    public static final CreativeItemIndex CREATIVE_INDEX = new CreativeItemIndex();

    public static IItemIndex index(ItemStack tool, Player player) {
        if (player.isCreative())
            return CREATIVE_INDEX;
        return new PlayerItemIndex(tool, player);
    }

    static Storage<ItemVariant> getHandlers(ItemStack stack, Player player) {
        List<Storage<ItemVariant>> handlers = new ArrayList<>();

        InventoryLinker.getLinkedInventory(player.level, stack).ifPresent(handlers::add);
        handlers.add(PlayerInventoryStorage.of(player));

        return new CombinedStorage<>(handlers);
    }

    public static Optional<BlockData> getSafeBlockData(Player player, BlockPos pos, InteractionHand hand) {
        BlockPlaceContext blockItemUseContext = new BlockPlaceContext(new UseOnContext(player, hand, CommonUtils.fakeRayTrace(player.position(), pos)));
        return getSafeBlockData(player, pos, blockItemUseContext);
    }

    public static Optional<BlockData> getSafeBlockData(Player player, BlockPos pos, BlockPlaceContext useContext) {
        Level world = player.level;
        boolean isCopyPasteGadget = (AbstractGadget.getGadget(player).getItem() instanceof GadgetCopyPaste);
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof LiquidBlock)
            return Optional.empty();

        // Support doors
        if (state.getBlock() instanceof DoorBlock && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
            return Optional.empty();
        }

        BlockState placeState = state.getBlock().defaultBlockState();
        for (Property<?> prop : placeState.getProperties()) {
            if (BASE_UNSAFE_PROPERTIES.contains(prop) || !isCopyPasteGadget && UNSAFE_PROPERTIES.contains(prop)) {
                continue;
            }
            placeState = applyProperty(placeState, state, prop);
        }

        return Optional.of(new BlockData(placeState, TileSupport.createTileData(world, pos)));
    }

    //proper generics...
    private static <T extends Comparable<T>> BlockState applyProperty(BlockState state, BlockState from, Property<T> prop) {
        return state.setValue(prop, from.getValue(prop));
    }
}
