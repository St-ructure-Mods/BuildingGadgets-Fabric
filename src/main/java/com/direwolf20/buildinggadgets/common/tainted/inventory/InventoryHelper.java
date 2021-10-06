package com.direwolf20.buildinggadgets.common.tainted.inventory;

import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.tainted.inventory.handle.IObjectHandle;
import com.direwolf20.buildinggadgets.common.util.CommonUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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

import java.util.*;

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

    static List<IInsertProvider> indexInsertProviders(ItemStack tool, Player player) {
        ImmutableList.Builder<IInsertProvider> builder = ImmutableList.builder();

        for (Storage<ItemVariant> storage : getHandlers(tool, player)) {
            builder.add((stack, count, simulate) -> {
                try (Transaction transaction = Transaction.openOuter()) {
                    return (int) storage.insert(ItemVariant.of(stack), count, transaction);
                }
            });
        }

        return builder.build();
    }

    static Map<Class<?>, Map<Object, List<IObjectHandle>>> indexMap(ItemStack tool, Player player) {
        Map<Class<?>, Map<Object, List<IObjectHandle>>> map = new HashMap<>();

        for (Storage<ItemVariant> handler : getHandlers(tool, player)) {
            // TODO: Index
        }

        return map;
    }

    static List<Storage<ItemVariant>> getHandlers(ItemStack stack, Player player) {
        List<Storage<ItemVariant>> handlers = new ArrayList<>();

        InventoryLinker.getLinkedInventory(player.level, stack).ifPresent(handlers::add);
        handlers.add(PlayerInventoryStorage.of(player));

        return handlers;
    }

    public static void registerHandleProviders() {
        // InterModComms.sendTo(Reference.MODID, Reference.HandleProviderReference.IMC_METHOD_HANDLE_PROVIDER, () -> (Supplier<TopologicalRegistryBuilder<IHandleProvider>>)
        //         (() -> TopologicalRegistryBuilder.<IHandleProvider>create()
        //                 .addMarker(Reference.MARKER_AFTER_RL)
        //                 .addValue(Reference.HandleProviderReference.STACK_HANDLER_ITEM_HANDLE_RL, new ItemHandlerProvider())
        //                 .addDependency(Reference.HandleProviderReference.STACK_HANDLER_ITEM_HANDLE_RL, Reference.MARKER_AFTER_RL)));
    }

    public static boolean giveItem(ItemStack itemStack, Player player, Level world) {
        if (player.isCreative()) {
            return true;
        }
        if (itemStack.getCount() == 0) {
            return true;
        }

        //Fill any unfilled stacks in the player's inventory first
        Inventory inv = player.getInventory();
        List<Integer> slots = findItem(itemStack.getItem(), inv);
        for (int slot : slots) {
            ItemStack stackInSlot = inv.getItem(slot);
            if (stackInSlot.getCount() < stackInSlot.getItem().getMaxStackSize()) {
                ItemStack giveItemStack = itemStack.copy();
                boolean success = inv.add(giveItemStack);
                if (success) return true;
            }
        }

        //Try to insert into the remote inventory.
        ItemStack tool = AbstractGadget.getGadget(player);
        Optional<Storage<ItemVariant>> linkedInventory = InventoryLinker.getLinkedInventory(world, tool);

        if (linkedInventory.isPresent()) {
            Storage<ItemVariant> storage = linkedInventory.get();
            ItemVariant variant = ItemVariant.of(itemStack);

            try (Transaction transaction = Transaction.openOuter()) {
                itemStack.setCount((int) storage.insert(variant, itemStack.getCount(), transaction));

                if (itemStack.isEmpty()) {
                    return true;
                }
            }
        }

        // TODO: AFAIK there's no API for interacting with items storing items in 1.16 fabric
        // List<ItemStack> invContainers = findInvContainers(inv);
        // if (invContainers.size() > 0) {
        //     for (ItemStack stack : invContainers) {
        //         Container container = (Container) stack.getItem();
        //         for (int i = 0; i < container.getContainerSize(); i++) {
        //             ItemStack containerItem = container.getItem(i);
        //             ItemStack giveItemStack = itemStack.copy();
        //             if (containerItem.getItem() == giveItemStack.getItem()) {
        //                 giveItemStack = container.insertItem(i, giveItemStack, false);
        //                 if (giveItemStack.isEmpty())
        //                     return true;

        //                 itemStack = giveItemStack.copy();
        //             }
        //         }
        //     }
        // }

        ItemStack giveItemStack = itemStack.copy();
        return inv.add(giveItemStack);
    }


    private static List<ItemStack> findInvContainers(Inventory inv) {
        List<ItemStack> containers = new ArrayList<>();

        for (int i = 0; i < 36; ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof Container) {
                containers.add(stack);
            }
        }

        return containers;
    }

    private static List<Integer> findItem(Item item, Inventory inv) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 36; ++i) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                slots.add(i);
            }
        }
        return slots;
    }

    public static List<Integer> findItemClass(Class<?> c, Inventory inv) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 36; ++i) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && c.isInstance(stack.getItem())) {
                slots.add(i);
            }
        }
        return slots;
    }

    public static ItemStack getSilkTouchDrop(BlockState state) {
        return new ItemStack(state.getBlock());
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
