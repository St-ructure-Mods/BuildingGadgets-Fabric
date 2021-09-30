package com.direwolf20.buildinggadgets.common.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockPlaceEvent {

    private BlockPlaceEvent() {}

    public static final Event<Place> ON_PLACE = EventFactory.createArrayBacked(Place.class, (listeners) -> ((serverPlayer, level, itemStack, interactionHand, blockHitResult) -> {
        for(Place event : listeners) {
            event.OnPlace(serverPlayer, level, itemStack, interactionHand, blockHitResult);
        }
    }));

    public interface Place {
        void OnPlace(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, BlockHitResult blockHitResult);
    }
}
