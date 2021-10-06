package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.client.renders.BaseRenderer;
import com.direwolf20.buildinggadgets.client.renders.BuildRender;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.items.modes.AbstractMode;
import com.direwolf20.buildinggadgets.common.items.modes.ExchangingModes;
import com.direwolf20.buildinggadgets.common.network.C2S.PacketBindTool;
import com.direwolf20.buildinggadgets.common.network.C2S.PacketRotateMirror;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileEntityData;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.tainted.inventory.MatchResult;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.LangUtil;
import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.BlockReference.TagReference;
import com.direwolf20.buildinggadgets.common.world.MockBuilderWorld;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import dev.architectury.event.events.common.BlockEvent;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.*;

public class GadgetExchanger extends AbstractGadget {
    private static final MockBuilderWorld fakeWorld = new MockBuilderWorld();

    public GadgetExchanger() {
        super(OurItems.nonStackableItemProperties(),
                0,
                "",
                TagReference.WHITELIST_EXCHANGING,
                TagReference.BLACKLIST_EXCHANGING);
    }

    @Override
    public long getEnergyCapacity() {
        return BuildingGadgets.config.GADGETS.GADGET_EXCHANGER.maxEnergy;
    }

    @Override
    public long getEnergyCost(ItemStack tool) {
        return BuildingGadgets.config.GADGETS.GADGET_EXCHANGER.energyCost;
    }

    @Override
    protected Supplier<BaseRenderer> createRenderFactory() {
        return () -> new BuildRender(true);
    }

