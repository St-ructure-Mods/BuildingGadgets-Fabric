package com.direwolf20.buildinggadgets.common.tainted.building;

import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.tainted.inventory.MatchResult;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.UniqueItem;
import com.direwolf20.buildinggadgets.common.util.CommonUtils;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import team.reborn.energy.api.EnergyStorage;

import java.util.function.BiPredicate;
import java.util.function.ToLongFunction;

/**
 * This class performs all Placement checks required for the Copy-Paste-Gadget. Aka it tests for availability of energy, items and free placement-space.
 * You can extract information about whether the tests succeed, paste was used etc. from the CheckResult.
 */
public final class PlacementChecker {
    private final EnergyStorage energyStorage;
    private final ToLongFunction<PlacementTarget> energyFun;
    private final IItemIndex index;
    private final BiPredicate<BuildContext, PlacementTarget> placeCheck;

    public PlacementChecker(EnergyStorage energyStorage, ToLongFunction<PlacementTarget> energyFun, IItemIndex index, BiPredicate<BuildContext, PlacementTarget> placeCheck) {
        this.energyStorage = energyStorage;
        this.energyFun = energyFun;
        this.index = index;
        this.placeCheck = placeCheck;
    }

    /**
     * @implNote This code is so god damn messy. Good luck understanding it.
     */
    public CheckResult checkPositionWithResult(BuildContext context, PlacementTarget target, boolean giveBackItems) {
        if (target.getPos().getY() > context.getWorld().getMaxBuildHeight() || target.getPos().getY() < 0 || !placeCheck.test(context, target))
            return new CheckResult(MatchResult.failure(), ImmutableMultiset.of(), false, false);
        long energy = energyFun.applyAsLong(target);
        Multiset<UniqueItem> insertedItems = ImmutableMultiset.of();
        boolean isCreative = context.getPlayer() != null && context.getPlayer().isCreative();

        try (Transaction transaction = Transaction.openOuter()) {
            boolean check = energyStorage.extract(energy, transaction) != energy;
            transaction.abort();

            if (!isCreative && check) {
                return new CheckResult(MatchResult.failure(), insertedItems, false, false);
            }
        }

        HitResult targetRayTrace = null;
        if (context.getPlayer() != null) {
            Player player = context.getPlayer();
            targetRayTrace = CommonUtils.fakeRayTrace(player.position(), target.getPos());
        }
        MaterialList materials = target.getRequiredMaterials(context, targetRayTrace);
        MatchResult match = index.tryMatch(materials);
        boolean usePaste = false;
        if (!match.isSuccess()) {
            match = index.tryMatch(InventoryHelper.PASTE_LIST);
            if (!match.isSuccess())
                return new CheckResult(match, insertedItems, false, false);
            usePaste = true;
        }

        ServerLevel world = context.getServerWorld();
        BlockState state = world.getBlockState(target.getPos());
        boolean isAir = state.isAir();

        // TODO: Understand
        // if (ForgeEventFactory.onBlockPlace(context.getPlayer(), blockSnapshot, Direction.UP)) {
        //     return new CheckResult(match, insertedItems, false, usePaste);
        // }

        if (!isAir) {
            if (!world.mayInteract(context.getPlayer(), target.getPos())) {
                return new CheckResult(match, insertedItems, false, usePaste);
            }

            if (giveBackItems) {
                insertedItems = TileSupport.createTileData(context.getWorld().getBlockEntity(target.getPos()))
                        .getRequiredItems(context, state, null, target.getPos()).iterator().next();
                index.insert(insertedItems);
            }
        }

        if (!isCreative) {
            try (Transaction transaction = Transaction.openOuter()) {
                if (energyStorage.extract(energy, transaction) == energy) {
                    transaction.commit();
                } else {
                    transaction.abort();
                    return new CheckResult(match, insertedItems, false, usePaste);
                }
            }
        }

        return new CheckResult(match, insertedItems, index.applyMatch(match), usePaste);
    }

    public static final class CheckResult {
        private final MatchResult match;
        private final Multiset<UniqueItem> insertedItems;
        private final boolean success;
        private final boolean usingPaste;

        private CheckResult(MatchResult match, Multiset<UniqueItem> insertedItems, boolean success, boolean usingPaste) {
            this.match = match;
            this.insertedItems = insertedItems;
            this.success = success;
            this.usingPaste = usingPaste;
        }

        public Multiset<UniqueItem> getInsertedItems() {
            return insertedItems;
        }

        public MatchResult getMatch() {
            return match;
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isUsingPaste() {
            return usingPaste;
        }
    }
}
