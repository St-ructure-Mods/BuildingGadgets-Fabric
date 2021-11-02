package com.direwolf20.buildinggadgets.client.renders;

import com.direwolf20.buildinggadgets.client.renderer.OurRenderTypes;
import com.direwolf20.buildinggadgets.common.component.BGComponent;
import com.direwolf20.buildinggadgets.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.tainted.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.tainted.building.Region;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.building.view.IBuildView;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider.IUpdateListener;
import com.direwolf20.buildinggadgets.common.tainted.template.Template;
import com.direwolf20.buildinggadgets.common.world.MockDelegationWorld;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.io.Closeable;
import java.util.*;
import java.util.function.Consumer;


public class CopyPasteRender extends BaseRenderer implements IUpdateListener {
    private MultiVBORenderer renderBuffer;
    private int tickTrack = 0;
    private UUID lastRendered = null;

    @Override
    public void onTemplateUpdate(ITemplateProvider provider, ITemplateKey key, Template template) {
        if (provider.getId(key).equals(lastRendered))
            renderBuffer = null;
    }

    @Override
    public void onTemplateUpdateSend(ITemplateProvider provider, ITemplateKey key, Template template) {
        onTemplateUpdate(provider, key, template);
    }

    @Override
    public void renderAfterSetup(WorldRenderContext context, Player player, ItemStack heldItem) {
        if (GadgetCopyPaste.getToolMode(heldItem) == GadgetCopyPaste.ToolMode.COPY) {
            GadgetCopyPaste.getSelectedRegion(heldItem).ifPresent(region -> {
                PoseStack stack = context.matrixStack();
                Vec3 cameraView = context.camera().getPosition();

                stack.pushPose();
                stack.translate(-cameraView.x(), -cameraView.y(), -cameraView.z());
                renderCopy(stack, region);
                stack.popPose();
            });
        }
    }

    @Override
    public void render(WorldRenderContext context, Player player, ItemStack heldItem) {
        // We can completely trust that heldItem isn't empty and that it's a copy paste gadget.
        super.render(context, player, heldItem);

        if (GadgetCopyPaste.getToolMode(heldItem) != GadgetCopyPaste.ToolMode.COPY) {
            // Provide this as both renders require the data.
            Vec3 cameraView = context.camera().getPosition();

            // translate the matric to the projected view
            PoseStack stack = context.matrixStack(); //Get current matrix position from the evt call
            stack.pushPose(); //Save the render position from RenderWorldLast
            stack.translate(-cameraView.x(), -cameraView.y(), -cameraView.z()); //Sets render position to 0,0,0

            renderPaste(stack, cameraView, player, heldItem);
            stack.popPose();
        }
    }

    private void renderCopy(PoseStack matrix, Region region) {
        BlockPos startPos = region.getMin();
        BlockPos endPos = region.getMax();
        BlockPos blankPos = BlockPos.ZERO;

        if (startPos.equals(blankPos) || endPos.equals(blankPos)) {
            return;
        }

        int R = 255, G = 223, B = 127;

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        LevelRenderer.renderLineBox(matrix, buffer.getBuffer(OurRenderTypes.CopyGadgetLines), new AABB(region.getMin(), region.getMax().offset(1, 1, 1)), R / 255f, G / 255f, B / 255f, 1f);
    }

    private void renderPaste(PoseStack matrices, Vec3 cameraView, Player player, ItemStack heldItem) {
        Level world = player.level;

        // Check the template cap from the world
        // Fetch the template key (because for some reason this is it's own cap)
        BGComponent.TEMPLATE_PROVIDER_COMPONENT.maybeGet(world).ifPresent((ITemplateProvider provider) -> BGComponent.TEMPLATE_KEY_COMPONENT.maybeGet(heldItem).ifPresent((ITemplateKey key) -> {
            // Finally get the data from the render.
            GadgetCopyPaste.getActivePos(player, heldItem).ifPresent(startPos -> {
                MockDelegationWorld fakeWorld = new MockDelegationWorld(world);

                BuildContext context = BuildContext.builder().player(player).stack(heldItem).build(fakeWorld);

                // Get the template and move it to the start pos (player.pick())
                IBuildView view = provider.getTemplateForKey(key).createViewInContext(context);

                // Sort the render
                List<PlacementTarget> targets = new ArrayList<>(view.estimateSize());
                for (PlacementTarget target : view) {
                    if (target.placeIn(context)) {
                        targets.add(target);
                    }
                }
                UUID id = provider.getId(key);
                if (!id.equals(lastRendered))
                    renderBuffer = null;
                renderTargets(matrices, cameraView, context, targets, startPos);
                lastRendered = id;
            });
        }));
    }