    @Override
    public int getEnchantmentValue() {
        return 3;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

//    @Override
//    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
//        return EnchantmentHelper.getEnchantments(book).containsKey(Enchantments.SILK_TOUCH) || super.isBookEnchantable(stack, book);
//    }
//
//    @Override
//    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
//        return enchantment == Enchantments.SILK_TOUCH || super.canApplyAtEnchantingTable(stack, enchantment);
//    }

    private static void setToolMode(ItemStack tool, ExchangingModes mode) {
        //Store the tool's mode in NBT as a string
        CompoundTag tagCompound = tool.getOrCreateTag();
        tagCompound.putString("mode", mode.toString());
        tool.setTag(tagCompound);
    }

    public static ExchangingModes getToolMode(ItemStack tool) {
        CompoundTag tagCompound = tool.getOrCreateTag();
        return ExchangingModes.getFromName(tagCompound.getString("mode"));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        addEnergyInformation(tooltip, stack);

        ExchangingModes mode = getToolMode(stack);
        tooltip.add(TooltipTranslation.GADGET_MODE
                .componentTranslation((mode == ExchangingModes.SURFACE && getConnectedArea(stack)
                        ? TooltipTranslation.GADGET_CONNECTED.format(new TranslatableComponent(mode.getTranslationKey()).getString())
                        : new TranslatableComponent(mode.getTranslationKey())))
                .setStyle(Styles.AQUA));

        tooltip.add(TooltipTranslation.GADGET_BLOCK
                .componentTranslation(LangUtil.getFormattedBlockName(getToolBlock(stack).getState()))
                .setStyle(Styles.DK_GREEN));

        int range = getToolRange(stack);
        tooltip.add(TooltipTranslation.GADGET_RANGE
                .componentTranslation(range, getRangeInBlocks(range, mode.getMode()))
                .setStyle(Styles.LT_PURPLE));

        tooltip.add(TooltipTranslation.GADGET_FUZZY
                .componentTranslation(String.valueOf(getFuzzy(stack)))
                .setStyle(Styles.GOLD));

        addInformationRayTraceFluid(tooltip, stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        if (!world.isClientSide) {
            if (player.isShiftKeyDown()) {
                InteractionResultHolder<Block> result = selectBlock(itemstack, player);
                if (!result.getResult().consumesAction()) {
                    player.displayClientMessage(MessageTranslation.INVALID_BLOCK.componentTranslation(Registry.BLOCK.getKey(result.getObject())).setStyle(Styles.AQUA), true);
                    return super.use(world, player, hand);
                }
            } else if (player instanceof ServerPlayer) {
                try(Transaction transaction = Transaction.openOuter()) {
                    exchange((ServerPlayer) player, itemstack, transaction);
                    transaction.commit();
                }
            }
        } else {
            if (!player.isShiftKeyDown()) {
                BaseRenderer.updateInventoryCache();
            } else {
                if (Screen.hasControlDown()) {
                    PacketBindTool.send();
                }
            }
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
    }

    public void setMode(ItemStack heldItem, int modeInt) {
        //Called when we specify a mode with the radial menu
        ExchangingModes mode = ExchangingModes.values()[modeInt];
        setToolMode(heldItem, mode);
    }

    public static void rangeChange(Player player, ItemStack heldItem) {
        int range = getToolRange(heldItem);
        int changeAmount = (getToolMode(heldItem) == ExchangingModes.GRID || (range % 2 == 0)) ? 1 : 2;
        if (player.isShiftKeyDown()) {
            range = (range <= 1) ? BuildingGadgets.config.GADGETS.maxRange : range - changeAmount;
        } else {
            range = (range >= BuildingGadgets.config.GADGETS.maxRange) ? 1 : range + changeAmount;
        }
        setToolRange(heldItem, range);
        player.displayClientMessage(MessageTranslation.RANGE_SET.componentTranslation(range).setStyle(Styles.AQUA), true);
    }

    private void exchange(ServerPlayer player, ItemStack stack, TransactionContext transactionContext) {
        ServerLevel world = player.getLevel();
        ItemStack heldItem = getGadget(player);
        if (heldItem.isEmpty())
            return;

        BlockData blockData = getToolBlock(heldItem);

        // Don't attempt to do anything if we can't actually do it.
        BlockHitResult lookingAt = VectorHelper.getLookingAt(player, stack);
        BlockEntity tileEntity = world.getBlockEntity(lookingAt.getBlockPos());
        BlockState lookingAtState = player.level.getBlockState(lookingAt.getBlockPos());
        Block lookAtBlock = lookingAtState.getBlock();
        if (blockData.getState() == Blocks.AIR.defaultBlockState()
                || lookAtBlock == OurBlocks.EFFECT_BLOCK
                || blockData.getState() == lookingAtState
                || tileEntity != null) {
            return;
        }

        // Get the anchor or build the collection
        Optional<List<BlockPos>> anchor = GadgetUtils.getAnchor(stack);
        List<BlockPos> coords = anchor.orElseGet(
                () -> getToolMode(stack).getMode().getCollection(new AbstractMode.UseContext(world, blockData.getState(), lookingAt.getBlockPos(), heldItem, lookingAt.getDirection(), getConnectedArea(heldItem)), player)
        );

        if (anchor.isPresent()) {
            setAnchor(stack); // Remove the anchor
        }

        IItemIndex index = InventoryHelper.index(stack, player);

        //TODO replace fakeWorld
        fakeWorld.setWorldAndState(player.level, blockData.getState(), coords); // Initialize the fake world's blocks
        for (BlockPos coordinate : coords) {
            //Get the extended block state in the fake world Disabled to fix Chisel
            //state = state.getBlock().getExtendedState(state, fakeWorld, coordinate);

            exchangeBlock(world, player, index, coordinate, blockData, transactionContext);
        }
    }

    private void exchangeBlock(ServerLevel world, ServerPlayer player, IItemIndex index, BlockPos pos, BlockData setBlock, TransactionContext transaction) {
        BlockState currentBlock = world.getBlockState(pos);
        ITileEntityData data;

        BlockEntity be = world.getBlockEntity(pos);
        data = TileSupport.createTileData(world, pos);

        ItemStack tool = getGadget(player);
        if (tool.isEmpty() || !this.canUse(tool, player))
            return;

        BuildContext buildContext = BuildContext.builder()
                .stack(tool)
                .player(player)
                .build(world);

        MaterialList requiredItems = setBlock.getRequiredItems(buildContext, null, pos);
        MatchResult match = index.tryMatch(requiredItems);
        if (!match.isSuccess()) {
            if (setBlock.getState().hasBlockEntity())
                return;
        }

        if (!player.mayBuild() || !world.mayInteract(player, pos))
            return;

        if (!world.mayInteract(player, pos)) {
            return;
        }

        // Should be equivalent to the above check?
        // BlockSnapshot blockSnapshot = BlockSnapshot.create(world.dimension(), world, pos);
        // BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, pos, currentBlock, player);
        // if (ForgeEventFactory.onBlockPlace(player, blockSnapshot, Direction.UP) || MinecraftForge.EVENT_BUS.post(e))
        //     return;

        this.applyDamage(tool, player);

        if (index.applyMatch(match)) {
            MaterialList materials = data.getRequiredItems(
                    buildContext,
                    currentBlock,
                    world.clip(new ClipContext(player.position(), Vec3.atLowerCornerOf(pos), ClipContext.Block.COLLIDER, Fluid.NONE, player)),
                    pos);

            Iterator<ImmutableMultiset<ItemVariant>> it = materials.iterator();
            Multiset<ItemVariant> producedItems = LinkedHashMultiset.create();

            if (buildContext.getStack().isEnchanted() && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, buildContext.getStack()) > 0) {
                producedItems = it.hasNext() ? it.next() : ImmutableMultiset.of();
            } else {
                List<ItemStack> drops = Block.getDrops(currentBlock, (ServerLevel) buildContext.getWorld(), pos, buildContext.getWorld().getBlockEntity(pos));
                producedItems.addAll(drops.stream().map(ItemVariant::of).collect(Collectors.toList()));
            }
                index.insert(producedItems, transaction);

            EffectBlock.spawnEffectBlock(world, pos, setBlock, EffectBlock.Mode.REPLACE);
        }
    }

    public static ItemStack getGadget(Player player) {
        ItemStack stack = AbstractGadget.getGadget(player);
        if (!(stack.getItem() instanceof GadgetExchanger))
            return ItemStack.EMPTY;

        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 20;
    }

    @Override
    public boolean performRotate(ItemStack stack, Player player) {
        GadgetUtils.rotateOrMirrorToolBlock(stack, player, PacketRotateMirror.Operation.ROTATE);
        return true;
    }

    @Override
    public boolean performMirror(ItemStack stack, Player player) {
        GadgetUtils.rotateOrMirrorToolBlock(stack, player, PacketRotateMirror.Operation.MIRROR);
        return true;
    }
}
