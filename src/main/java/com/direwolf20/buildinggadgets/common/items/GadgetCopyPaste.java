package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.client.renders.BaseRenderer;
import com.direwolf20.buildinggadgets.client.screen.GuiMod;
import com.direwolf20.buildinggadgets.client.screen.tooltip.TemplateData;
import com.direwolf20.buildinggadgets.client.screen.tooltip.TemplateTooltip;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.commands.ForceUnloadedCommand;
import com.direwolf20.buildinggadgets.common.commands.OverrideBuildSizeCommand;
import com.direwolf20.buildinggadgets.common.commands.OverrideCopySizeCommand;
import com.direwolf20.buildinggadgets.common.component.BGComponent;
import com.direwolf20.buildinggadgets.common.network.C2S.PacketBindTool;
import com.direwolf20.buildinggadgets.common.network.S2C.LookupResult;
import com.direwolf20.buildinggadgets.common.network.Target;
import com.direwolf20.buildinggadgets.common.tainted.building.PlacementChecker;
import com.direwolf20.buildinggadgets.common.tainted.building.Region;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.building.view.IBuildView;
import com.direwolf20.buildinggadgets.common.tainted.building.view.WorldBuildView;
import com.direwolf20.buildinggadgets.common.tainted.concurrent.CopyScheduler;
import com.direwolf20.buildinggadgets.common.tainted.concurrent.PlacementScheduler;
import com.direwolf20.buildinggadgets.common.tainted.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider;
import com.direwolf20.buildinggadgets.common.tainted.template.Template;
import com.direwolf20.buildinggadgets.common.tainted.template.TemplateHeader;
import com.direwolf20.buildinggadgets.common.util.Additions;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.*;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.TagReference;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSortedSet;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.List;
import java.util.Optional;

public class GadgetCopyPaste extends AbstractGadget {

    public enum ToolMode {
        COPY(ModeTranslation.COPY),
        PASTE(ModeTranslation.PASTE);

        private final ITranslationProvider translation;

        ToolMode(ITranslationProvider translation) {
            this.translation = translation;
        }

        public ITranslationProvider getTranslation() {
            return translation;
        }
    }

    private static final Joiner CHUNK_JOINER = Joiner.on("; ");

    public GadgetCopyPaste(Properties builder) {
        super(builder, TagReference.WHITELIST_COPY_PASTE, TagReference.BLACKLIST_COPY_PASTE);
    }

    @Override
    public long getEnergyCapacity() {
        return BuildingGadgets.getConfig().gadgets.gadgetCopyPaste.maxEnergy;
    }

    @Override
    public long getEnergyCost(ItemStack tool) {
        return BuildingGadgets.getConfig().gadgets.gadgetCopyPaste.energyCost;
    }


    @Override
    public long getEnergyMaxOutput() {
        return 10000;
    }

    @Override
    public boolean performRotate(ItemStack stack, Player player) {
        return BGComponent.TEMPLATE_PROVIDER_COMPONENT.maybeGet(player.level).flatMap(provider ->
                BGComponent.TEMPLATE_KEY_COMPONENT.maybeGet(stack).map(key -> {
                    Template template = provider.getTemplateForKey(key);
                    provider.setTemplate(key, template.rotate(Rotation.CLOCKWISE_90));
                    provider.requestRemoteUpdate(key, new Target(PacketFlow.CLIENTBOUND, (ServerPlayer) player));
                    return true;
                })).orElse(false);
    }

    @Override
    public boolean performMirror(ItemStack stack, Player player) {
        return BGComponent.TEMPLATE_PROVIDER_COMPONENT.maybeGet(player.level).flatMap(provider ->
                BGComponent.TEMPLATE_KEY_COMPONENT.maybeGet(stack).map(key -> {
                    Template template = provider.getTemplateForKey(key);
                    provider.setTemplate(key, template.mirror(player.getDirection().getAxis()));
                    provider.requestRemoteUpdate(key, new Target(PacketFlow.CLIENTBOUND, (ServerPlayer) player));
                    return true;
                })).orElse(false);
    }

    public static void setRelativeVector(ItemStack stack, BlockPos vec) {
        CompoundTag nbt = stack.getOrCreateTag();

        if (vec.equals(BlockPos.ZERO)) {
            nbt.remove(NBTKeys.GADGET_REL_POS);
        } else {
            nbt.put(NBTKeys.GADGET_REL_POS, NbtUtils.writeBlockPos(vec));
        }
    }

    public static BlockPos getRelativeVector(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        // If not present, then this will just return (0, 0, 0)
        return NbtUtils.readBlockPos(nbt.getCompound(NBTKeys.GADGET_REL_POS));
    }

