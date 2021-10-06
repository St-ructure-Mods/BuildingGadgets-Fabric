package com.direwolf20.buildinggadgets.common.tainted.inventory.handle;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public interface IObjectHandle {

    int match(ItemVariant item, int count, TransactionContext transaction);

    int insert(ItemVariant item, int count, TransactionContext transaction);

    boolean shouldCleanup();
}
