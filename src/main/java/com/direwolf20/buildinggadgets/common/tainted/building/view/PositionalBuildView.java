package com.direwolf20.buildinggadgets.common.tainted.building.view;

import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.tainted.building.Region;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * A simple {@link IBuildView} backed by a {@link Map Map<BlockPos, BlockData>}. {@link PlacementTarget PlacementTargets} will be created
 * lazily when iterating over this {@link IBuildView}. You can supply this with a mutable {@link Map} via {@link #createUnsafe(BuildContext, Map, Region)}
 * for efficiency reasons, note however that you will encounter undefined behaviour if the {@link Map} is modified after this {@link IBuildView} was
 * created.
 */
public final class PositionalBuildView implements IBuildView {

    private final Map<BlockPos, BlockData> map;
    private Region boundingBox;
    private BlockPos translation;
    private final BuildContext context;

    public static PositionalBuildView createUnsafe(BuildContext context, Map<BlockPos, BlockData> map, Region boundingBox) {
        return new PositionalBuildView(
                Objects.requireNonNull(context, "Cannot have a PositionalBuildView without BuildContext!"),
                Objects.requireNonNull(map, "Cannot have a PositionalBuildView without position to data map!"),
                Objects.requireNonNull(boundingBox, "Cannot have a PositionalBuildView without a boundingBox!")
        );
    }

    private PositionalBuildView(BuildContext context, Map<BlockPos, BlockData> map, Region boundingBox) {
        this.context = context;
        this.map = map;
        this.boundingBox = boundingBox;
        this.translation = BlockPos.ZERO;
    }

    @NotNull
    @Override
    public Iterator<PlacementTarget> iterator() {
        return Iterators.transform(map.entrySet().iterator(), entry -> new PlacementTarget(entry.getKey(), entry.getValue()));
    }

    @Override
    public PositionalBuildView translateTo(BlockPos pos) {
        boundingBox = boundingBox.translate(pos.subtract(translation));//translate the bounding box to the correct position
        this.translation = pos;
        return this;
    }


    @Override
    public PositionalBuildView copy() {
        return new PositionalBuildView(context, map, boundingBox);
    }

    @Override
    public Region getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public BuildContext getContext() {
        return context;
    }

    public ImmutableMap<BlockPos, BlockData> getMap() {
        return ImmutableMap.copyOf(map);
    }
}
