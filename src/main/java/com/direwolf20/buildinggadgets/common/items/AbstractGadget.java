package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.commands.ForceUnloadedCommand;
import com.direwolf20.buildinggadgets.common.compat.FLANCompat;
import com.direwolf20.buildinggadgets.common.compat.FTBChunksCompat;
import com.direwolf20.buildinggadgets.common.compat.GOMLCompat;
import com.direwolf20.buildinggadgets.common.component.BGComponent;
import com.direwolf20.buildinggadgets.common.items.modes.*;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.concurrent.UndoScheduler;
import com.direwolf20.buildinggadgets.common.tainted.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.tainted.save.Undo;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.collect.ImmutableSortedSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.base.SimpleBatteryItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.withSuffix;

public abstract class AbstractGadget extends Item implements SimpleBatteryItem {

    private final TagKey<Block> whiteList;
    private final TagKey<Block> blackList;

    public AbstractGadget(Properties builder, ResourceLocation whiteListTag, ResourceLocation blackListTag) {
        super(builder.defaultDurability(0));

        this.whiteList = TagKey.create(Registry.BLOCK_REGISTRY, whiteListTag);
        this.blackList = TagKey.create(Registry.BLOCK_REGISTRY, blackListTag);
    }

    public abstract long getEnergyCapacity();

    public abstract long getEnergyCost(ItemStack tool);

