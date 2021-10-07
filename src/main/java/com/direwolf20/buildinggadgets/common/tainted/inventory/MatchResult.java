package com.direwolf20.buildinggadgets.common.tainted.inventory;

import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import com.google.common.collect.ImmutableMultiset;

/**
 * The result of a match by an {@link IItemIndex}. Allows access to the {@link #getMatchedList() matched MaterialList}, the Items which were found, the chosen option and of course
 * whether the match was a success or not.
 */
public record MatchResult(MaterialList matchedList, ImmutableMultiset<ItemVariant> foundItems, ImmutableMultiset<ItemVariant> chosenOption, boolean isSuccess) {
    public static MatchResult success(MaterialList matchedList, ImmutableMultiset<ItemVariant> foundItems, ImmutableMultiset<ItemVariant> chosenOption) {
        return new MatchResult(matchedList, foundItems, chosenOption, true);
    }

    public static MatchResult failure() {
        return new MatchResult(MaterialList.empty(), ImmutableMultiset.of(), ImmutableMultiset.of(), false);
    }

    public static MatchResult failure(MaterialList matchedList, ImmutableMultiset<ItemVariant> foundItems, ImmutableMultiset<ItemVariant> chosenOption) {
        return new MatchResult(matchedList, foundItems, chosenOption, false);
    }

    /**
     * If this result is a success, then this will be a reference to the same set returned by {@link #getChosenOption()} as all the {@link ItemVariant unique objects}
     * in there will be available. If this match is not a success, then this will return the amount of found Items for all {@link ItemVariant unique objects} across
     * all options.
     *
     * @return The found items
     */
    public ImmutableMultiset<ItemVariant> getFoundItems() {
        return foundItems();
    }

    public ImmutableMultiset<ItemVariant> getChosenOption() {
        return chosenOption();
    }
}
