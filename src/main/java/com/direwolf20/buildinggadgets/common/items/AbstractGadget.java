package com.direwolf20.buildinggadgets.common.items;


import com.direwolf20.buildinggadgets.client.renders.BaseRenderer;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.commands.ForceUnloadedCommand;
import com.direwolf20.buildinggadgets.common.items.modes.*;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.concurrent.UndoScheduler;
import com.direwolf20.buildinggadgets.common.tainted.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.tainted.save.SaveManager;
import com.direwolf20.buildinggadgets.common.tainted.save.Undo;
import com.direwolf20.buildinggadgets.common.tainted.save.UndoWorldSave;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.collect.ImmutableSortedSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.Tag;
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
import java.util.function.Supplier;

import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.withSuffix;

public abstract class AbstractGadget extends Item implements SimpleBatteryItem {
    private final BaseRenderer renderer;
    private final Tag.Named<Block> whiteList;
    private final Tag.Named<Block> blackList;
    private final Supplier<UndoWorldSave> saveSupplier;

    public AbstractGadget(Properties builder, int undoLength, String undoName, ResourceLocation whiteListTag, ResourceLocation blackListTag) {
        super(builder.defaultDurability(0));

        renderer = createRenderFactory().get();
        this.whiteList = TagFactory.BLOCK.create(whiteListTag);
        this.blackList = TagFactory.BLOCK.create(blackListTag);
        saveSupplier = SaveManager.INSTANCE.registerUndoSave(w -> SaveManager.getUndoSave(w, undoLength, undoName));
    }

    public abstract long getEnergyCapacity();

    public abstract long getEnergyCost(ItemStack tool);

    @Override
    public long getEnergyMaxInput() {
        return 10000;
    }

    @Override
    public long getEnergyMaxOutput() {
        return 0;
    }

    public Tag.Named<Block> getWhiteList() {
        return whiteList;
    }

    public Tag.Named<Block> getBlackList() {
        return blackList;
    }

    @Environment(EnvType.CLIENT)
    public BaseRenderer getRender() {
        return renderer;
    }

    @Environment(EnvType.CLIENT)
    protected abstract Supplier<BaseRenderer> createRenderFactory();

    protected UndoWorldSave getUndoSave() {
        return saveSupplier.get();
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
        if (getWhiteList().getValues().isEmpty()) {
            return !getBlackList().contains(block);
        }
        return getWhiteList().contains(block);
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

    public void applyDamage(ItemStack tool, ServerPlayer player) {
        if (player.isCreative() || this.getEnergyCapacity() == 0) {
            return;
        }

        ((AbstractGadget) tool.getItem()).tryUseEnergy(tool, getEnergyCost(tool));
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
        if (!nbt.hasUUID(NBTKeys.GADGET_UUID)) {
            UUID newId = getUndoSave().getFreeUUID();
            nbt.putUUID(NBTKeys.GADGET_UUID, newId);
            return newId;
        }
        return nbt.getUUID(NBTKeys.GADGET_UUID);
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

    protected void pushUndo(ItemStack stack, Undo undo) {
        // Don't save if there is nothing to undo...
        if (undo.getUndoData().isEmpty()) {
            return;
        }

        UndoWorldSave save = getUndoSave();
        save.insertUndo(getUUID(stack), undo);
    }

    public void undo(Level world, Player player, ItemStack stack) {
        UndoWorldSave save = getUndoSave();
        Optional<Undo> undoOptional = save.getUndo(getUUID(stack));

        if (undoOptional.isPresent()) {
            Undo undo = undoOptional.get();
            IItemIndex index = InventoryHelper.index(stack, player);
            if (!ForceUnloadedCommand.mayForceUnloadedChunks(player)) {//TODO separate command
                ImmutableSortedSet<ChunkPos> unloadedChunks = undo.getBoundingBox().getUnloadedChunks(world);
                if (!unloadedChunks.isEmpty()) {
                    pushUndo(stack, undo);
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

            UndoScheduler.scheduleUndo(undo, index, buildContext, BuildingGadgets.getConfig().GADGETS.placeSteps);
        } else {
            player.displayClientMessage(MessageTranslation.NOTHING_TO_UNDO.componentTranslation().setStyle(Styles.RED), true);
        }
    }
}
