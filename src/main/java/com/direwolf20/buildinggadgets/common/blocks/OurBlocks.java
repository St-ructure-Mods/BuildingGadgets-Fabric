package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;

public final class OurBlocks {
    private OurBlocks() {}

    public static void registerBlocks() {
        Registry.register(Registry.BLOCK, BuildingGadgets.id("effect_block"), EFFECT_BLOCK);
        Registry.register(Registry.BLOCK, BuildingGadgets.id("construction_block"), CONSTRUCTION_BLOCK);
        Registry.register(Registry.BLOCK, BuildingGadgets.id("construction_block_dense"), CONSTRUCTION_DENSE_BLOCK);
        Registry.register(Registry.BLOCK, BuildingGadgets.id("construction_block_powder"), CONSTRUCTION_POWDER_BLOCK);
        Registry.register(Registry.BLOCK, BuildingGadgets.id("template_manager"), TEMPLATE_MANGER_BLOCK);
    }

    public static final Block EFFECT_BLOCK = new EffectBlock();
    public static final Block CONSTRUCTION_BLOCK = new ConstructionBlock();
    public static final Block CONSTRUCTION_DENSE_BLOCK = new ConstructionBlockDense();
    public static final Block CONSTRUCTION_POWDER_BLOCK = new ConstructionBlockPowder();
    public static final Block TEMPLATE_MANGER_BLOCK = new TemplateManager();
}
