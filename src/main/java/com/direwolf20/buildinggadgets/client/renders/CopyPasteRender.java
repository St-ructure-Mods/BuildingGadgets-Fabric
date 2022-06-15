package com.direwolf20.buildinggadgets.client.renders;

import com.direwolf20.buildinggadgets.client.renderer.OurRenderTypes;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
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
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
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
    private ShaderInstance instance;

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
                List<PlacementTarget> targets = new ArrayList<>();
                for (PlacementTarget target : view) {
                    if (target.placeIn(context)) {
                        targets.add(target);
                    }
                }
                UUID id = provider.getId(key);
                if (!id.equals(lastRendered)) {
                    renderBuffer = null;
                    System.gc();
                }

                renderTargets(matrices, cameraView, context, targets, startPos);
                lastRendered = id;
            });
        }));
    }

    private void renderTargets(PoseStack matrix, Vec3 projectedView, BuildContext context, List<PlacementTarget> targets, BlockPos startPos) {
        tickTrack++;
        if (renderBuffer != null && tickTrack < 300) {
            if (tickTrack % 30 == 0) {
                try {
                    //Vec3 projectedView2 = projectedView;
                    //Vec3 startPosView = new Vec3(startPos.getX(), startPos.getY(), startPos.getZ());
                    //projectedView2 = projectedView2.subtract(startPosView);
                    //renderBuffer.sort((float) projectedView2.x(), (float) projectedView2.y(), (float) projectedView2.z());
                } catch (Exception ignored) {
                }
            }

            matrix.translate(startPos.getX(), startPos.getY(), startPos.getZ());
            renderBuffer.render(matrix.last().pose()); //Actually draw whats in the buffer
            return;
        }


        tickTrack = 0;
        if (renderBuffer != null) //Reset Render Buffer before rebuilding
            renderBuffer.close();

        renderBuffer = MultiVBORenderer.of((buffer) -> {
            OurRenderTypes.MultiplyAlphaRenderTypeBuffer mutatedBuffer = new OurRenderTypes.MultiplyAlphaRenderTypeBuffer(buffer, .7f);

            BlockRenderDispatcher dispatcher = getMc().getBlockRenderer();

            PoseStack stack = new PoseStack(); //Create a new matrix stack for use in the buffer building process
            stack.pushPose(); //Save position

            for (PlacementTarget target : targets) {
                BlockPos targetPos = target.getPos();
                BlockState state = context.getWorld().getBlockState(target.getPos());

                stack.pushPose(); //Save position again
                stack.translate(targetPos.getX(), targetPos.getY(), targetPos.getZ());

                try {
                    if (state.getRenderShape() == RenderShape.MODEL) {
                        dispatcher.renderSingleBlock(state, stack, mutatedBuffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

                    }
                } catch (Exception e) {
                    BuildingGadgets.LOG.trace("Caught exception whilst rendering {}.", state, e);
                }

                stack.popPose(); // Load the position we saved earlier
            }
            stack.popPose(); //Load after loop
        });
        //Vec3 projectedView2 = getMc().gameRenderer.getMainCamera().getPosition();
        //Vec3 startPosView = new Vec3(startPos.getX(), startPos.getY(), startPos.getZ());
        //projectedView2 = projectedView2.subtract(startPosView);
        //renderBuffer.sort((float) projectedView2.x(), (float) projectedView2.y(), (float) projectedView2.z());
        matrix.translate(startPos.getX(), startPos.getY(), startPos.getZ());
        renderBuffer.render(matrix.last().pose()); //Actually draw whats in the buffer
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

                VertexBuffer vbo = new VertexBuffer();
                vbo.bind();
                vbo.upload(builder.end());
                return vbo;
            });

            return new MultiVBORenderer(buffers);
        }

        private final ImmutableMap<RenderType, VertexBuffer> buffers;

        protected MultiVBORenderer(Map<RenderType, VertexBuffer> buffers) {
            this.buffers = ImmutableMap.copyOf(buffers);
        }

        //TODO: Sort verts
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
            RenderSystem.setShader(GameRenderer::getPositionTexLightmapColorShader);
            buffers.forEach((rt, vbo) -> {

                rt.setupRenderState();
                vbo.bind();
                vbo.drawWithShader(modelViewMatrix, RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
                rt.clearRenderState();
            });
        }

        @Override
        public void close() {
            for (VertexBuffer value : buffers.values()) {
                value.close();
            }
        }
    }
}
