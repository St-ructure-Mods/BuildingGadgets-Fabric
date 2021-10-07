package com.direwolf20.buildinggadgets.common.tainted.inventory.materials;

import com.direwolf20.buildinggadgets.common.util.ref.JsonKeys;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.collect.*;
import com.google.common.collect.Multiset.Entry;
import com.google.gson.*;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Objects;

record SimpleMaterialListEntry(
        ImmutableMultiset<ItemVariant> items) implements MaterialListEntry<SimpleMaterialListEntry> {
    static final MaterialListEntry.Serializer<SimpleMaterialListEntry> SERIALIZER = new Serializer();

    SimpleMaterialListEntry(ImmutableMultiset<ItemVariant> items) {
        this.items = Objects.requireNonNull(items, "Cannot have a SimpleMaterialListEntry without any Materials!");
    }

    ImmutableMultiset<ItemVariant> getItems() {
        return items;
    }

    @Override
    public @NotNull PeekingIterator<ImmutableMultiset<ItemVariant>> iterator() {
        return Iterators.peekingIterator(Iterators.singletonIterator(items));
    }

    @Override
    public MaterialListEntry.Serializer<SimpleMaterialListEntry> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public SimpleMaterialListEntry simplify() {
        return this;
    }

    private static class Serializer implements MaterialListEntry.Serializer<SimpleMaterialListEntry> {
        private static final Comparator<Entry<ItemVariant>> COMPARATOR = Comparator
                .<Entry<ItemVariant>, ResourceLocation>comparing(e -> Registry.ITEM.getKey(e.getElement().getItem()))
                .thenComparingInt(Entry::getCount);

        @Override
        public SimpleMaterialListEntry readFromNBT(CompoundTag nbt, boolean persisted) {
            ListTag nbtList = nbt.getList(NBTKeys.KEY_DATA, NbtType.COMPOUND);
            ImmutableMultiset.Builder<ItemVariant> builder = ImmutableMultiset.builder();
            for (Tag nbtEntry : nbtList) {
                CompoundTag compoundEntry = (CompoundTag) nbtEntry;
                builder.addCopies(
                        ItemVariant.fromNbt((compoundEntry.getCompound(NBTKeys.KEY_DATA))),
                        compoundEntry.getInt(NBTKeys.KEY_COUNT));
            }
            return new SimpleMaterialListEntry(builder.build());
        }

        @Override
        public CompoundTag writeToNBT(SimpleMaterialListEntry listEntry, boolean persisted) {
            CompoundTag res = new CompoundTag();
            ListTag nbtList = new ListTag();
            for (Entry<ItemVariant> entry : listEntry.getItems().entrySet()) {
                CompoundTag nbtEntry = new CompoundTag();
                nbtEntry.put(NBTKeys.KEY_DATA, entry.getElement().toNbt());
                nbtEntry.putInt(NBTKeys.KEY_COUNT, entry.getCount());
                nbtList.add(nbtEntry);
            }
            res.put(NBTKeys.KEY_DATA, nbtList);
            return res;
        }

        @Override
        public JsonSerializer<SimpleMaterialListEntry> asJsonSerializer(boolean printName, boolean extended) {
            return (src, typeOfSrc, context) -> {
                Multiset<ItemVariant> set = src.getItems();
                JsonArray jsonArray = new JsonArray();
                for (Entry<ItemVariant> entry : ImmutableList.sortedCopyOf(COMPARATOR, set.entrySet())) {
                    JsonElement element = Dynamic.convert(NbtOps.INSTANCE, JsonOps.INSTANCE, entry.getElement().toNbt());
                    JsonObject obj = new JsonObject();
                    obj.addProperty(JsonKeys.MATERIAL_LIST_ITEM_COUNT, entry.getCount());
                    obj.add(JsonKeys.MATERIAL_LIST_ITEM, element);
                    jsonArray.add(obj);
                }
                return jsonArray;
            };
        }

        @Override
        public JsonDeserializer<SimpleMaterialListEntry> asJsonDeserializer() {
            return (json, typeOfT, context) -> {
                JsonArray array = json.getAsJsonArray();
                ImmutableMultiset.Builder<ItemVariant> items = ImmutableMultiset.builder();
                for (JsonElement element : array) {
                    JsonObject object = element.getAsJsonObject();
                    int count = object.getAsJsonPrimitive(JsonKeys.MATERIAL_LIST_ITEM_COUNT).getAsInt();
                    ItemVariant item = ItemVariant.fromNbt((CompoundTag) Dynamic.convert(JsonOps.INSTANCE, NbtOps.INSTANCE, object.get(JsonKeys.MATERIAL_LIST_ITEM)));
                    items.addCopies(item, count);
                }
                return new SimpleMaterialListEntry(items.build());
            };
        }

        @Override
        public ResourceLocation getRegistryName() {
            return NBTKeys.SIMPLE_SERIALIZER_ID;
        }
    }
}
