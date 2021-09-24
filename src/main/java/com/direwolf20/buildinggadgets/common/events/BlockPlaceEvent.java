package com.direwolf20.buildinggadgets.common.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockPlaceEvent {

    private BlockPlaceEvent() {}

    public static final Event<Place> ON_PLACE = EventFactory.createArrayBacked(Place.class, (listeners) -> ((world, entity, pos, state, itemStack) -> {
        for(Place event : listeners) {
            event.OnPlace(world, entity, pos, state, itemStack);
        }
    }));

    public interface Place {
        void OnPlace(Level world, @Nullable /* Nullable */ LivingEntity entity, BlockPos pos, BlockState state, ItemStack itemStack);
    }
}