    @Override
    public int getBarColor(ItemStack itemStack) {
        float f = getBarWidth(itemStack) / 13.0F;
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isBarVisible(ItemStack itemStack) {
        if(getEnergyCapacity() <= 0)
            return false;
        return (getEnergyCapacity() != getStoredEnergy(itemStack));
    }

    @Override
    public int getBarWidth(ItemStack itemStack) {
        return (int) (13.0F - (getEnergyCapacity() - (float) getStoredEnergy(itemStack)) * 13.0F / (float) getEnergyCapacity());
    }

    @Override
    public long getEnergyMaxInput() {
        return 10000;
    }

    @Override
    public long getEnergyMaxOutput() {
        return 0;
    }

    public TagKey<Block> getWhiteList() {
        return whiteList;
    }

    public TagKey<Block> getBlackList() {
        return blackList;
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        super.fillItemCategory(group, items);
        if (!allowdedIn(group)) {
            return;
        }

        ItemStack charged = new ItemStack(this);
        charged.getOrCreateTag().putDouble(NBTKeys.ENERGY, this.getEnergyCapacity());
        items.add(charged);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.getItem() instanceof AbstractGadget && repair.getItem() == Items.DIAMOND;
    }

    public boolean isAllowedBlock(Block block) {
        if(!Registry.BLOCK.getTagOrEmpty(getWhiteList()).iterator().hasNext()) {
            return !block.builtInRegistryHolder().is(getBlackList());
        }
        return block.builtInRegistryHolder().is(getWhiteList());
    }

    public static ItemStack getGadget(Player player) {
        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof AbstractGadget)) {
            heldItem = player.getOffhandItem();
            if (!(heldItem.getItem() instanceof AbstractGadget)) {
                return ItemStack.EMPTY;
            }
        }
        return heldItem;
    }

    public boolean canUse(ItemStack tool, Player player) {
        if (player.isCreative() || this.getEnergyCapacity() == 0) {
            return true;
        }

        return getEnergyCost(tool) <= SimpleBatteryItem.getStoredEnergyUnchecked(tool);
    }

    public boolean useEnergy(ItemStack tool, ServerPlayer player) {
        if (player.isCreative() || this.getEnergyCapacity() == 0) {
            return true;
        }

        return ((AbstractGadget) tool.getItem()).tryUseEnergy(tool, getEnergyCost(tool));
    }

    protected void addEnergyInformation(List<Component> tooltip, ItemStack stack) {
        if (this.getEnergyCapacity() == 0) {
            return;
        }

        if (stack.getItem() instanceof SimpleBatteryItem) {
            tooltip.add(TooltipTranslation.GADGET_ENERGY
                    .componentTranslation(withSuffix((int) getStoredEnergy(stack)), withSuffix((int) getEnergyCapacity()))
                    .setStyle(Styles.GRAY));
        }
    }

    public final void onRotate(ItemStack stack, Player player) {
        if (performRotate(stack, player)) {
            player.displayClientMessage(MessageTranslation.ROTATED.componentTranslation().setStyle(Styles.AQUA), true);
        }
    }

    protected boolean performRotate(ItemStack stack, Player player) {
        return false;
    }

    public final void onMirror(ItemStack stack, Player player) {
        if (performMirror(stack, player)) {
            player.displayClientMessage(MessageTranslation.MIRRORED.componentTranslation().setStyle(Styles.AQUA), true);
        }
    }

    protected boolean performMirror(ItemStack stack, Player player) {
        return false;
    }

    public final void onAnchor(ItemStack stack, Player player) {
        if (getAnchor(stack) == null) {
            BlockHitResult lookingAt = VectorHelper.getLookingAt(player, stack);
            if ((player.level.isEmptyBlock(lookingAt.getBlockPos()))) {
                return;
            }
            onAnchorSet(stack, player, lookingAt);
            player.displayClientMessage(MessageTranslation.ANCHOR_SET.componentTranslation().setStyle(Styles.AQUA), true);
        } else {
            onAnchorRemoved(stack, player);
            player.displayClientMessage(MessageTranslation.ANCHOR_REMOVED.componentTranslation().setStyle(Styles.AQUA), true);
        }
    }

    protected void onAnchorSet(ItemStack stack, Player player, BlockHitResult lookingAt) {
        GadgetUtils.writePOSToNBT(stack, lookingAt.getBlockPos(), NBTKeys.GADGET_ANCHOR);
    }

    protected void onAnchorRemoved(ItemStack stack, Player player) {
        stack.getOrCreateTag().remove(NBTKeys.GADGET_ANCHOR);
    }

    @Nullable
    public BlockPos getAnchor(ItemStack stack) {
        return GadgetUtils.getPOSFromNBT(stack, NBTKeys.GADGET_ANCHOR);
    }

    public static boolean getFuzzy(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(NBTKeys.GADGET_FUZZY);
    }

    public static void toggleFuzzy(Player player, ItemStack stack) {
        stack.getOrCreateTag().putBoolean(NBTKeys.GADGET_FUZZY, !getFuzzy(stack));
        player.displayClientMessage(MessageTranslation.FUZZY_MODE.componentTranslation(getFuzzy(stack)).setStyle(Styles.AQUA), true);
    }

    public static boolean getConnectedArea(ItemStack stack) {
        return !stack.getOrCreateTag().getBoolean(NBTKeys.GADGET_UNCONNECTED_AREA);
    }

    public static void toggleConnectedArea(Player player, ItemStack stack) {
        stack.getOrCreateTag().putBoolean(NBTKeys.GADGET_UNCONNECTED_AREA, getConnectedArea(stack));
        player.displayClientMessage((stack.getItem() instanceof GadgetDestruction ? MessageTranslation.CONNECTED_AREA : MessageTranslation.CONNECTED_SURFACE)
                .componentTranslation(getConnectedArea(stack)).setStyle(Styles.AQUA), true);
    }

    public static boolean shouldRayTraceFluid(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(NBTKeys.GADGET_RAYTRACE_FLUID);
    }

    public static void toggleRayTraceFluid(ServerPlayer player, ItemStack stack) {
        stack.getOrCreateTag().putBoolean(NBTKeys.GADGET_RAYTRACE_FLUID, !shouldRayTraceFluid(stack));
        player.displayClientMessage(MessageTranslation.RAYTRACE_FLUID.componentTranslation(shouldRayTraceFluid(stack)).setStyle(Styles.AQUA), true);
    }

    public static void addInformationRayTraceFluid(List<Component> tooltip, ItemStack stack) {
        tooltip.add(TooltipTranslation.GADGET_RAYTRACE_FLUID
                .componentTranslation(String.valueOf(shouldRayTraceFluid(stack)))
                .setStyle(Styles.BLUE));
    }

    //this should only be called Server-Side!!!
    public UUID getUUID(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();

        if (nbt.hasUUID(NBTKeys.GADGET_UUID)) {
            return nbt.getUUID(NBTKeys.GADGET_UUID);
        } else {
            UUID newId = UUID.randomUUID();
            nbt.putUUID(NBTKeys.GADGET_UUID, newId);
            return newId;
        }
    }

    // Todo: tweak and fix.
    public static int getRangeInBlocks(int range, AbstractMode mode) {
        if (mode instanceof StairMode ||
            mode instanceof VerticalColumnMode ||
            mode instanceof HorizontalColumnMode) {
            return range;
        }

        if (mode instanceof GridMode) {
            return range < 7 ? 9 : range < 13 ? 11 * 11 : 19 * 19;
        }

        return range == 1 ? 1 : (range + 1) * (range + 1);
    }

    protected void pushUndo(ItemStack stack, Undo undo, Level world) {
        // Don't save if there is nothing to undo...
        if (undo.getUndoData().isEmpty()) {
            return;
        }

        BGComponent.UNDO_COMPONENT.get(world.getLevelData()).insertUndo(getUUID(stack), undo);
    }

    public void undo(Level world, Player player, ItemStack stack) {
        Optional<Undo> undoOptional = BGComponent.UNDO_COMPONENT.get(world.getLevelData()).getUndo(getUUID(stack));

        if (undoOptional.isPresent()) {
            Undo undo = undoOptional.get();
            IItemIndex index = InventoryHelper.index(stack, player);
            if (!ForceUnloadedCommand.mayForceUnloadedChunks(player)) {//TODO separate command
                ImmutableSortedSet<ChunkPos> unloadedChunks = undo.getBoundingBox().getUnloadedChunks(world);
                if (!unloadedChunks.isEmpty()) {
                    pushUndo(stack, undo, world);
                    player.displayClientMessage(MessageTranslation.UNDO_UNLOADED.componentTranslation().setStyle(Styles.RED), true);
                    BuildingGadgets.LOG.error("Player attempted to undo a Region missing {} unloaded chunks. Denied undo!", unloadedChunks.size());
                    BuildingGadgets.LOG.trace("The following chunks were detected as unloaded {}.", unloadedChunks);
                    return;
                }
            }
            BuildContext buildContext = BuildContext.builder()
                    .player(player)
                    .stack(stack)
                    .build(world);

            UndoScheduler.scheduleUndo(undo, index, buildContext, BuildingGadgets.getConfig().gadgets.placeSteps);
        } else {
            player.displayClientMessage(MessageTranslation.NOTHING_TO_UNDO.componentTranslation().setStyle(Styles.RED), true);
        }
    }

    protected static boolean mayInteract(ServerPlayer player, BlockPos pos) {
        return player.mayInteract(player.level, pos) && GOMLCompat.canUse(player, pos) && FLANCompat.canUse(player, pos) && FTBChunksCompat.canUse(player, pos);
    }
}
