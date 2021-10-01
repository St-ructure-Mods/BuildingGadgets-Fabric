package com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IUniqueObjectSerializer extends IForgeRegistryEntry<IUniqueObjectSerializer> {
    CompoundTag serialize(UniqueItem item, boolean persisted);

    UniqueItem deserialize(CompoundTag res);

    JsonSerializer<UniqueItem> asJsonSerializer(boolean printName, boolean extended);

    JsonDeserializer<UniqueItem> asJsonDeserializer();
}
