package com.direwolf20.buildinggadgets.common.tainted.template;

import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataSerializer;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileEntityData;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.NBTTileEntityData;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.IUniqueObjectSerializer;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.UniqueItem;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.base.Preconditions;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;

public final class SerialisationSupport {
    private SerialisationSupport() {
    }

    // TODO: Register as Reference.TileDataSerializerReference.DUMMY_SERIALIZER_RL
    private static final ITileDataSerializer DUMMY_TILE_DATA_SERIALIZER = new DummyTileDataSerializer();

    public static ITileDataSerializer dummyDataSerializer() {
        return DUMMY_TILE_DATA_SERIALIZER;
    }

    private static final class DummyTileDataSerializer implements ITileDataSerializer {

        private DummyTileDataSerializer() {
            super();
        }

        @Override
        public CompoundTag serialize(ITileEntityData data, boolean persisted) {
            return new CompoundTag();
        }

        @Override
        public ITileEntityData deserialize(CompoundTag tagCompound, boolean persisted) {
            return TileSupport.dummyTileEntityData();
        }
    }

    // TODO: Register as Reference.TileDataSerializerReference.NBT_TILE_ENTITY_DATA_SERIALIZER_RL
    private static final ITileDataSerializer NBT_TILE_DATA_SERIALIZER = new NBTTileEntityDataSerializer();

    public static ITileDataSerializer nbtTileDataSerializer() {
        return NBT_TILE_DATA_SERIALIZER;
    }

    private static final class NBTTileEntityDataSerializer implements ITileDataSerializer {
        private NBTTileEntityDataSerializer() {
        }

        @Override
        public CompoundTag serialize(ITileEntityData data, boolean persisted) {
            Preconditions.checkArgument(data instanceof NBTTileEntityData);
            NBTTileEntityData nbtData = (NBTTileEntityData) data;
            CompoundTag res = new CompoundTag();
            res.put(NBTKeys.KEY_DATA, nbtData.getNBT());
            if (nbtData.getRequiredMaterials() != null)
                res.put(NBTKeys.KEY_MATERIALS, nbtData.getRequiredMaterials().serialize(persisted));
            return res;
        }

        @Override
        public ITileEntityData deserialize(CompoundTag tagCompound, boolean persisted) {
            CompoundTag data = tagCompound.getCompound(NBTKeys.KEY_DATA);
            MaterialList materialList = null;
            if (tagCompound.contains(NBTKeys.KEY_MATERIALS, NbtType.COMPOUND))
                materialList = MaterialList.deserialize(tagCompound.getCompound(NBTKeys.KEY_MATERIALS), persisted);
            return new NBTTileEntityData(data, materialList);
        }
    }

    // TODO: Register as Reference.UniqueObjectSerializerReference.SIMPLE_UNIQUE_ITEM_ID_RL
    private static final IUniqueObjectSerializer UNIQUE_ITEM_SERIALIZER = new UniqueItem.Serializer();

    public static IUniqueObjectSerializer uniqueItemSerializer() {
        return UNIQUE_ITEM_SERIALIZER;
    }

}
