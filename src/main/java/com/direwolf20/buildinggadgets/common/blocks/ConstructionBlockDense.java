package com.direwolf20.buildinggadgets.common.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;

public class ConstructionBlockDense extends Block {
    public ConstructionBlockDense() {
        super(FabricBlockSettings.of(Material.STONE).strength(3f, 0f).breakByTool(FabricToolTags.PICKAXES));
    }
}
