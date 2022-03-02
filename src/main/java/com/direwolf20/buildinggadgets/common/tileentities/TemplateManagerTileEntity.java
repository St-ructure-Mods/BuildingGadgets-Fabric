package com.direwolf20.buildinggadgets.common.tileentities;

import com.direwolf20.buildinggadgets.common.component.BGComponent;
import com.direwolf20.buildinggadgets.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.inventory.ImplContainer;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.ItemReference;
import com.google.common.base.Preconditions;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TemplateManagerTileEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplContainer {
    public static final TagKey<Item> TEMPLATE_CONVERTIBLES = TagKey.create(Registry.ITEM_REGISTRY, ItemReference.TAG_TEMPLATE_CONVERTIBLE);

    public static final int SIZE = 2;

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public TemplateManagerTileEntity(BlockPos pos, BlockState state) {
        super(OurTileEntities.TEMPLATE_MANAGER_TILE_ENTITY, pos, state);
    }

    @Override
    @NotNull
    public Component getDisplayName() {
        return new TextComponent("Template Manager GUI");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player playerEntity) {
        Preconditions.checkArgument(getLevel() != null);
        return new TemplateManagerContainer(windowId, playerInventory, this);
    }

    public boolean isTemplateStack(ItemStack stack) {
        return BGComponent.TEMPLATE_KEY_COMPONENT.getNullable(stack) != null;
    }

    @Override
    public void load(CompoundTag compound) {
        ContainerHelper.loadAllItems(compound, inventory);
        super.load(compound);
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        ContainerHelper.saveAllItems(compound, inventory);
        super.saveAdditional(compound);
    }

    public boolean canInteractWith(Player playerIn) {
        // If we are too far away (>4 blocks) from this tile entity you cannot use it
        return !isRemoved() && playerIn.distanceToSqr(Vec3.atLowerCornerOf(worldPosition).add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level instanceof ServerLevel) {
            ((ServerChunkCache) level.getChunkSource()).blockChanged(getBlockPos());
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeBlockPos(getBlockPos());
    }
}
