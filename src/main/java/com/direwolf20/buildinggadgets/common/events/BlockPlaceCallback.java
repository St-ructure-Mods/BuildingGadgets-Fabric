package com.direwolf20.buildinggadgets.common.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public interface BlockPlaceCallback {

    Event<BlockPlaceCallback> ON_PLACE = EventFactory.createArrayBacked(BlockPlaceCallback.class, (listeners) -> (serverPlayer, level, itemStack, interactionHand, blockHitResult) -> {
        for (BlockPlaceCallback event : listeners) {
            event.onPlace(serverPlayer, level, itemStack, interactionHand, blockHitResult);
        }
    });

    void onPlace(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, BlockHitResult blockHitResult);
}
