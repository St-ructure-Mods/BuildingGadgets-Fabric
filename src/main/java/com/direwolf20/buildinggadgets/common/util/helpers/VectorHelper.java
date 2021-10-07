package com.direwolf20.buildinggadgets.common.util.helpers;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class VectorHelper {

    public static BlockHitResult getLookingAt(Player player, ItemStack tool) {
        return getLookingAt(player, AbstractGadget.shouldRayTraceFluid(tool) ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE);
    }

    public static BlockHitResult getLookingAt(Player player, boolean shouldRayTrace) {
        return getLookingAt(player, shouldRayTrace ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE);
    }

    public static BlockHitResult getLookingAt(Player player, ClipContext.Fluid rayTraceFluid) {
        double rayTraceRange = BuildingGadgets.config.GENERAL.rayTraceRange;
        HitResult result = player.pick(rayTraceRange, 0f, rayTraceFluid != ClipContext.Fluid.NONE);

        return (BlockHitResult) result;
    }

}
