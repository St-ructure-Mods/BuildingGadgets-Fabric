package com.direwolf20.buildinggadgets.common.tainted.inventory;

import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.UniqueItem;
import com.google.common.collect.ImmutableMultiset;

/**
 * The result of a match by an {@link IItemIndex}. Allows access to the {@link #getMatchedList() matched MaterialList}, the Items which were found, the chosen option and of course
 * whether the match was a success or not.
 */
public final class MatchResult {
    private final MaterialList matchedList;
    private final ImmutableMultiset<UniqueItem> foundItems;
    private final ImmutableMultiset<UniqueItem> chosenOption;
    private final boolean isSuccess;

    public static MatchResult success(MaterialList matchedList, ImmutableMultiset<UniqueItem> foundItems, ImmutableMultiset<UniqueItem> chosenOption) {
        return new MatchResult(matchedList, foundItems, chosenOption, true);
    }

    public static MatchResult failure() {
        return new MatchResult(MaterialList.empty(), ImmutableMultiset.of(), ImmutableMultiset.of(), false);
    }

    public static MatchResult failure(MaterialList matchedList, ImmutableMultiset<UniqueItem> foundItems, ImmutableMultiset<UniqueItem> chosenOption) {
        return new MatchResult(matchedList, foundItems, chosenOption, false);
    }

    MatchResult(MaterialList matchedList, ImmutableMultiset<UniqueItem> foundItems, ImmutableMultiset<UniqueItem> chosenOption, boolean isSuccess) {
        this.matchedList = matchedList;
        this.foundItems = foundItems;
        this.chosenOption = chosenOption;
        this.isSuccess = isSuccess;
    }

    public MaterialList getMatchedList() {
        return matchedList;
    }

    /**
     * If this result is a success, then this will be a reference to the same set returned by {@link #getChosenOption()} as all the {@link UniqueItem unique objects}
     * in there will be available. If this match is not a success, then this will return the amount of found Items for all {@link UniqueItem unique objects} across
     * all options.
     *
     * @return The found items
     */
    public ImmutableMultiset<UniqueItem> getFoundItems() {
        return foundItems;
    }

    public ImmutableMultiset<UniqueItem> getChosenOption() {
        return chosenOption;
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
