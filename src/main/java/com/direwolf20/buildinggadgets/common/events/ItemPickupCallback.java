package com.direwolf20.buildinggadgets.common.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

public interface ItemPickupCallback {

    Event<ItemPickupCallback> EVENT = EventFactory.createArrayBacked(ItemPickupCallback.class, callbacks -> (item, player) -> {
        for (ItemPickupCallback callback : callbacks) {
            callback.onPickup(item, player);
        }
    });

    void onPickup(ItemEntity item, Player player);
}