    //TODO: fix
    private void renderTargets(PoseStack matrix, Vec3 projectedView, BuildContext context, List<PlacementTarget> targets, BlockPos startPos) {
        tickTrack++;
            if (tickTrack % 30 == 0) {
                try {
                    Vec3 projectedView2 = projectedView;
                    Vec3 startPosView = new Vec3(startPos.getX(), startPos.getY(), startPos.getZ());
                    projectedView2 = projectedView2.subtract(startPosView);
                    //renderBuffer.sort((float) projectedView2.x(), (float) projectedView2.y(), (float) projectedView2.z());
                } catch (Exception ignored) {
                }
            }

            RenderSystem.enableDepthTest();
            MultiBufferSource.BufferSource bufferSource = getMc().renderBuffers().bufferSource();
            matrix.translate(startPos.getX(), startPos.getY(), startPos.getZ());
            BlockRenderDispatcher dispatcher = getMc().getBlockRenderer();
            BlockColors blockColors = Minecraft.getInstance().getBlockColors();

            for (PlacementTarget target : targets) {
                matrix.pushPose();
                BlockPos pos = target.pos();
                BlockState state = context.getWorld().getBlockState(pos);
                matrix.translate(pos.getX(), pos.getY(), pos.getZ());
                int color = blockColors.getColor(state, context.getWorld(), pos, 0);
                float f = (float) (color >> 16 & 255) / 255.0F;
                float f1 = (float) (color >> 8 & 255) / 255.0F;
                float f2 = (float) (color & 255) / 255.0F;

                OurRenderTypes.MultiplyAlphaRenderTypeBuffer mutatedBuffer = new OurRenderTypes.MultiplyAlphaRenderTypeBuffer(Minecraft.getInstance().renderBuffers().bufferSource(), .55f);
                dispatcher.renderSingleBlock(state, matrix, mutatedBuffer, 15728640, OverlayTexture.NO_OVERLAY);

                matrix.popPose();
                bufferSource.endBatch();
            }
            RenderSystem.disableDepthTest();
            bufferSource.endBatch();

            //renderBuffer.render(matrix.last().pose()); //Actually draw whats in the buffer
            //return;

//        List<BlockPos> blockPosList = sorter.getSortedTargets().stream().map(PlacementTarget::getPos).collect(Collectors.toList());

/*        tickTrack = 0;
        if (renderBuffer != null) //Reset Render Buffer before rebuilding
            renderBuffer.close();
        //TODO: FIX GL_INVALID_OPERATION, Invalid VBO usage
        renderBuffer = MultiVBORenderer.of((buffer) -> {
            VertexConsumer builder = buffer.getBuffer(RenderType.solid());
            VertexConsumer noDepthbuilder = buffer.getBuffer(RenderType.solid());

            BlockRenderDispatcher dispatcher = getMc().getBlockRenderer();

            PoseStack stack = new PoseStack(); //Create a new matrix stack for use in the buffer building process
            stack.pushPose(); //Save position

            for (PlacementTarget target : targets) {
                BlockPos targetPos = target.getPos();
                BlockState state = context.getWorld().getBlockState(target.getPos());

                stack.pushPose(); //Save position again
                matrix.translate(-startPos.getX(), -startPos.getY(), -startPos.getZ());
                stack.translate(targetPos.getX(), targetPos.getY(), targetPos.getZ());

                BakedModel ibakedmodel = dispatcher.getBlockModel(state);
                BlockColors blockColors = Minecraft.getInstance().getBlockColors();
                int color = blockColors.getColor(state, context.getWorld(), targetPos, 0);

                float f = (float) (color >> 16 & 255) / 255.0F;
                float f1 = (float) (color >> 8 & 255) / 255.0F;
                float f2 = (float) (color & 255) / 255.0F;
                try {
                    if (state.getRenderShape() == RenderShape.MODEL) {
                        for (Direction direction : Direction.values()) {
                            // TODO: likely broken this
                            if (Block.shouldRenderFace(state, context.getWorld(), targetPos, direction, target.getPos()) && !(context.getWorld().getBlockState(targetPos.relative(direction)).getBlock().equals(state.getBlock()))) {
                                if (state.getMaterial().isSolidBlocking()) {
                                    renderModelBrightnessColorQuads(stack.last(), builder, f, f1, f2, 0.7f, ibakedmodel.getQuads(state, direction, new Random(Mth.getSeed(targetPos))), 15728640, 655360);
                                } else {
                                    renderModelBrightnessColorQuads(stack.last(), noDepthbuilder, f, f1, f2, 0.7f, ibakedmodel.getQuads(state, direction, new Random(Mth.getSeed(targetPos))), 15728640, 655360);
                                }
                            }
                        }
                        if (state.getMaterial().isSolidBlocking())
                            renderModelBrightnessColorQuads(stack.last(), builder, f, f1, f2, 0.7f, ibakedmodel.getQuads(state, null, new Random(Mth.getSeed(targetPos))), 15728640, 655360);
                        else
                            renderModelBrightnessColorQuads(stack.last(), noDepthbuilder, f, f1, f2, 0.7f, ibakedmodel.getQuads(state, null, new Random(Mth.getSeed(targetPos))), 15728640, 655360);
                    }
                } catch (Exception e) {
                    BuildingGadgets.LOG.trace("Caught exception whilst rendering {}.", state, e);
                }

                stack.popPose(); // Load the position we saved earlier
            }
            stack.popPose(); //Load after loop
        });
*/
//        try {
        //Vec3 projectedView2 = getMc().gameRenderer.getMainCamera().getPosition();
        //Vec3 startPosView = new Vec3(startPos.getX(), startPos.getY(), startPos.getZ());
        //projectedView2 = projectedView2.subtract(startPosView);
        //renderBuffer.sort((float) projectedView2.x(), (float) projectedView2.y(), (float) projectedView2.z());
//        } catch (Exception ignored) {
//        }
        //matrix.translate(startPos.getX(), startPos.getY(), startPos.getZ());
        //renderBuffer.render(matrix.last().pose()); //Actually draw whats in the buffer
    }

