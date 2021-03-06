package com.direwolf20.buildinggadgets.common.tainted.building;

import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.tainted.inventory.MatchResult;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.util.CommonUtils;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
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
public record PlacementChecker(EnergyStorage energyStorage, ToLongFunction<PlacementTarget> energyFun, IItemIndex index,
                               BiPredicate<BuildContext, PlacementTarget> placeCheck) {

    /**
     * @implNote This code is so god damn messy. Good luck understanding it.
     */
    public CheckResult checkPositionWithResult(BuildContext context, PlacementTarget target, boolean giveBackItems, TransactionContext transaction) {
        if (target.getPos().getY() > context.getWorld().getMaxBuildHeight() || target.getPos().getY() < context.getWorld().getMinBuildHeight() || !placeCheck.test(context, target))
            return new CheckResult(MatchResult.failure(), ImmutableMultiset.of(), false);
        long energy = energyFun.applyAsLong(target);
        Multiset<ItemVariant> insertedItems = ImmutableMultiset.of();
        boolean isCreative = context.getPlayer() != null && context.getPlayer().isCreative();

        // Fail-fast energy check; repeated below
        try (Transaction test = Transaction.openNested(transaction)) {
            if (!isCreative && energyStorage.extract(energy, test) != energy) {
                return new CheckResult(MatchResult.failure(), insertedItems, false);
            }
        }

        HitResult targetRayTrace = null;
        if (context.getPlayer() != null) {
            Player player = context.getPlayer();
            targetRayTrace = CommonUtils.fakeRayTrace(player.position(), target.getPos());
        }
        MaterialList materials = target.getRequiredMaterials(context, targetRayTrace);
        MatchResult match = index.match(materials, transaction);

        ServerLevel world = context.getServerWorld();
        BlockState state = world.getBlockState(target.getPos());
        boolean isAir = state.isAir();

        // TODO: Understand
        // if (ForgeEventFactory.onBlockPlace(context.getPlayer(), blockSnapshot, Direction.UP)) {
        //     return new CheckResult(match, insertedItems, false, usePaste);
        // }

        if (!isAir) {
            if (!world.mayInteract(context.getPlayer(), target.getPos())) {
                return new CheckResult(match, insertedItems, false);
            }

            if (giveBackItems) {
                insertedItems = TileSupport.createTileData(context.getWorld().getBlockEntity(target.getPos()))
                        .getRequiredItems(context, state, null, target.getPos()).iterator().next();
                index.insert(insertedItems, transaction);
            }
        }

        if (!isCreative) {
            try (Transaction extract = Transaction.openNested(transaction)) {
                if (energyStorage.extract(energy, extract) == energy) {
                    extract.commit();
                }
                else {
                    return new CheckResult(match, insertedItems, false);
                }
            }
        }

        return new CheckResult(match, insertedItems, match.isSuccess());
    }

    public record CheckResult(MatchResult match, Multiset<ItemVariant> insertedItems, boolean success) {

        public Multiset<ItemVariant> getInsertedItems() {
            return insertedItems();
        }

        public MatchResult getMatch() {
            return match();
        }

        public boolean isSuccess() {
            return success();
        }
    }
}
