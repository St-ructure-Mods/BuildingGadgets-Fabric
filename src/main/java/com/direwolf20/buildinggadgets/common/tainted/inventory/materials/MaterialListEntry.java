package com.direwolf20.buildinggadgets.common.tainted.inventory.materials;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.PeekingIterator;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/* This is currently hidden, to avoid having yet another Registry
 - if it turns out someone needs something else then the default implementations, we can still add that and make it public
 */
interface MaterialListEntry<T extends MaterialListEntry<T>> extends Iterable<ImmutableMultiset<ItemVariant>> {
    @Override
    PeekingIterator<ImmutableMultiset<ItemVariant>> iterator();

    Serializer<T> getSerializer();

    MaterialListEntry<?> simplify();

    interface Serializer<T extends MaterialListEntry<T>> {
        ResourceLocation getRegistryName();

        T readFromNBT(CompoundTag nbt, boolean persisted);

        CompoundTag writeToNBT(T entry, boolean persisted);

        JsonSerializer<T> asJsonSerializer(boolean printName, boolean extended);

        JsonDeserializer<T> asJsonDeserializer();
    }
}
