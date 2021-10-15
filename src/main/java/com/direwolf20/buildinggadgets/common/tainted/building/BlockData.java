package com.direwolf20.buildinggadgets.common.tainted.building;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataSerializer;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileEntityData;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.registry.Registries;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

/**
 * Representation of the data one block can hold, in the form of an {@link BlockState} and an instance of {@link ITileEntityData}.
 * This calls offers serialisation facilities as well as delegating placement through to the {@link ITileEntityData}.
 * <p>
 * Notice that this class is immutable as long as the {@link ITileEntityData} instance is immutable.
 */
public record BlockData(BlockState state, ITileEntityData tileData) {
    public static final BlockData AIR = new BlockData(Blocks.AIR.defaultBlockState(), TileSupport.dummyTileEntityData());

    /**
     * Attempts to retrieve a BlockData from the given {@link CompoundTag}, if present.
     *
     * @param tag       The {@link CompoundTag} representing the serialized block data.
     * @param persisted Whether or not the {@link CompoundTag} was created using an persisted save.
     * @return A new instance of {@code BlockData} as represented by the {@link CompoundTag}, if it could be created or null otherwise.
     * @see #deserialize(CompoundTag, boolean)
     */
    @Nullable
    public static BlockData tryDeserialize(@Nullable CompoundTag tag, boolean persisted) {
        return tryDeserialize(tag, persisted ? null : i -> Registries.getTileDataSerializers().byId(i), persisted);
    }

    @Nullable
    public static BlockData tryDeserialize(@Nullable CompoundTag tag, @Nullable IntFunction<ITileDataSerializer> serializerProvider, boolean readDataPersisted) {
        if (tag == null || !(tag.contains(NBTKeys.KEY_STATE) && tag.contains(NBTKeys.KEY_SERIALIZER) && tag.contains(NBTKeys.KEY_DATA)))
            return null;
        BlockState state = NbtUtils.readBlockState(tag.getCompound(NBTKeys.KEY_STATE));
        ITileDataSerializer serializer;
        try {
            if (serializerProvider == null)
                serializer = Registries.getTileDataSerializers().get(new ResourceLocation(tag.getString(NBTKeys.KEY_SERIALIZER)));
            else
                serializer = serializerProvider.apply(tag.getInt(NBTKeys.KEY_SERIALIZER));
        } catch (Exception e) {
            BuildingGadgets.LOG.error("Failed to create deserializer!", e);
            return null;
        }
        if (serializer == null)
            return null;
        ITileEntityData data = serializer.deserialize(tag.getCompound(NBTKeys.KEY_DATA), readDataPersisted);
        return new BlockData(state, data);
    }

    /**
     * @param tag       The {@link CompoundTag} representing the serialized block data.
     * @param persisted Whether or not the {@link CompoundTag} was created using an persisted save.
     * @return A new instance of {@code BlockData} as represented by the {@link CompoundTag}.
     * @throws IllegalArgumentException if the given tag does not represent a valid {@code BlockData}.
     * @throws NullPointerException     if the tag was null.
     */
    public static BlockData deserialize(CompoundTag tag, boolean persisted) {
        return deserialize(tag, persisted ? null : i -> Registries.getTileDataSerializers().byId(i), persisted);
    }

    public static BlockData deserialize(CompoundTag tag, @Nullable IntFunction<ITileDataSerializer> serializerProvider, boolean readDataPersisted) {
        Preconditions.checkNotNull(tag, "Cannot deserialize from a null tag compound");
        Preconditions.checkArgument(tag.contains(NBTKeys.KEY_STATE) && tag.contains(NBTKeys.KEY_SERIALIZER) && tag.contains(NBTKeys.KEY_DATA),
                "Given NBTTagCompound does not contain a valid BlockData instance. Missing NBT-Keys in Tag {}!", tag.toString());
        BlockState state = NbtUtils.readBlockState(tag.getCompound(NBTKeys.KEY_STATE));
        ITileDataSerializer serializer;
        try {
            if (serializerProvider == null)
                serializer = Registries.getTileDataSerializers().get(new ResourceLocation(tag.getString(NBTKeys.KEY_SERIALIZER)));
            else
                serializer = serializerProvider.apply(tag.getInt(NBTKeys.KEY_SERIALIZER));
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not retrieve serializer with persisted=" + readDataPersisted + "!", e);
        }
        Preconditions.checkArgument(serializer != null,
                "Failed to retrieve serializer for tag {} and persisted={}", tag.toString(), readDataPersisted);
        ITileEntityData data = serializer.deserialize(tag.getCompound(NBTKeys.KEY_DATA), readDataPersisted);
        return new BlockData(state, data);
    }

    /**
     * @return The {@link BlockState} contained by this {@code BlockData}
     */
    public BlockState getState() {
        return state();
    }

    /**
     * @return The {@link ITileEntityData} contained by this {@code BlockState}.
     */
    public ITileEntityData getTileData() {
        return tileData();
    }

    /**
     * @param context The {@link BuildContext} in which to perform the placement.
     * @param pos     The {@link BlockPos} at which to perform the placement.
     * @return whether or not the {@link ITileEntityData} reported that placement was performed.
     */
    public boolean placeIn(BuildContext context, BlockPos pos) {
        return tileData.placeIn(context, state, pos);
    }

    /**
     * Serializes this {@code BlockData} to NBT. If persisted is false, registry id's will be used instead of registry-names, for serialisation.
     *
     * @param persisted Whether or not this should be written as a persisted save.
     * @return The serialized form of this {@code BlockData}.
     */
    public CompoundTag serialize(boolean persisted) {
        return serialize(persisted ? null : ser -> Registries.getTileDataSerializers().getId(ser), persisted);
    }

    public CompoundTag serialize(@Nullable ToIntFunction<ITileDataSerializer> idGetter, boolean writeDataPersisted) {
        CompoundTag tag = new CompoundTag();
        tag.put(NBTKeys.KEY_STATE, NbtUtils.writeBlockState(state));
        if (idGetter == null)
            tag.putString(NBTKeys.KEY_SERIALIZER, Registries.getTileDataSerializers().getKey(tileData.getSerializer()).toString());
        else
            tag.putInt(NBTKeys.KEY_SERIALIZER, idGetter.applyAsInt(tileData.getSerializer()));
        tag.put(NBTKeys.KEY_DATA, tileData.getSerializer().serialize(tileData, writeDataPersisted));
        return tag;
    }

    public BlockData mirror(Mirror mirror) {
        return new BlockData(getState().mirror(mirror), getTileData());
    }

    public BlockData rotate(Rotation rotation) {
        return new BlockData(getState().rotate(rotation), getTileData());
    }

    public MaterialList getRequiredItems(BuildContext context, @Nullable HitResult target, @Nullable BlockPos pos) {
        return getTileData().getRequiredItems(context, getState(), target, pos);
    }
}
