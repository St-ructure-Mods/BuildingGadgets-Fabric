package com.direwolf20.buildinggadgets.common.events;

import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BreakEventHandler implements PlayerBlockBreakEvents.After {

    @Override
    public void afterBlockBreak(Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        ItemStack heldItem = AbstractGadget.getGadget(player);

        if (heldItem.isEmpty()) {
            return;
        }

        if (world instanceof ServerLevel level) {
            List<ItemStack> drops = Block.getDrops(state, level, pos, blockEntity);

            for (ItemStack item : drops) {
                InventoryHelper.giveItem(item, player, level);
            }
        }
    }
}
