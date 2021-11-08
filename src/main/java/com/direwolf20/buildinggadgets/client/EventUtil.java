package com.direwolf20.buildinggadgets.client;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

import net.minecraft.core.Registry;

import java.util.Comparator;

/**
 * This class was adapted from code written by Vazkii
 * Thanks Vazkii!!
 */
@Environment(EnvType.CLIENT)
public class EventUtil {
    public static final Comparator<Multiset.Entry<ItemVariant>> ENTRY_COMPARATOR = Comparator
            .<Multiset.Entry<ItemVariant>, Integer>comparing(Entry::getCount)
            .reversed()
            .thenComparing(e -> Registry.ITEM.getKey(e.getElement().getItem()));

    public static final int STACKS_PER_LINE = 8;
}
