package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.client.screen.GuiMod;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.Region;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.tainted.save.Undo;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.BlockReference.TagReference;
import com.google.common.collect.ImmutableMultiset;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerRegistryImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GadgetDestruction extends AbstractGadget {

    public GadgetDestruction(Properties builder) {
        super(builder, TagReference.WHITELIST_DESTRUCTION, TagReference.BLACKLIST_DESTRUCTION);
    }

    @Override
    public long getEnergyCapacity() {
        return BuildingGadgets.getConfig().gadgets.gadgetDestruction.maxEnergy;
    }

    @Override
    public long getEnergyCost(ItemStack tool) {
        return BuildingGadgets.getConfig().gadgets.gadgetDestruction.energyCost * getCostMultiplier(tool);
    }

    private int getCostMultiplier(ItemStack tool) {
        return (int) (!getFuzzy(tool) ? BuildingGadgets.getConfig().gadgets.gadgetDestruction.nonFuzzyMultiplier : 1);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        addEnergyInformation(tooltip, stack);

        tooltip.add(TooltipTranslation.GADGET_DESTROYWARNING
                .componentTranslation()
                .setStyle(Styles.RED));

        tooltip.add(TooltipTranslation.GADGET_DESTROYSHOWOVERLAY
                .componentTranslation(String.valueOf(getOverlay(stack)))
                .setStyle(Styles.AQUA));

        tooltip.add(TooltipTranslation.GADGET_BUILDING_PLACE_ATOP
                .componentTranslation(String.valueOf(getConnectedArea(stack)))
                .setStyle(Styles.YELLOW));

        if (BuildingGadgets.getConfig().gadgets.gadgetDestruction.nonFuzzyEnabled)
            tooltip.add(TooltipTranslation.GADGET_FUZZY
                    .componentTranslation(String.valueOf(getFuzzy(stack)))
                    .setStyle(Styles.GOLD));

        addInformationRayTraceFluid(tooltip, stack);
    }

    public static void setAnchor(ItemStack stack, BlockPos pos) {
        GadgetUtils.writePOSToNBT(stack, pos, NBTKeys.GADGET_ANCHOR);
    }

    public static void setAnchorSide(ItemStack stack, Direction side) {
        CompoundTag tag = stack.getOrCreateTag();
        if (side == null)
            tag.remove(NBTKeys.GADGET_ANCHOR_SIDE);
        else
            tag.putString(NBTKeys.GADGET_ANCHOR_SIDE, side.getName());
    }

    public static Direction getAnchorSide(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        String facing = tag.getString(NBTKeys.GADGET_ANCHOR_SIDE);
        if (facing.isEmpty())
            return null;
        return Direction.byName(facing);
    }

    public static void setToolValue(ItemStack stack, int value, String valueName) {
        stack.getOrCreateTag().putInt(valueName, value);
    }

    public static int getToolValue(ItemStack stack, String valueName) {
        return stack.getOrCreateTag().getInt(valueName);
    }

    public static boolean getOverlay(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(NBTKeys.GADGET_OVERLAY))
            return tag.getBoolean(NBTKeys.GADGET_OVERLAY);

        tag.putBoolean(NBTKeys.GADGET_OVERLAY, true);
        tag.putBoolean(NBTKeys.GADGET_FUZZY, true);
        stack.setTag(tag);// We want a Destruction Gadget to start with fuzzy=true
        return true;
    }

    public static void setOverlay(ItemStack stack, boolean showOverlay) {
        stack.getOrCreateTag().putBoolean(NBTKeys.GADGET_OVERLAY, showOverlay);
    }

    public static void switchOverlay(Player player, ItemStack stack) {
        boolean newOverlay = !getOverlay(stack);
        setOverlay(stack, newOverlay);
        player.displayClientMessage(TooltipTranslation.GADGET_DESTROYSHOWOVERLAY
                .componentTranslation(newOverlay).setStyle(Styles.AQUA), true);
    }

    public static boolean getIsFluidOnly(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(NBTKeys.GADGET_FLUID_ONLY);
    }

    public static void toggleFluidMode(ItemStack stack) {
        stack.getOrCreateTag().putBoolean(NBTKeys.GADGET_FLUID_ONLY, !getIsFluidOnly(stack));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);

        if (!world.isClientSide) {
            if (!player.isShiftKeyDown()) {
                BlockPos anchorPos = getAnchor(stack);
                Direction anchorSide = getAnchorSide(stack);
                if (anchorPos != null && anchorSide != null) {
                    clearArea(world, anchorPos, anchorSide, (ServerPlayer) player, stack);
                    onAnchorRemoved(stack, player);
                    return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
                }

                BlockHitResult lookingAt = VectorHelper.getLookingAt(player, stack);
                if (!world.isEmptyBlock(lookingAt.getBlockPos())) {
                    clearArea(world, lookingAt.getBlockPos(), lookingAt.getDirection(), (ServerPlayer) player, stack);
                    onAnchorRemoved(stack, player);
                    return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
                }

                return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
            }
        } else if (player.isShiftKeyDown()) {
            GuiMod.DESTRUCTION.openScreen(player);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @Override
    protected void onAnchorSet(ItemStack stack, Player player, BlockHitResult lookingAt) {
        super.onAnchorSet(stack, player, lookingAt);
        setAnchorSide(stack, lookingAt.getDirection());
    }

    @Override
    protected void onAnchorRemoved(ItemStack stack, Player player) {
        super.onAnchorRemoved(stack, player);
        setAnchorSide(stack, null);
    }

    public static List<BlockPos> getArea(Level world, BlockPos pos, Direction incomingSide, Player player, ItemStack stack) {
        ItemStack tool = getGadget(player);
        int depth = getToolValue(stack, NBTKeys.GADGET_VALUE_DEPTH);

        if (tool.isEmpty() || depth == 0 || !player.mayBuild())
            return new ArrayList<>();

        boolean vertical = incomingSide.getAxis().isVertical();
        Direction up = vertical ? player.getDirection() : Direction.UP;
        Direction down = up.getOpposite();
        Direction right = vertical ? up.getClockWise() : incomingSide.getCounterClockWise();
        Direction left = right.getOpposite();

        BlockPos first = pos.relative(left, getToolValue(stack, NBTKeys.GADGET_VALUE_LEFT)).relative(up, getToolValue(stack, NBTKeys.GADGET_VALUE_UP));
        BlockPos second = pos.relative(right, getToolValue(stack, NBTKeys.GADGET_VALUE_RIGHT))
                .relative(down, getToolValue(stack, NBTKeys.GADGET_VALUE_DOWN))
                .relative(incomingSide.getOpposite(), depth - 1);

        boolean isFluidOnly = getIsFluidOnly(stack);
        return new Region(first, second).stream()
                .filter(e ->
                        isFluidOnly
                                ? isFluidBlock(world, e)
                                : isValidBlock(world, e, player, world.getBlockState(e))
                )
                .sorted(Comparator.comparing(player.blockPosition()::distSqr))
                .collect(Collectors.toList());
    }

    public static boolean isFluidBlock(Level world, BlockPos pos) {
        if (world.getFluidState(pos).isEmpty()) {
            return false;
        }

        return FluidRenderHandlerRegistryImpl.INSTANCE.get(world.getFluidState(pos).getType()) != null;
    }

    public static boolean isValidBlock(Level world, BlockPos voidPos, Player player, BlockState currentBlock) {
        if (world.isEmptyBlock(voidPos) ||
            currentBlock.equals(OurBlocks.EFFECT_BLOCK.defaultBlockState()) ||
            currentBlock.getDestroySpeed(world, voidPos) < 0 ||
            !world.mayInteract(player, voidPos)) return false;

        BlockEntity be = world.getBlockEntity(voidPos);
        return (be == null);
    }

    public void clearArea(Level world, BlockPos pos, Direction side, ServerPlayer player, ItemStack stack) {
        List<BlockPos> positions = getArea(world, pos, side, player, stack);
        Undo.Builder builder = Undo.builder();

        for (BlockPos clearPos : positions) {
            BlockState state = world.getBlockState(clearPos);
            BlockEntity be = world.getBlockEntity(clearPos);
            if (!isAllowedBlock(state.getBlock()))
                continue;
            if (be == null) {
                destroyBlock(world, clearPos, player, builder);
            }
        }

        pushUndo(stack, builder.build(world), world);
    }

    private boolean destroyBlock(Level world, BlockPos voidPos, ServerPlayer player, Undo.Builder builder) {
        if (world.isEmptyBlock(voidPos))
            return false;

        ItemStack tool = getGadget(player);
        if (tool.isEmpty())
            return false;

        if (!this.canUse(tool, player))
            return false;

        this.applyDamage(tool, player);
        builder.record(world, voidPos, BlockData.AIR, ImmutableMultiset.of(), ImmutableMultiset.of());
        EffectBlock.spawnEffectBlock(world, voidPos, TileSupport.createBlockData(world, voidPos), EffectBlock.Mode.REMOVE);
        return true;
    }

    public static ItemStack getGadget(Player player) {
        ItemStack stack = AbstractGadget.getGadget(player);
        if (!(stack.getItem() instanceof GadgetDestruction))
            return ItemStack.EMPTY;

        return stack;
    }

}
