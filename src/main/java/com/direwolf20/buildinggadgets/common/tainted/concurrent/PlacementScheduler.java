package com.direwolf20.buildinggadgets.common.tainted.concurrent;

import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock.Mode;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.PlacementChecker;
import com.direwolf20.buildinggadgets.common.tainted.building.PlacementChecker.CheckResult;
import com.direwolf20.buildinggadgets.common.tainted.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.building.view.IBuildView;
import com.direwolf20.buildinggadgets.common.tainted.save.Undo;
import com.direwolf20.buildinggadgets.common.tainted.save.Undo.Builder;
import com.google.common.base.Preconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

public final class PlacementScheduler extends SteppedScheduler {
    public static PlacementScheduler schedulePlacement(IBuildView view, PlacementChecker checker, int steps) {
        Preconditions.checkArgument(steps > 0);

        PlacementScheduler res = new PlacementScheduler(
                Objects.requireNonNull(view),
                Objects.requireNonNull(checker),
                steps);

        ServerTickingScheduler.runTicked(res);
        return res;
    }

    private final IBuildView view;
    private final Iterator<PlacementTarget> iterator;
    private final PlacementChecker checker;
    private Consumer<PlacementScheduler> finisher;
    private final Undo.Builder undoBuilder;

    private PlacementScheduler(IBuildView view, PlacementChecker checker, int steps) {
        super(steps);
        this.checker = checker;
        this.view = view;
        this.iterator = view.iterator();
        this.undoBuilder = Undo.builder();
        this.finisher = p -> {
        };
    }

    @Override
    protected void onFinish() {
        finisher.accept(this);
    }

    @Override
    protected boolean advance() {
        if (iterator.hasNext()) {
            return checkTarget(iterator.next());
        } else {
            return false;
        }
    }

    public Builder getUndoBuilder() {
        return undoBuilder;
    }

    public PlacementScheduler withFinisher(Consumer<PlacementScheduler> runnable) {
        this.finisher = Objects.requireNonNull(runnable);
        return this;
    }

    private boolean checkTarget(PlacementTarget target) {
        try (Transaction transaction = Transaction.openOuter()) {
            CheckResult res = checker.checkPositionWithResult(view.getContext(), target, false, transaction);

            if (res.isSuccess()) {
                undoBuilder.record(view.getContext().getWorld(), target.getPos(), target.getData(), res.getMatch().getChosenOption(), res.getInsertedItems());
                EffectBlock.spawnEffectBlock(view.getContext(), target, Mode.PLACE);

                BuildContext context = view.getContext();
                BlockData targetBlock = target.getData();

                if (target.getData().getState().getBlock() instanceof DoorBlock && targetBlock.getState().getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER && context.getWorld().isEmptyBlock(target.getPos().above())) {
                    EffectBlock.spawnEffectBlock(context.getWorld(), target.getPos().above(), new BlockData(targetBlock.getState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), TileSupport.dummyTileEntityData()), Mode.PLACE);
                }

                transaction.commit();
            }
        }

        return true;
    }
}
