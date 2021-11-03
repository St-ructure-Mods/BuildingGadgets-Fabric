package com.direwolf20.buildinggadgets.common.tainted.concurrent;

import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.tainted.inventory.MatchResult;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.save.Undo;
import com.direwolf20.buildinggadgets.common.tainted.save.Undo.BlockInfo;
import com.google.common.base.Preconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public final class UndoScheduler extends SteppedScheduler {
    public static UndoScheduler scheduleUndo(Undo undo, IItemIndex index, BuildContext context, int steps) {
        Preconditions.checkArgument(steps > 0);

        UndoScheduler res = new UndoScheduler(
                Objects.requireNonNull(undo),
                Objects.requireNonNull(index),
                Objects.requireNonNull(context),
                steps
        );

        ServerTickingScheduler.runTicked(res);
        return res;
    }

    private final Iterator<Map.Entry<BlockPos, BlockInfo>> iterator;
    private final BuildContext context;
    private final IItemIndex index;

    private UndoScheduler(Undo undo, IItemIndex index, BuildContext context, int steps) {
        super(steps);
        assert context.getPlayer() != null;
        assert !context.getStack().isEmpty();

        this.iterator = undo.getUndoData().entrySet().iterator();
        this.index = index;
        this.context = context;
    }

    @Override
    protected boolean advance() {
        if (iterator.hasNext()) {
            return undoBlock(iterator.next());
        } else {
            return false;
        }
    }

    private boolean undoBlock(Map.Entry<BlockPos, BlockInfo> entry) {
        //if the block that was placed is no longer there, we should not undo anything
        BlockState state = context.getWorld().getBlockState(entry.getKey());
        BlockEntity be = context.getWorld().getBlockEntity(entry.getKey());
        BlockData data;
        data = TileSupport.createBlockData(state, be);

        if (data.getState().getBlock().defaultBlockState() != entry.getValue().getPlacedData().getState().getBlock().defaultBlockState()) {
            return true;
        }

        if (!state.isAir() && !context.getServerWorld().mayInteract(context.getPlayer(), entry.getKey())) {
            return true;
        }

        try (Transaction transaction = Transaction.openOuter()) {
            MatchResult matchResult = index.match(MaterialList.of(entry.getValue().getProducedItems()), transaction);

            if (matchResult.isSuccess()) {
                index.insert(entry.getValue().getUsedItems(), transaction);

                EffectBlock.spawnUndoBlock(context, new PlacementTarget(entry.getKey(), entry.getValue().getRecordedData()));
                transaction.commit();
            }
        }

        return true;
    }

    @Override
    protected void onFinish() {
    }
}