    public static int getAndIncrementCopyCounter(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        int count = nbt.getInt(NBTKeys.TEMPLATE_COPY_COUNT); // Returns 0 if not present
        nbt.putInt(NBTKeys.TEMPLATE_COPY_COUNT, count + 1);
        return count;
    }

    public static Optional<BlockPos> getActivePos(Player playerEntity, ItemStack stack) {
        BlockPos pos = ((AbstractGadget) stack.getItem()).getAnchor(stack);

        if (pos == null) {
            BlockHitResult res = VectorHelper.getLookingAt(playerEntity, stack);

            if (res == null || res.getType() == Type.MISS) {
                return Optional.empty();
            }

            pos = res.getBlockPos().relative(res.getDirection());
        }

        return Optional.of(pos).map(p -> p.offset(getRelativeVector(stack)));
    }

    public static Optional<Region> getSelectedRegion(ItemStack stack) {
        BlockPos lower = getLowerRegionBound(stack);
        BlockPos upper = getUpperRegionBound(stack);

        if (lower != null && upper != null) {
            return Optional.of(new Region(lower, upper));
        }

        return Optional.empty();
    }

    public static void setSelectedRegion(ItemStack stack, @Nullable Region region) {
        if (region != null) {
            setLowerRegionBound(stack, region.getMin());
            setUpperRegionBound(stack, region.getMax());
        } else {
            setLowerRegionBound(stack, null);
            setUpperRegionBound(stack, null);
        }
    }

    public static void setUpperRegionBound(ItemStack stack, @Nullable BlockPos pos) {
        CompoundTag nbt = stack.getOrCreateTag();

        if (pos != null) {
            nbt.put(NBTKeys.GADGET_START_POS, NbtUtils.writeBlockPos(pos));
        } else {
            nbt.remove(NBTKeys.GADGET_START_POS);
        }
    }

