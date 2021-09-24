package com.direwolf20.buildinggadgets.client.models;

import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
//import net.minecraftforge.client.MinecraftForgeClient;
//import net.minecraftforge.client.model.data.IDynamicBakedModel;
//import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ConstructionBakedModel implements BakedModel {
    private BlockState facadeState;
    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return null;
    }

    @Override
    public ItemTransforms getTransforms() {
        return null;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, Random random) {
        BakedModel model;
        RenderType layer = ItemBlockRenderTypes.getChunkRenderType(blockState);
        if (facadeState == null || facadeState == Blocks.AIR.defaultBlockState())
            facadeState = OurBlocks.CONSTRUCTION_DENSE_BLOCK.defaultBlockState();
        if (layer != null) { // always render in the null layer or the block-breaking textures don't show up
            return Collections.emptyList();
        }
        model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(facadeState);
        return model.getQuads(facadeState, direction, random);

    }

    @Override
    public boolean useAmbientOcclusion() {
        if (facadeState == null) return false;
        BakedModel model;
        model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(facadeState);
        return model.useAmbientOcclusion();
    }

//    @Override
//    public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
//        return this.getBakedModel().getParticleTexture(data);
//    }

    @Override
    public ItemOverrides getOverrides() {
        return null;
    }
}
