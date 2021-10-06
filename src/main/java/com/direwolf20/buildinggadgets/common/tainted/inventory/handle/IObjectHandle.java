package com.direwolf20.buildinggadgets.common.tainted.inventory.handle;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

public interface IObjectHandle {

    int match(ItemVariant item, int count, boolean simulate);

    int insert(ItemVariant item, int count, boolean simulate);

    boolean shouldCleanup();
}
