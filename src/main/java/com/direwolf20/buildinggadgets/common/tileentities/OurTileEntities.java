package com.direwolf20.buildinggadgets.common.tileentities;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class OurTileEntities {
    public static BlockEntityType<EffectBlockTileEntity> EFFECT_BLOCK_TILE_ENTITY;
    public static BlockEntityType<TemplateManagerTileEntity> TEMPLATE_MANAGER_TILE_ENTITY;

    public static void initBE() {
        EFFECT_BLOCK_TILE_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, BuildingGadgets.id("effect_block_tile"), FabricBlockEntityTypeBuilder.create(EffectBlockTileEntity::new, OurBlocks.EFFECT_BLOCK).build());
        TEMPLATE_MANAGER_TILE_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, BuildingGadgets.id("template_manager_tile"), FabricBlockEntityTypeBuilder.create(TemplateManagerTileEntity::new, OurBlocks.TEMPLATE_MANGER_BLOCK).build());
    }
}
