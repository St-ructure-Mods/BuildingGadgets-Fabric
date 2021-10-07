package com.direwolf20.buildinggadgets.common.tainted.inventory;

import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * An {@link IItemIndex} which instead of inserting or extracting Items from the backing {@link IItemIndex} keeps record of
 * everything that was attempted to be inserted and then simulates extraction/insertion of the combination of the "record" and
 * the new Items.
 */
public final class RecordingItemIndex implements IItemIndex {
    private final IItemIndex other;
    private final Multiset<ItemVariant> extractedItems;
    private final Multiset<ItemVariant> insertedItems;

    public RecordingItemIndex(IItemIndex other) {
        this.other = other;
        this.extractedItems = HashMultiset.create();
        this.insertedItems = HashMultiset.create();
    }

    @Override
    public void insert(Multiset<ItemVariant> items, TransactionContext transaction) {
        other.insert(items, transaction);
        insertedItems.addAll(items);
    }

    @Override
    public MatchResult match(MaterialList list, TransactionContext transaction) {
        return other.match(MaterialList.and(list, MaterialList.of(extractedItems)), transaction);
    }

    @Override
    public MatchResult match(Multiset<ItemVariant> items) {
        return other.match(ImmutableMultiset.<ItemVariant>builder()
                .addAll(items)
                .addAll(extractedItems)
                .build());
    }

}