    public static void setLowerRegionBound(ItemStack stack, @Nullable BlockPos pos) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (pos != null) {
            nbt.put(NBTKeys.GADGET_END_POS, NbtUtils.writeBlockPos(pos));
        } else {
            nbt.remove(NBTKeys.GADGET_END_POS);
        }
    }

    @Nullable
    public static BlockPos getUpperRegionBound(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (nbt.contains(NBTKeys.GADGET_START_POS, NbtType.COMPOUND)) {
            return NbtUtils.readBlockPos(nbt.getCompound(NBTKeys.GADGET_START_POS));
        }
        return null;
    }

    @Nullable
    public static BlockPos getLowerRegionBound(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (nbt.contains(NBTKeys.GADGET_END_POS, NbtType.COMPOUND)) {
            return NbtUtils.readBlockPos(nbt.getCompound(NBTKeys.GADGET_END_POS));
        }
        return null;
    }

    private static void setToolMode(ItemStack stack, ToolMode mode) {
        CompoundTag tagCompound = stack.getOrCreateTag();
        tagCompound.putInt(NBTKeys.GADGET_MODE, mode.ordinal());
        stack.setTag(tagCompound);
    }

    public static ToolMode getToolMode(ItemStack stack) {
        CompoundTag tagCompound = stack.getOrCreateTag();
        return ToolMode.values()[tagCompound.getByte(NBTKeys.GADGET_MODE)];
    }

    @Override
    protected void onAnchorSet(ItemStack stack, Player player, BlockHitResult lookingAt) {
        //offset by one
        super.onAnchorSet(stack, player, new BlockHitResult(lookingAt.getLocation(), lookingAt.getDirection(), lookingAt.getBlockPos().relative(lookingAt.getDirection()), lookingAt.isInside()));
    }

    public static ItemStack getGadget(Player player) {
        ItemStack stack = AbstractGadget.getGadget(player);

        if (!(stack.getItem() instanceof GadgetCopyPaste)) {
            return ItemStack.EMPTY;
        }

        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        addEnergyInformation(tooltip, stack);

        tooltip.add(TooltipTranslation.GADGET_MODE.componentTranslation(getToolMode(stack).translation.format()).setStyle(Styles.AQUA));
        addInformationRayTraceFluid(tooltip, stack);
        GadgetUtils.addTooltipNameAndAuthor(stack, world, tooltip);
    }

    public void setMode(ItemStack heldItem, int modeInt) {
        // Called when we specify a mode with the radial menu
        ToolMode mode = ToolMode.values()[modeInt];
        setToolMode(heldItem, mode);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);

        BlockHitResult posLookingAt = VectorHelper.getLookingAt(player, stack);

        if (!world.isClientSide()) {
            boolean lookingAtInventory = ItemStorage.SIDED.find(world, posLookingAt.getBlockPos(), posLookingAt.getDirection()) != null;
            if (player.isShiftKeyDown() && lookingAtInventory) {
                LookupResult.sendToClient((ServerPlayer) player, lookingAtInventory);
                return InteractionResultHolder.pass(stack);
            }

            if (getToolMode(stack) == ToolMode.COPY) {
                if (!world.getBlockState(posLookingAt.getBlockPos()).isAir()) {
                    setRegionAndCopy(stack, world, player, posLookingAt.getBlockPos());
                }
            } else if (getToolMode(stack) == ToolMode.PASTE && !player.isShiftKeyDown()) {
                getActivePos(player, stack).ifPresent(pos -> build(stack, world, player, pos, hand));
            }
        } else {
            if (getToolMode(stack) == ToolMode.COPY) {
                if (player.isShiftKeyDown() && world.getBlockState(posLookingAt.getBlockPos()) == Blocks.AIR.defaultBlockState()) {
                    GuiMod.COPY.openScreen(player);
                }
            } else if (player.isShiftKeyDown()) {
                GuiMod.PASTE.openScreen(player);
            } else {
                BaseRenderer.updateInventoryCache();
            }
        }

        return InteractionResultHolder.success(stack);
    }

    private void setRegionAndCopy(ItemStack stack, Level world, Player player, BlockPos lookedAt) {
        if (player.isShiftKeyDown()) {
            BlockPos bound = getLowerRegionBound(stack);

            if (bound == null || checkCopy(world, player, new Region(lookedAt, bound))) {
                setUpperRegionBound(stack, lookedAt);
            } else {
                return;
            }
        } else {
            BlockPos bound = getUpperRegionBound(stack);

            if (bound == null || checkCopy(world, player, new Region(lookedAt, bound))) {
                setLowerRegionBound(stack, lookedAt);
            } else {
                return;
            }
        }

        Optional<Region> regionOpt = getSelectedRegion(stack);

        if (regionOpt.isEmpty()) {
            // Notify of single copy
            player.displayClientMessage(MessageTranslation.FIRST_COPY.componentTranslation().setStyle(Styles.DK_GREEN), true);
        } else {
            tryCopy(stack, world, player, regionOpt.get());
        }
    }

    public void tryCopy(ItemStack stack, Level world, Player player, Region region) {
        BuildContext context = BuildContext.builder()
                .player(player)
                .stack(stack)
                .build(world);
        WorldBuildView buildView = WorldBuildView.create(context, region,
                (c, p) -> InventoryHelper.getSafeBlockData(player, p, player.getUsedItemHand()));
        performCopy(stack, buildView);
    }

    private boolean checkCopy(Level world, Player player, Region region) {
        if (!ForceUnloadedCommand.mayForceUnloadedChunks(player)) {
            ImmutableSortedSet<ChunkPos> unloaded = region.getUnloadedChunks(world);

            if (!unloaded.isEmpty()) {
                player.displayClientMessage(MessageTranslation.COPY_UNLOADED.componentTranslation(unloaded.size()).setStyle(Styles.RED), true);
                BuildingGadgets.LOG.debug("Prevented copy because {} chunks where detected as unloaded.", unloaded.size());
                BuildingGadgets.LOG.trace("The following chunks were detected as unloaded {}.", CHUNK_JOINER.join(unloaded));
                return false;
            }
        }

        int maxDimension = BuildingGadgets.getConfig().gadgets.gadgetCopyPaste.maxCopySize;

        if (region.getXSize() > 0xFFFF || region.getYSize() > 255 || region.getZSize() > 0xFFFF ||  //these are the max dimensions of a Template
            ((region.getXSize() > maxDimension || region.getYSize() > maxDimension || region.getZSize() > maxDimension) && !OverrideCopySizeCommand.mayPerformLargeCopy(player))) {
            BlockPos sizeVec = region.getMax().subtract(region.getMin());
            player.displayClientMessage(MessageTranslation.COPY_TOO_LARGE
                    .componentTranslation(sizeVec.getX(), sizeVec.getY(), sizeVec.getZ(), Math.min(maxDimension, 0xFFFF), Math.min(maxDimension, 255), Math.min(maxDimension, 0xFFFF))
                    .setStyle(Styles.RED), true);
            return false;
        }

        return true;
    }

    private void performCopy(ItemStack stack, WorldBuildView buildView) {
        BuildContext context = buildView.getContext();
        assert context.getPlayer() != null;
        Player player = context.getPlayer();
        CopyScheduler.scheduleCopy((map, region) -> {
            Template newTemplate = new Template(map,
                    TemplateHeader.builder(region)
                            .name("Copy " + getAndIncrementCopyCounter(stack))
                            .author(player.getName().getContents())
                            .build());
            onCopyFinished(newTemplate.normalize(), stack, player);
        }, buildView, BuildingGadgets.getConfig().gadgets.gadgetCopyPaste.copySteps);
    }

    private void onCopyFinished(Template newTemplate, ItemStack stack, Player player) {
        if (!Additions.sizeInvalid(player, newTemplate.getHeader().getBoundingBox())) {
            sendMessage(stack, player, MessageTranslation.AREA_COPIED, Styles.DK_GREEN);
        }

        ITemplateKey key = BGComponent.TEMPLATE_KEY_COMPONENT.get(stack);
        BGComponent.TEMPLATE_PROVIDER_COMPONENT.maybeGet(player.level).ifPresent(provider -> {
            provider.setTemplate(key, newTemplate);
            provider.requestRemoteUpdate(key, new Target(PacketFlow.CLIENTBOUND, (ServerPlayer) player));
        });
    }

    private void build(ItemStack stack, Level world, Player player, BlockPos pos, InteractionHand hand) {
        BGComponent.TEMPLATE_PROVIDER_COMPONENT.maybeGet(world).ifPresent((ITemplateProvider provider) -> BGComponent.TEMPLATE_KEY_COMPONENT.maybeGet(stack).ifPresent((ITemplateKey key) -> {
            Template template = provider.getTemplateForKey(key);
            BuildContext buildContext = BuildContext.builder()
                    .stack(stack)
                    .player(player)
                    .build(world);
            IBuildView view = template.createViewInContext(buildContext);
            view.translateTo(pos);
            if (!checkPlacement(world, player, view.getBoundingBox())) {
                return;
            }
            schedulePlacement(stack, view, player, hand);
        }));
    }

    private boolean checkPlacement(Level world, Player player, Region region) {
        if (!ForceUnloadedCommand.mayForceUnloadedChunks(player)) {
            ImmutableSortedSet<ChunkPos> unloaded = region.getUnloadedChunks(world);

            if (!unloaded.isEmpty()) {
                player.displayClientMessage(MessageTranslation.BUILD_UNLOADED.componentTranslation(unloaded.size()).setStyle(Styles.RED), true);
                BuildingGadgets.LOG.debug("Prevented build because {} chunks where detected as unloaded.", unloaded.size());
                BuildingGadgets.LOG.trace("The following chunks were detected as unloaded {}.", CHUNK_JOINER.join(unloaded));
                return false;
            }
        }

        int maxDimension = BuildingGadgets.getConfig().gadgets.gadgetCopyPaste.maxBuildSize;

        if ((region.getXSize() > maxDimension || region.getYSize() > maxDimension || region.getZSize() > maxDimension) &&
            !OverrideBuildSizeCommand.mayPerformLargeBuild(player)) {
            BlockPos sizeVec = region.getMax().subtract(region.getMin());
            player.displayClientMessage(MessageTranslation.BUILD_TOO_LARGE
                    .componentTranslation(sizeVec.getX(), sizeVec.getY(), sizeVec.getZ(), maxDimension, maxDimension, maxDimension)
                    .setStyle(Styles.RED), true);
            return false;
        }

        return true;
    }

    private void schedulePlacement(ItemStack stack, IBuildView view, Player player, InteractionHand hand) {
        ServerLevel world = view.getContext().getServerWorld();
        IItemIndex index = InventoryHelper.index(stack, player);
        long energyCost = getEnergyCost(stack);
        boolean overwrite = BuildingGadgets.getConfig().general.allowOverwriteBlocks;
        BlockPlaceContext useContext = new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND, VectorHelper.getLookingAt(player, stack)));
        PlacementChecker checker = new PlacementChecker(
                EnergyStorage.ITEM.find(stack, ContainerItemContext.ofPlayerHand(player, hand)),
                t -> energyCost,
                index,
                (c, t) -> overwrite ? world.getBlockState(t.getPos()).canBeReplaced(useContext) && mayInteract((ServerPlayer) player, t.getPos()) : world.isEmptyBlock(t.getPos()) && mayInteract((ServerPlayer) player, t.getPos()));
        PlacementScheduler.schedulePlacement(view, checker, BuildingGadgets.getConfig().gadgets.placeSteps)
                .withFinisher(p -> {
                    pushUndo(stack, p.getUndoBuilder().build(world), world);
                    onBuildFinished(stack, player, view.getBoundingBox());
                });
    }

    private void onBuildFinished(ItemStack stack, Player player, Region bounds) {
        if (!Additions.sizeInvalid(player, bounds)) {
            sendMessage(stack, player, MessageTranslation.TEMPLATE_BUILD, Styles.DK_GREEN);
        }
    }

    private void sendMessage(ItemStack stack, Player player, ITranslationProvider messageSource, Style style) {
        player.displayClientMessage(messageSource.componentTranslation().setStyle(style), true);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
        return Optional.of(new TemplateData(itemStack));
    }
}
