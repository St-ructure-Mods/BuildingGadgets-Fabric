package com.direwolf20.buildinggadgets.common.util.helpers;

import com.direwolf20.buildinggadgets.common.tainted.Tainted;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.collect.Multiset;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Tuple;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Utility class providing additional Methods for reading and writing array's which are not normally provided as
 * NBT-Objects from Minecraft.
 */

@Tainted(reason = "Everything here is single use. It should not be a helper")
public class NBTHelper {

    public static <T> ListTag writeIterable(Iterable<T> iterable, Function<? super T, ? extends Tag> serializer) {
        ListTag list = new ListTag();

        for (T t : iterable) {
            list.add(serializer.apply(t));
        }

        return list;
    }

    public static <K, V> ListTag serializeMap(Map<K, V> map, Function<? super K, ? extends Tag> keySerializer, Function<? super V, ? extends Tag> valueSerializer) {
        ListTag list = new ListTag();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            CompoundTag compound = new CompoundTag();
            compound.put(NBTKeys.MAP_SERIALIZE_KEY, keySerializer.apply(entry.getKey()));
            compound.put(NBTKeys.MAP_SERIALIZE_VALUE, valueSerializer.apply(entry.getValue()));
            list.add(compound);
        }
        return list;
    }

    public static <V> ListTag serializeUUIDMap(Map<UUID, V> map, Function<? super V, ? extends Tag> valueSerializer) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, V> entry : map.entrySet()) {
            CompoundTag compound = new CompoundTag();
            compound.putUUID(NBTKeys.MAP_SERIALIZE_KEY, entry.getKey());
            compound.put(NBTKeys.MAP_SERIALIZE_VALUE, valueSerializer.apply(entry.getValue()));
            list.add(compound);
        }
        return list;
    }

    public static <K, V> Map<K, V> deserializeMap(ListTag list, Map<K, V> toAppendTo, Function<Tag, ? extends K> keyDeserializer, Function<Tag, ? extends V> valueDeserializer) {
        for (Tag nbt : list) {
            if (nbt instanceof CompoundTag compound) {
                toAppendTo.put(
                        keyDeserializer.apply(compound.get(NBTKeys.MAP_SERIALIZE_KEY)),
                        valueDeserializer.apply(compound.get(NBTKeys.MAP_SERIALIZE_VALUE))
                );
            }
        }
        return toAppendTo;
    }

    public static <V> Map<UUID, V> deserializeUUIDMap(ListTag list, Map<UUID, V> toAppendTo, Function<Tag, ? extends V> valueDeserializer) {
        for (Tag nbt : list) {
            if (nbt instanceof CompoundTag compound) {
                toAppendTo.put(
                        compound.getUUID(NBTKeys.MAP_SERIALIZE_KEY),
                        valueDeserializer.apply(compound.get(NBTKeys.MAP_SERIALIZE_VALUE))
                );
            }
        }
        return toAppendTo;
    }

    public static <T, C extends Collection<T>> C deserializeCollection(ListTag list, C toAppendTo, Function<Tag, ? extends T> elementDeserializer) {
        for (Tag nbt : list) {
            toAppendTo.add(elementDeserializer.apply(nbt));
        }
        return toAppendTo;
    }

    public static <T> Multiset<T> deserializeMultisetEntries(ListTag list, Multiset<T> toAppendTo, Function<Tag, Tuple<? extends T, Integer>> entryDeserializer) {
        list.stream().map(entryDeserializer).forEach(p -> toAppendTo.add(p.getA(), p.getB()));
        return toAppendTo;
    }
}
