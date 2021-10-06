package com.direwolf20.buildinggadgets.common;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record Location(ResourceKey<Level> level, BlockPos blockPos) {
}
