package com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.nbt.CompoundTag;

public interface IUniqueObjectSerializer {
    CompoundTag serialize(ItemVariant item, boolean persisted);

    ItemVariant deserialize(CompoundTag res);

    JsonSerializer<ItemVariant> asJsonSerializer(boolean printName, boolean extended);

    JsonDeserializer<ItemVariant> asJsonDeserializer();
}
