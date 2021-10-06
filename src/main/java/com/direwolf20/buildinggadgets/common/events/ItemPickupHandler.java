package com.direwolf20.buildinggadgets.common.events;

import com.direwolf20.buildinggadgets.common.items.ConstructionPaste;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ItemPickupHandler implements ItemPickupCallback {

    @Override
    public void onPickup(ItemEntity item, Player player) {
        ItemStack itemStack = item.getItem();

        if (itemStack.getItem() instanceof ConstructionPaste) {
            InventoryHelper.addPasteToContainer(player, itemStack);
            item.setItem(itemStack);
        }
    }
}
