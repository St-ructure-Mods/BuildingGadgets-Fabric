package com.direwolf20.buildinggadgets.common.tainted.inventory;

import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;

/**
 * Item Index representation all Items accessible for the Player by BuildingGadgets.
 * To allow for better performance, the Items in the player's Inventory are indexed by their Item and upon query only those with the appropriate Item need to be iterated.
 */
public final class PlayerItemIndex implements IItemIndex {

    private final Player player;
    private final Storage<ItemVariant> storage;

    public PlayerItemIndex(ItemStack stack, Player player) {
        this.player = player;
        this.storage = InventoryHelper.getHandlers(stack, player);
    }

    @Override
    public void insert(Multiset<ItemVariant> items, TransactionContext transaction) {
        for (Multiset.Entry<ItemVariant> entry : items.entrySet()) {
            insertObject(entry.getElement(), entry.getCount(), transaction);
        }
    }

    private void insertObject(ItemVariant obj, int count, TransactionContext transaction) {
        int remainingCount = insertIntoProviders(obj, count, transaction);

        if (remainingCount != 0) {
            PlayerInventoryStorage.of(player).drop(obj, count, transaction);
        }
    }

    private int insertIntoProviders(ItemVariant variant, int toInsert, TransactionContext transaction) {
        return (int) (toInsert - storage.insert(variant, toInsert, transaction));
    }

    @Override
    public MatchResult match(MaterialList list, TransactionContext transaction) {
        MatchResult result = null;

        for (ImmutableMultiset<ItemVariant> multiset : list) {
            try (Transaction inner = Transaction.openNested(transaction)) {
                result = match(list, multiset, inner);

                if (result.isSuccess()) {
                    inner.commit();
                    return MatchResult.success(list, result.getFoundItems(), multiset);
                }
            }
        }

        if (result == null) {
            return MatchResult.success(list, ImmutableMultiset.of(), ImmutableMultiset.of());
        } else {
            return evaluateFailingOptionFoundItems(list, transaction);
        }
    }

    private MatchResult evaluateFailingOptionFoundItems(MaterialList list, TransactionContext transaction) {
        Multiset<ItemVariant> multiset = HashMultiset.create();

        for (ImmutableMultiset<ItemVariant> option : list.getItemOptions()) {
            for (Entry<ItemVariant> entry : option.entrySet()) {
                multiset.setCount(entry.getElement(), Math.max(multiset.count(entry.getElement()), entry.getCount()));
            }
        }

        multiset.addAll(list.getRequiredItems());
        MatchResult result = match(list, multiset, transaction);
        if (result.isSuccess())
            throw new RuntimeException("This should not be possible! The the content changed between matches?!?");
        Iterator<ImmutableMultiset<ItemVariant>> it = list.iterator();
        return it.hasNext() ? MatchResult.failure(list, result.getFoundItems(), it.next()) : result;
    }

    private MatchResult match(MaterialList list, Multiset<ItemVariant> multiset, TransactionContext transaction) {
        ImmutableMultiset.Builder<ItemVariant> availableBuilder = ImmutableMultiset.builder();
        boolean success = true;

        for (Entry<ItemVariant> entry : multiset.entrySet()) {
            int remainingCount = entry.getCount();
            int extracted = (int) storage.extract(entry.getElement(), remainingCount, transaction);
            success &= extracted == remainingCount;
            availableBuilder.addCopies(entry.getElement(), extracted);
        }

        if (success) {
            return MatchResult.success(list, availableBuilder.build(), ImmutableMultiset.of());
        } else {
            return MatchResult.failure(list, availableBuilder.build(), ImmutableMultiset.of());
        }
    }

}