    @Override
    public boolean isLinkable() {
        return true;
    }

    /**
     * Vertex Buffer Object for caching the render. Pretty similar to how the chunk caching works
     */
    public static class MultiVBORenderer implements Closeable {
        private static final int BUFFER_SIZE = 2 * 1024 * 1024 * 3;

        public static MultiVBORenderer of(Consumer<MultiBufferSource> vertexProducer) {
            final Map<RenderType, BufferBuilder> builders = Maps.newHashMap();

            vertexProducer.accept(rt -> builders.computeIfAbsent(rt, (_rt) -> {
                BufferBuilder builder = new BufferBuilder(BUFFER_SIZE);
                builder.begin(_rt.mode(), _rt.format());

                return builder;
            }));

            Map<RenderType, VertexBuffer> buffers = Maps.transformEntries(builders, (rt, builder) -> {
                Objects.requireNonNull(rt);
                Objects.requireNonNull(builder);

                builder.end();
                VertexBuffer vbo = new VertexBuffer();

                vbo.upload(builder);
                return vbo;
            });

            return new MultiVBORenderer(buffers);
        }

        private final ImmutableMap<RenderType, VertexBuffer> buffers;

        protected MultiVBORenderer(Map<RenderType, VertexBuffer> buffers) {
            this.buffers = ImmutableMap.copyOf(buffers);
        }

        public void sort(float x, float y, float z) {
            // Dire the fucking depth buffer. WHAT THE FUCK
            // Fuck you for putting me through this pain


//            for (Map.Entry<RenderType, DireBufferBuilder.State> kv : sortCaches.entrySet()) {
//                RenderType rt = kv.getKey();
//                DireBufferBuilder.State state = kv.getValue();
//                DireBufferBuilder builder = new DireBufferBuilder(BUFFER_SIZE);
//                builder.begin(rt.mode().asGLMode, rt.format());
//                builder.setVertexState(state);
//                builder.sortVertexData(x, y, z);
//                builder.finishDrawing();
//
//                DireVertexBuffer vbo = buffers.get(rt);
//                vbo.upload(builder);
//            }
        }

        public void render(Matrix4f modelViewMatrix) {
            buffers.forEach((rt, vbo) -> {
                VertexFormat fmt = rt.format();

                rt.setupRenderState();
                vbo.bind();
                fmt.setupBufferState();
//                vbo.drawWithShader(modelViewMatrix, RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
                VertexBuffer.unbind();
                fmt.clearBufferState();
                rt.clearRenderState();
            });
        }

        public void close() {
            for (VertexBuffer value : buffers.values()) {
                value.close();
            }
        }
    }
}
