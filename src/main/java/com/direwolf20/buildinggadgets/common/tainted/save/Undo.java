package com.direwolf20.buildinggadgets.common.tainted.save;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.Region;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataSerializer;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileEntityData;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.NBTTileEntityData;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.tainted.registry.Registries;
import com.direwolf20.buildinggadgets.common.util.compression.DataCompressor;
import com.direwolf20.buildinggadgets.common.util.compression.DataDecompressor;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Multisets;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

public record Undo(ResourceKey<Level> dim, Map<BlockPos, BlockInfo> dataMap, Region boundingBox) {

    public static Undo deserialize(CompoundTag nbt) {
        Preconditions.checkArgument(nbt.contains(NBTKeys.WORLD_SAVE_DIM, NbtType.STRING)
                                    && nbt.contains(NBTKeys.WORLD_SAVE_UNDO_BLOCK_LIST, NbtType.LIST)
                                    && nbt.contains(NBTKeys.WORLD_SAVE_UNDO_DATA_LIST, NbtType.LIST)
                                    && nbt.contains(NBTKeys.WORLD_SAVE_UNDO_DATA_SERIALIZER_LIST, NbtType.LIST));
        DataDecompressor<ITileDataSerializer> serializerReverseObjectIncrementer = new DataDecompressor<>(
                (ListTag) nbt.get(NBTKeys.WORLD_SAVE_UNDO_DATA_SERIALIZER_LIST),
                inbt -> {
                    String s = inbt.getAsString();
                    ITileDataSerializer serializer = Registries.getTileDataSerializers().get(new ResourceLocation(s));
                    if (serializer == null) {
                        BuildingGadgets.LOG.warn("Found unknown serializer {}. Replacing with dummy!", s);
                        serializer = TileSupport.dummyTileEntityData().getSerializer();
                    }
                    return serializer;
                },
                value -> {
                    BuildingGadgets.LOG.warn("Attempted to query unknown serializer {}. Replacing with dummy!", value);
                    return TileSupport.dummyTileEntityData().getSerializer();
                });
        DataDecompressor<BlockData> dataReverseObjectIncrementer = new DataDecompressor<>(
                (ListTag) nbt.get(NBTKeys.WORLD_SAVE_UNDO_DATA_LIST),
                inbt -> BlockData.deserialize((CompoundTag) inbt, serializerReverseObjectIncrementer, true),
                value -> BlockData.AIR);
        DataDecompressor<Multiset<ItemVariant>> itemSetReverseObjectIncrementer = new DataDecompressor<>(
                (ListTag) nbt.get(NBTKeys.WORLD_SAVE_UNDO_ITEMS_LIST),
                inbt -> NBTHelper.deserializeMultisetEntries((ListTag) inbt, HashMultiset.create(), Undo::readEntry),
                value -> HashMultiset.create());
        Map<BlockPos, BlockInfo> map = NBTHelper.deserializeMap(
                (ListTag) nbt.get(NBTKeys.WORLD_SAVE_UNDO_BLOCK_LIST), new HashMap<>(),
                inbt -> NbtUtils.readBlockPos((CompoundTag) inbt),
                inbt -> BlockInfo.deserialize((CompoundTag) inbt, dataReverseObjectIncrementer, itemSetReverseObjectIncrementer));

        ResourceKey<Level> dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(nbt.getString(NBTKeys.WORLD_SAVE_DIM)));
        Region bounds = Region.deserializeFrom(nbt.getCompound(NBTKeys.WORLD_SAVE_UNDO_BOUNDS));
        return new Undo(dim, map, bounds);
    }

    private static Tuple<ItemVariant, Integer> readEntry(Tag inbt) {
        CompoundTag nbt = (CompoundTag) inbt;
        int count = nbt.getInt(NBTKeys.UNIQUE_ITEM_COUNT);
        ItemVariant item = ItemVariant.fromNbt(nbt.getCompound(NBTKeys.UNIQUE_ITEM_ITEM));
        return new Tuple<>(item, count);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Region getBoundingBox() {
        return boundingBox;
    }

    public Map<BlockPos, BlockInfo> getUndoData() {
        return Collections.unmodifiableMap(dataMap);
    }

    public CompoundTag serialize() {
        DataCompressor<BlockData> dataObjectIncrementer = new DataCompressor<>();
        DataCompressor<Multiset<ItemVariant>> itemObjectIncrementer = new DataCompressor<>();
        DataCompressor<ITileDataSerializer> serializerObjectIncrementer = new DataCompressor<>();
        CompoundTag res = new CompoundTag();

        ListTag infoList = NBTHelper.serializeMap(dataMap, NbtUtils::writeBlockPos, i -> i.serialize(dataObjectIncrementer, itemObjectIncrementer));
        ListTag dataList = dataObjectIncrementer.write(d -> d.serialize(serializerObjectIncrementer, true));
        ListTag itemSetList = itemObjectIncrementer.write(ms -> NBTHelper.writeIterable(ms.entrySet(), this::writeEntry));
        ListTag dataSerializerList = serializerObjectIncrementer.write(ts -> StringTag.valueOf(Registries.getTileDataSerializers().getKey(ts).toString()));

        res.putString(NBTKeys.WORLD_SAVE_DIM, dim.location().toString());
        res.put(NBTKeys.WORLD_SAVE_UNDO_BLOCK_LIST, infoList);
        res.put(NBTKeys.WORLD_SAVE_UNDO_DATA_LIST, dataList);
        res.put(NBTKeys.WORLD_SAVE_UNDO_DATA_SERIALIZER_LIST, dataSerializerList);
        res.put(NBTKeys.WORLD_SAVE_UNDO_ITEMS_LIST, itemSetList);
        res.put(NBTKeys.WORLD_SAVE_UNDO_BOUNDS, boundingBox.serialize());

        return res;
    }

    private CompoundTag writeEntry(Entry<ItemVariant> entry) {
        CompoundTag res = new CompoundTag();
        res.put(NBTKeys.UNIQUE_ITEM_ITEM, entry.getElement().toNbt());
        res.putInt(NBTKeys.UNIQUE_ITEM_COUNT, entry.getCount());
        return res;
    }

    public record BlockInfo(BlockData recordedData, BlockData placedData, Multiset<ItemVariant> usedItems,
                            Multiset<ItemVariant> producedItems) {
        private static BlockInfo deserialize(CompoundTag nbt, IntFunction<BlockData> dataSupplier, IntFunction<Multiset<ItemVariant>> itemSetSupplier) {
            BlockData data = dataSupplier.apply(nbt.getInt(NBTKeys.WORLD_SAVE_UNDO_RECORDED_DATA));
            BlockData placedData = dataSupplier.apply(nbt.getInt(NBTKeys.WORLD_SAVE_UNDO_PLACED_DATA));
            Multiset<ItemVariant> usedItems = itemSetSupplier.apply(nbt.getInt(NBTKeys.WORLD_SAVE_UNDO_ITEMS_USED));
            Multiset<ItemVariant> producedItems = itemSetSupplier.apply(nbt.getInt(NBTKeys.WORLD_SAVE_UNDO_ITEMS_PRODUCED));
            return new BlockInfo(data, placedData, usedItems, producedItems);
        }

        private CompoundTag serialize(ToIntFunction<BlockData> dataIdSupplier, ToIntFunction<Multiset<ItemVariant>> itemIdSupplier) {
            CompoundTag res = new CompoundTag();
            res.putInt(NBTKeys.WORLD_SAVE_UNDO_RECORDED_DATA, dataIdSupplier.applyAsInt(recordedData));
            res.putInt(NBTKeys.WORLD_SAVE_UNDO_PLACED_DATA, dataIdSupplier.applyAsInt(placedData));
            res.putInt(NBTKeys.WORLD_SAVE_UNDO_ITEMS_USED, itemIdSupplier.applyAsInt(usedItems));
            res.putInt(NBTKeys.WORLD_SAVE_UNDO_ITEMS_PRODUCED, itemIdSupplier.applyAsInt(producedItems));
            return res;
        }

        public BlockData getRecordedData() {
            return recordedData;
        }

        public BlockData getPlacedData() {
            return placedData;
        }

        public Multiset<ItemVariant> getUsedItems() {
            return Multisets.unmodifiableMultiset(usedItems);
        }

        public Multiset<ItemVariant> getProducedItems() {
            return Multisets.unmodifiableMultiset(producedItems);
        }
    }

    public static final class Builder {
        private final ImmutableMap.Builder<BlockPos, BlockInfo> mapBuilder;
        private Region.Builder regionBuilder;

        private Builder() {
            mapBuilder = ImmutableMap.builder();
            regionBuilder = null;
        }

        public Builder record(BlockGetter reader, BlockPos pos, BlockData placeData, Multiset<ItemVariant> requiredItems, Multiset<ItemVariant> producedItems) {
            BlockState state = reader.getBlockState(pos);
            BlockEntity be = reader.getBlockEntity(pos);
            ITileEntityData data = be != null ? NBTTileEntityData.ofTile(be) : TileSupport.dummyTileEntityData();
            return record(pos, new BlockData(state, data), placeData, requiredItems, producedItems);
        }

        private Builder record(BlockPos pos, BlockData recordedData, BlockData placedData, Multiset<ItemVariant> requiredItems, Multiset<ItemVariant> producedItems) {
            mapBuilder.put(pos, new BlockInfo(recordedData, placedData, requiredItems, producedItems));
            if (regionBuilder == null)
                regionBuilder = Region.enclosingBuilder();
            regionBuilder.enclose(pos);
            return this;
        }

        public Undo build(Level dim) {
            return new Undo(dim.dimension(), mapBuilder.build(), regionBuilder != null ? regionBuilder.build() : Region.singleZero());
        }
    }
}
