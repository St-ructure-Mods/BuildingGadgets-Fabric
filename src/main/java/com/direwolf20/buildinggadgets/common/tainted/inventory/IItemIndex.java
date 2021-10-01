package com.direwolf20.buildinggadgets.common.tainted.inventory;

import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.UniqueItem;
import com.google.common.collect.Multiset;

/**
 * Represents Index for accessible Items. It allows for extraction/insertion into some kind of UniqueItem container(s).
 * An Implementation must also handle the options represented by a MaterialList correctly - test all available options until the first one matches, or no other is left
 * to search.
 * <p>
 * To update this index with contents that were inserted/extracted independently, you'll need to call {@link #reIndex()}.
 *
 * @see PlayerItemIndex
 * @see CreativeItemIndex
 */
public interface IItemIndex {
    default Multiset<UniqueItem> insert(Multiset<UniqueItem> items) {
        return insert(items, false);
    }

    //returns the remaining items
    Multiset<UniqueItem> insert(Multiset<UniqueItem> items, boolean simulate);

    void reIndex();

    MatchResult tryMatch(MaterialList list);

    default MatchResult tryMatch(Multiset<UniqueItem> items) {
        return tryMatch(MaterialList.of(items));
    }

    boolean applyMatch(MatchResult result);

    default boolean applyMatch(MaterialList list) {
        MatchResult result = tryMatch(list);
        return result.isSuccess() && applyMatch(result);
    }
}
