package com.direwolf20.buildinggadgets.client.renders;

import com.direwolf20.buildinggadgets.client.renderer.OurRenderTypes;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.modes.AbstractMode;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.tainted.inventory.MatchResult;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.Level;

import java.util.List;
import java.util.Optional;

import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.getAnchor;
import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.getToolBlock;

public class BuildRender extends BaseRenderer {
    private final boolean isExchanger;
    private static final BlockState DEFAULT_EFFECT_BLOCK = OurBlocks.EFFECT_BLOCK.defaultBlockState();
    private BlockState errorState;

    public BuildRender(boolean isExchanger) {
        this.isExchanger = isExchanger;
    }

    @Override
    public void render(WorldRenderContext evt, Player player, ItemStack heldItem) {
        super.render(evt, player, heldItem);

        BlockHitResult lookingAt = VectorHelper.getLookingAt(player, heldItem);
        BlockState startBlock = player.level.getBlockState(lookingAt.getBlockPos());
        Optional<List<BlockPos>> anchor = getAnchor(heldItem);

        if ((player.level.isEmptyBlock(lookingAt.getBlockPos()) && anchor.isEmpty()) || startBlock == DEFAULT_EFFECT_BLOCK) {
            return;
        }

        BlockData data = getToolBlock(heldItem);
        BlockState renderBlockState = data.getState();

        if (errorState == renderBlockState || renderBlockState == BaseRenderer.AIR) {
            return;
        }

        if(errorState != null) {
            errorState = null;
        }

        // Get the coordinates from the anchor. If the anchor isn't present then build the collector.
        List<BlockPos> coordinates = anchor.orElseGet(() -> {
            AbstractMode mode = !this.isExchanger ? GadgetBuilding.getToolMode(heldItem).getMode() : GadgetExchanger.getToolMode(heldItem).getMode();
            return mode.getCollection(
                    new AbstractMode.UseContext(player.level, renderBlockState, lookingAt.getBlockPos(), heldItem, lookingAt.getDirection(), !this.isExchanger && GadgetBuilding.shouldPlaceAtop(heldItem), GadgetBuilding.getConnectedArea(heldItem)),
                    player
            );
        });

        // Sort them on a new line for readability
//        coordinates = SortingHelper.Blocks.byDistance(coordinates, player);

        //Prepare the fake world -- using a fake world lets us render things properly, like fences connecting.
        getBuilderWorld().setWorldAndState(player.level, renderBlockState, coordinates);

        Vec3 playerPos = evt.camera().getPosition();
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

        //Save the current position that is being rendered (I think)
        PoseStack matrix = evt.matrixStack();
        matrix.pushPose();
        matrix.translate(-playerPos.x(), -playerPos.y(), -playerPos.z());

        BlockRenderDispatcher dispatcher = evt.gameRenderer().getMinecraft().getBlockRenderer();

        RenderSystem.enableDepthTest();
        for (BlockPos coordinate : coordinates) {
            matrix.pushPose();
            matrix.translate(coordinate.getX(), coordinate.getY(), coordinate.getZ());

            if (this.isExchanger) {
                matrix.translate(-0.0005f, -0.0005f, -0.0005f);
                matrix.scale(1.001f, 1.001f, 1.001f);
            }

            try{
                OurRenderTypes.MultiplyAlphaRenderTypeBuffer mutatedBuffer = new OurRenderTypes.MultiplyAlphaRenderTypeBuffer(Minecraft.getInstance().renderBuffers().bufferSource(), .55f);
                dispatcher.renderSingleBlock(renderBlockState, matrix, mutatedBuffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            }
            catch (Exception e) {
                BuildingGadgets.LOG.log(Level.ERROR, "Failed to render blockstate with gadget, not rendering blockstate");
                errorState = renderBlockState;
            }

            matrix.popPose();
            buffer.endBatch();
        }
        RenderSystem.disableDepthTest();

        // Don't even waste the time checking to see if we have the right energy, items, etc for creative mode
        if (!player.isCreative()) {
            boolean hasLinkedInventory = getCacheInventory().maintainCache(heldItem);
            int remainingCached = getCacheInventory().getCache() == null ? -1 : getCacheInventory().getCache().count(ItemVariant.of(data.getState().getBlock().asItem()));

            // Figure out how many of the block we're rendering we have in the inventory of the player.
            IItemIndex index = InventoryHelper.index(heldItem, player);
            BuildContext context = new BuildContext(player.level, player, heldItem);

            MaterialList materials = data.getRequiredItems(context, null, null);
            long hasEnergy = getEnergy(player, heldItem);

            try (Transaction transaction = Transaction.openOuter()) {
                for (BlockPos coordinate : coordinates) { //Now run through the UNSORTED list of coords, to show which blocks won't place if you don't have enough of them.
                    boolean renderFree = false;
                    hasEnergy -= ((AbstractGadget) heldItem.getItem()).getEnergyCost(heldItem);
                    MatchResult match = index.match(materials, transaction);
                    VertexConsumer builder = buffer.getBuffer(OurRenderTypes.MissingBlockOverlay);

                    if (!match.isSuccess() || hasEnergy < 0) {
                        if (hasLinkedInventory && remainingCached > 0) {
                            renderFree = true;
                            remainingCached--;
                        } else {
                            renderMissingBlock(matrix.last().pose(), builder, coordinate);
                        }
                    } else {
                        renderBoxSolid(matrix.last().pose(), builder, coordinate, .97f, 1f, .99f, .1f);
                    }

                    if (renderFree) {
                        renderBoxSolid(matrix.last().pose(), builder, coordinate, .97f, 1f, .99f, .1f);
                    }
                }
            }
        }

        matrix.popPose();
        RenderSystem.disableDepthTest();
        buffer.endBatch(); // @mcp: finish (mcp) = draw (yarn)
    }

    @Override
    public boolean isLinkable() {
        return true;
    }

}
