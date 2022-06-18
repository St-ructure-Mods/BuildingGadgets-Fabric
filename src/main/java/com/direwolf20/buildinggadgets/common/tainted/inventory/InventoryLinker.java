package com.direwolf20.buildinggadgets.common.tainted.inventory;

import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class InventoryLinker {
    /**
     * Perform the link to the inventory
     */
    public static Result linkInventory(Level world, ItemStack stack, BlockHitResult trace) {
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(world, trace.getBlockPos(), trace.getDirection());

        if (storage == null) {
            return Result.fail(MessageTranslation.INVALID_BOUND_TILE);
        }

        // remove if the existing linked inventory is the same block we're setting now.
        boolean removed = getLinkedInventory(world, stack)
                .map(e -> removeIfSame(stack, trace.getBlockPos()))
                .orElse(false);

        if (removed) {
            return Result.removed();
        }

        // Set the relevant data
        CompoundTag compound = stack.getOrCreateTag();
        compound.putString(NBTKeys.REMOTE_INVENTORY_DIM, world.dimension().location().toString());
        compound.put(NBTKeys.REMOTE_INVENTORY_POS, NbtUtils.writeBlockPos(trace.getBlockPos()));
        compound.putString(NBTKeys.REMOTE_INVENTORY_FACE, trace.getDirection().name());
        return Result.success();
    }

    /**
     * Directly fetch the linked inventory if the tile exists (removes if not) and if the tile holds an inventory
     */
    public static Optional<Storage<ItemVariant>> getLinkedInventory(Level world, InventoryLink link, @Nullable ItemStack stack) {
        if (!world.dimension().equals(link.level())) {
            return Optional.empty();
        }

        Storage<ItemVariant> storage = ItemStorage.SIDED.find(world, link.blockPos, link.face);

        if (storage == null) {
            // Unlink if the storage no longer exists
            if (stack != null) {
                removeDataFromStack(stack);
            }

            return Optional.empty();
        }

        return Optional.ofNullable(storage);
    }

    public static Optional<Storage<ItemVariant>> getLinkedInventory(Level world, ItemStack stack) {
        InventoryLink dataFromStack = getDataFromStack(stack);
        if (dataFromStack == null) {
            return Optional.empty();
        }

        return getLinkedInventory(world, dataFromStack, stack);
    }

    /**
     * Remove the link from the ItemStack if the pos is the same as the target pos. This creates a toggle effect.
     *
     * @implNote Ideally this would not have to get the same data twice but for now, this works fine.
     */
    private static boolean removeIfSame(ItemStack stack, BlockPos pos) {
        // This isn't ideal that we have to do this twice
        InventoryLink dataFromStack = getDataFromStack(stack);
        if (dataFromStack == null) {
            return false;
        }

        if (dataFromStack.blockPos().equals(pos)) {
            removeDataFromStack(stack);
            return true;
        }

        return false;
    }

    /**
     * Removes the keys from the stack to allow for lazy contains
     */
    public static void removeDataFromStack(ItemStack stack) {
        CompoundTag compound = stack.getOrCreateTag();
        compound.remove(NBTKeys.REMOTE_INVENTORY_POS);
        compound.remove(NBTKeys.REMOTE_INVENTORY_DIM);
        compound.remove(NBTKeys.REMOTE_INVENTORY_FACE);
    }

    /**
     * Retrieves the link data from the ItemStack
     */
    @Nullable
    public static InventoryLink getDataFromStack(ItemStack stack) {
        CompoundTag compound = stack.getOrCreateTag();
        if (!(compound.contains(NBTKeys.REMOTE_INVENTORY_POS) && compound.contains(NBTKeys.REMOTE_INVENTORY_DIM) && compound.contains(NBTKeys.REMOTE_INVENTORY_FACE))) {
            return null;
        }

        ResourceKey<Level> dimKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(compound.getString(NBTKeys.REMOTE_INVENTORY_DIM)));
        Direction face;

        try {
            face = Direction.valueOf(compound.getString(NBTKeys.REMOTE_INVENTORY_FACE));
        } catch (Throwable ignored) {
            // smh why does valueOf die but not return null
            return null;
        }

        return new InventoryLink(dimKey, NbtUtils.readBlockPos(compound.getCompound(NBTKeys.REMOTE_INVENTORY_POS)), face);
    }

    /**
     * Handles if the Link was successful and a message to go with it.
     */
    public record Result(MessageTranslation i18n, boolean successful) {

        public static Result fail(MessageTranslation i18n) {
            return new Result(i18n, false);
        }

        public static Result success() {
            return new Result(MessageTranslation.BOUND_TO_TILE, true);
        }

        public static Result removed() {
            return new Result(MessageTranslation.UNBOUND_TO_TILE, true);
        }
    }

    public record InventoryLink(ResourceKey<Level> level, BlockPos blockPos, Direction face) {
    }
}
