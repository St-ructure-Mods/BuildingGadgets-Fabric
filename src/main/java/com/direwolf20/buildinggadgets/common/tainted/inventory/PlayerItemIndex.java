package com.direwolf20.buildinggadgets.common.tainted.inventory;

import com.direwolf20.buildinggadgets.common.tainted.inventory.handle.IObjectHandle;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Multisets;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;
import java.util.List;

/**
 * Item Index representation all Items accessible for the Player by BuildingGadgets.
 * To allow for better performance, the Items in the player's Inventory are indexed by their Item and upon query only those with the appropriate Item need to be iterated.
 */
public final class PlayerItemIndex implements IItemIndex {
    //use a class map first, to allow for non-Item IUniqueObjects...
    private List<IObjectHandle> handles;
    private List<Storage<ItemVariant>> insertProviders;
    private final ItemStack stack;
    private final Player player;

    public PlayerItemIndex(ItemStack stack, Player player) {
        this.stack = stack;
        this.player = player;
        reIndex();
    }

    @Override
    public Multiset<ItemVariant> insert(Multiset<ItemVariant> items, TransactionContext transaction) {
        Multiset<ItemVariant> copy = HashMultiset.create(items);
        Multiset<ItemVariant> toRemove = HashMultiset.create();
        for (Multiset.Entry<ItemVariant> entry : copy.entrySet()) {
            int remainingCount = insertObject(entry.getElement(), entry.getCount(), transaction);
            if (remainingCount < entry.getCount())
                toRemove.add(entry.getElement(), entry.getCount() - remainingCount);
        }
        Multisets.removeOccurrences(copy, toRemove);

        return copy;
    }

    private int insertObject(ItemVariant obj, int count, TransactionContext transaction) {
        int remainingCount = insertIntoProviders(stack, count, transaction);
        if (remainingCount == 0)
            return 0;

// this is extremely buggy and poorly planned out code.
//        insertIntoEmptyHandles(stack, remainingCount, transaction);
//        if (remainingCount == 0)
//            return 0;


        spawnRemainder(stack, remainingCount);

        return 0;
    }

    private int insertIntoProviders(ItemStack stack, int remainingCount, TransactionContext transaction) {
        for (Storage<ItemVariant> insertProvider : insertProviders) {
            remainingCount -= insertProvider.insert(ItemVariant.of(stack), remainingCount, transaction);
            if (remainingCount <= 0)
                return 0;
        }
        return remainingCount;
    }

    // todo: fix or rewrite. has many root issues:
    //       uses methods not intended for forge, has the ability to replace stacks, indexes players inventory even though we already handle the players inventory,
    //       doesn't check for a valid slot, ignores the IItemHandler contract. Maybe more
    private int insertIntoEmptyHandles(ItemStack stack, int remainingCount, TransactionContext transaction) {
//        List<IObjectHandle<?>> emptyHandles = handleMap
//                .computeIfAbsent(Item.class, c -> new HashMap<>())
//                .getOrDefault(Items.AIR, ImmutableList.of());
//
//        for (Iterator<IObjectHandle<?>> it = emptyHandles.iterator(); it.hasNext() && remainingCount >= 0; ) {
//            IObjectHandle<?> handle = it.next();
//            ItemVariant item = ItemVariant.of(stack);
//
//            int match = handle.insert(item, remainingCount, transaction);
//            if (match > 0)
//                remainingCount -= match;
//
//            handleMap.get(Item.class)
//                    .computeIfAbsent(item.getIndexObject(), i -> new ArrayList<>())
//                    .add(handle);
//
//            if (remainingCount <= 0)
//                return 0;
//        }
//
//        return remainingCount;
        return 0;
    }

    private void spawnRemainder(ItemStack stack, int remainingCount) {
        while (remainingCount > 0) {
            ItemStack copy = stack.copy();
            copy.setCount(Math.min(remainingCount, copy.getMaxStackSize()));
            remainingCount -= copy.getCount();
            ItemEntity itemEntity = new ItemEntity(player.level, player.getX(), player.getY(), player.getZ(), copy);
            player.level.addFreshEntity(itemEntity);
        }
    }

    @Override
    public void reIndex() {
        this.handles = InventoryHelper.indexMap(stack, player);
        this.insertProviders = InventoryHelper.getHandlers(stack, player);
    }

    @Override
    public MatchResult tryMatch(MaterialList list) {
        try (Transaction transaction = Transaction.openOuter()) {
            MatchResult result = null;

            for (ImmutableMultiset<ItemVariant> multiset : list) {
                result = match(list, multiset, transaction);
                if (result.isSuccess())
                    return MatchResult.success(list, result.getFoundItems(), multiset);
            }

            return result == null ? MatchResult.success(list, ImmutableMultiset.of(), ImmutableMultiset.of()) : evaluateFailingOptionFoundItems(list, transaction);
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
        boolean failure = false;
        for (Entry<ItemVariant> entry : multiset.entrySet()) {
            int remainingCount = entry.getCount();
            for (Iterator<IObjectHandle> it = handles.iterator(); it.hasNext() && remainingCount >= 0; ) {
                IObjectHandle handle = it.next();
                int match = handle.match(entry.getElement(), remainingCount, transaction);
                if (match > 0)
                    remainingCount -= match;
                if (handle.shouldCleanup()) {
                    it.remove();
                }
            }
            remainingCount = Math.max(0, remainingCount);
            if (remainingCount > 0)
                failure = true;
            availableBuilder.addCopies(entry.getElement(), entry.getCount() - remainingCount);
        }
        if (failure)
            return MatchResult.failure(list, availableBuilder.build(), ImmutableMultiset.of());
        return MatchResult.success(list, availableBuilder.build(), ImmutableMultiset.of());
    }

    @Override
    public boolean applyMatch(MatchResult result) {
        if (!result.isSuccess()) {
            return false;
        }

        try (Transaction transaction = Transaction.openOuter()) {
            return match(result.getMatchedList(), result.getChosenOption(), transaction).isSuccess();
        }
    }
}
