package com.direwolf20.buildinggadgets.common.tainted.building.view;

import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.tainted.building.Region;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * An {@link IBuildView} which views a {@link Region} in an {@link IWorld} as an {@link IBuildView}. {@link PlacementTarget PlacementTargets}
 * will be created lazily when iterating over this {@link IBuildView} via {@code new PlacementTarget(pos, TileSupport.createBlockData(world, pos))}
 * where pos is the Position currently iterating on and world is the world provided by this views {@link #getContext() build context}.
 * <p>
 * This {@link IBuildView} is especially useful, when trying to read all {@link BlockData} instances with in a given {@link Region}.
 * If you need this Information in a pre-determined way, or intend on iterating multiple times on this {@link IBuildView} consider
 * calling {@link #evaluate()} (which is equivalent to calling {@code PositionalBuildView.ofIterable(view.getContext(), view)})
 * to evaluate all {@link BlockData} instances described by this {@link IBuildView view}.
 */
public final class WorldBuildView implements IBuildView {

    private final BuildContext context;
    private final Region region;
    private final BiFunction<BuildContext, BlockPos, Optional<BlockData>> dataFactory;
    private BlockPos translation;

    public static WorldBuildView create(BuildContext context, Region region) {
        return create(context, region, null);
    }

    public static WorldBuildView create(BuildContext context, Region region, @Nullable BiFunction<BuildContext, BlockPos, Optional<BlockData>> dataFactory) {
        return new WorldBuildView(
                Objects.requireNonNull(context, "Cannot create WorldBuildView without an BuildContext!"),
                Objects.requireNonNull(region, "Cannot create WorldBuildView without an Region!"),
                dataFactory != null ? dataFactory : (c, p) -> Optional.of(TileSupport.createBlockData(c.getWorld(), p)));
    }

    private WorldBuildView(BuildContext context, Region region, BiFunction<BuildContext, BlockPos, Optional<BlockData>> dataFactory) {
        this.context = context;
        this.region = region;
        this.dataFactory = dataFactory;
        this.translation = BlockPos.ZERO;
    }

    @NotNull
    @Override
    public Iterator<PlacementTarget> iterator() {
        return getBoundingBox().stream()
                .map(pos -> dataFactory.apply(context, pos).map(data -> new PlacementTarget(pos.offset(translation), data)).orElse(null))
                .filter(Objects::nonNull)
                .iterator();
    }

    @Override
    public WorldBuildView translateTo(BlockPos pos) {
        this.translation = pos;
        return this;
    }

    @Override
    public WorldBuildView copy() {
        return new WorldBuildView(getContext(), getBoundingBox(), dataFactory);
    }

    @Override
    public BuildContext getContext() {
        return context;
    }

    public Region getBoundingBox() {
        return region;
    }
}
