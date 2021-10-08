package com.direwolf20.buildinggadgets.client.renderer;


import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.OptionalDouble;


public class OurRenderTypes extends RenderType {

    private static final LineStateShard THICK_LINES = new LineStateShard(OptionalDouble.of(3.0D));

    public static final RenderType RenderBlock = create("GadgetRenderBlock",
            DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
//                    .setShadeModelState(SMOOTH_SHADE)
                    .setShaderState(RenderStateShard.BLOCK_SHADER)
                    .setLightmapState(LIGHTMAP)
                    .setTextureState(BLOCK_SHEET_MIPPED) //BLOCK_SHEET_MIPPED (mcp) = BLOCK_SHEET_MIPPED (yarn)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING) // view_offset_z_layering
                    .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .createCompositeState(false));

    public static final RenderType MissingBlockOverlay = create("GadgetMissingBlockOverlay",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.BLOCK_SHADER)
                    .setLayeringState(POLYGON_OFFSET_LAYERING) // view_offset_z_layering
                    .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .createCompositeState(false));

    public static final RenderType CopyGadgetLines = create("GadgetCopyLines",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.LINE_STRIP, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLineState(new LineStateShard(OptionalDouble.of(2.0D)))
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING) // view_offset_z_layering
                    .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    public static final RenderType CopyPasteRenderBlock = create("CopyPasteRenderBlock",
            DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.BLOCK_SHADER)
//                    .setShadeModelState(SMOOTH_SHADE)
                    .setLightmapState(LIGHTMAP)
                    .setTextureState(BLOCK_SHEET_MIPPED) //BLOCK_SHEET_MIPPED (mcp) = BLOCK_SHEET_MIPPED (yarn)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING) // view_offset_z_layering
                    .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    public static final RenderType BlockOverlay = create("BGBlockOverlay",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.BLOCK_SHADER)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING) // view_offset_z_layering
                    .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .createCompositeState(false));

    public OurRenderTypes(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
    }
}
