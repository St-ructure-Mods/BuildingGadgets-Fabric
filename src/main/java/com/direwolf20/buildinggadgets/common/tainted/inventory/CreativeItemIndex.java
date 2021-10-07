package com.direwolf20.buildinggadgets.common.tainted.inventory;

import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.Iterator;

/**
 * Represents the Items available in Creative Mode: everything. All queries will succeed. Always.
 */
public final class CreativeItemIndex implements IItemIndex {
    @Override
    public void insert(Multiset<ItemVariant> items, TransactionContext transaction) {
    }

    @Override
    public MatchResult match(MaterialList list, TransactionContext transaction) {
        Iterator<ImmutableMultiset<ItemVariant>> it = list.iterator();
        ImmutableMultiset<ItemVariant> chosen = it.hasNext() ? it.next() : ImmutableMultiset.of();
        return MatchResult.success(list, chosen, chosen);
    }
}
