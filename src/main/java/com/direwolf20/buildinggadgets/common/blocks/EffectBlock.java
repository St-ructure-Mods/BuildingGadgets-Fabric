package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tileentities.EffectBlockTileEntity;
import com.direwolf20.buildinggadgets.common.tileentities.OurTileEntities;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
public class EffectBlock extends BaseEntityBlock {

    public enum Mode {
        // Serialization and networking based on `ordinal()`, please DO NOT CHANGE THE ORDER of the enums
        PLACE() {
            @Override
            public void onBuilderRemoved(EffectBlockTileEntity builder) {
                Level world = builder.getLevel();
                if (world == null)
                    return;

                BlockPos targetPos = builder.getBlockPos();
                BlockData targetBlock = builder.getRenderedBlock();
                if (targetBlock.getState().getBlock() instanceof LeavesBlock) {
                    targetBlock = new BlockData(targetBlock.getState().setValue(LeavesBlock.PERSISTENT, true), targetBlock.getTileData());
                }

                targetBlock.placeIn(BuildContext.builder().build(world), targetPos);

                // Instead of removing the block, we just sync the client & server to know that the block has been replaced
                world.sendBlockUpdated(targetPos, targetBlock.getState(), targetBlock.getState(), 1);

                BlockPos upPos = targetPos.above();
                world.getBlockState(targetPos).neighborChanged(world, targetPos, world.getBlockState(upPos).getBlock(), upPos, false);
            }
        },
        REMOVE() {
            @Override
            public void onBuilderRemoved(EffectBlockTileEntity builder) {
                builder.getLevel().removeBlock(builder.getBlockPos(), false);
            }
        },
        REPLACE() {
            @Override
            public void onBuilderRemoved(EffectBlockTileEntity builder) {
                spawnEffectBlock(builder.getLevel(), builder.getBlockPos(), builder.getSourceBlock(), PLACE);
            }
        };

        public static final Mode[] VALUES = values();

        public abstract void onBuilderRemoved(EffectBlockTileEntity builder);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, OurTileEntities.EFFECT_BLOCK_TILE_ENTITY, EffectBlockTileEntity::tick);
    }

    /**
     * As the effect block is effectively air it needs to have a material just like Air.
     * We don't use Material.AIR as this is replaceable.
     */
    private static final Material EFFECT_BLOCK_MATERIAL = new Material.Builder(MaterialColor.NONE).nonSolid().build();

    public static void spawnUndoBlock(BuildContext context, PlacementTarget target) {
        BlockState state = context.getWorld().getBlockState(target.getPos());

        BlockEntity curTe = context.getWorld().getBlockEntity(target.getPos());
        //can't use .isAir, because it's not build yet
        if (target.getData().getState() != Blocks.AIR.defaultBlockState()) {
            Mode mode = state.isAir() ? Mode.PLACE : Mode.REPLACE;
            spawnEffectBlock(curTe, state, context.getWorld(), target.getPos(), target.getData(), mode);
        } else if (!state.isAir()) {
            spawnEffectBlock(curTe, state, context.getWorld(), target.getPos(), TileSupport.createBlockData(state, curTe), Mode.REMOVE);
        }
    }

    public static void spawnEffectBlock(BuildContext context, PlacementTarget target, Mode mode) {
        spawnEffectBlock(context.getWorld(), target.getPos(), target.getData(), mode);
    }

    public static void spawnEffectBlock(LevelAccessor world, BlockPos spawnPos, BlockData spawnBlock, Mode mode) {
        BlockState state = world.getBlockState(spawnPos);
        BlockEntity curTe = world.getBlockEntity(spawnPos);
        spawnEffectBlock(curTe, state, world, spawnPos, spawnBlock, mode);
    }

    private static void spawnEffectBlock(@Nullable BlockEntity curTe, BlockState curState, LevelAccessor world, BlockPos spawnPos, BlockData spawnBlock, Mode mode) {
        BlockState state = OurBlocks.EFFECT_BLOCK.defaultBlockState();
        world.setBlock(spawnPos, state, 3);

        BlockEntity tile = world.getBlockEntity(spawnPos);
        if (!(tile instanceof EffectBlockTileEntity)) {
            // Fail safely by replacing with air. Kinda voids but meh...
            world.setBlock(spawnPos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }

        ((EffectBlockTileEntity) tile).initializeData(curState, curTe, spawnBlock, mode);
        // Send data to client
        if (world instanceof Level)
            ((Level) world).sendBlockUpdated(spawnPos, state, state, 1);
    }

    public EffectBlock() {
        super(Block.Properties.of(EFFECT_BLOCK_MATERIAL)
                .strength(20f)
                .noDrops());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return OurTileEntities.EFFECT_BLOCK_TILE_ENTITY.create(pos, state);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(BlockState state) {
        // We still make effect blocks invisible because all effects (scaling block, transparent box) are dynamic so they has to be in the TER
        return RenderShape.MODEL;
    }

    @Override
    public boolean skipRendering(BlockState p_200122_1_, BlockState p_200122_2_, Direction p_200122_3_) {
        return true;
    }

    /**
     * This gets a complete list of items dropped from this block.
     *
     * @param p_220076_1_ Current state
     */
    @Override
    public List<ItemStack> getDrops(BlockState p_220076_1_, LootContext.Builder p_220076_2_) {
        return new ArrayList<>();
    }

    @Override
    @Deprecated
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 0;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 1.0f;
    }
}
