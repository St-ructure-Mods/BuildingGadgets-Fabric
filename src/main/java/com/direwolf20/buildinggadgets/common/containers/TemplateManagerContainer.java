package com.direwolf20.buildinggadgets.common.containers;

import com.direwolf20.buildinggadgets.common.tileentities.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

public class TemplateManagerContainer extends BaseContainer {
    public static final String TEXTURE_LOC_SLOT_TOOL = Reference.MODID + ":gui/slot_copy_paste_gadget";
    public static final String TEXTURE_LOC_SLOT_TEMPLATE = Reference.MODID + ":gui/slot_template";

    private final TemplateManagerTileEntity be;

    public TemplateManagerContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(OurContainers.TEMPLATE_MANAGER_CONTAINER_TYPE, windowId);
        BlockPos pos = extraData.readBlockPos();

        this.be = (TemplateManagerTileEntity) playerInventory.player.level.getBlockEntity(pos);
        addOwnSlots();
        addPlayerSlots(playerInventory, -12, 70);
    }

    public TemplateManagerContainer(int windowId, Inventory playerInventory, TemplateManagerTileEntity tileEntity) {
        this(windowId, playerInventory, PacketByteBufs.create().writeBlockPos(tileEntity.getBlockPos()));
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return getTe().canInteractWith(playerIn);
    }

    private void addOwnSlots() {
        int x = 132;
        addSlot(new SlotTemplateManager(be, 0, x, 18, TEXTURE_LOC_SLOT_TOOL));
        addSlot(new SlotTemplateManager(be, 1, x, 63, TEXTURE_LOC_SLOT_TEMPLATE));
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return (slot.index == 0 && be.isTemplateStack(itemStack)) ||
                (slot.index == 1 && (be.isTemplateStack(itemStack) || TemplateManagerTileEntity.TEMPLATE_CONVERTIBLES.contains(itemStack.getItem())));
    }

    @Override
    @NotNull
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack currentStack = slot.getItem();
            itemstack = currentStack.copy();

            if (index < TemplateManagerTileEntity.SIZE) {
                if (! this.moveItemStackTo(currentStack, TemplateManagerTileEntity.SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (! this.moveItemStackTo(currentStack, 0, TemplateManagerTileEntity.SIZE, false)) {
                return ItemStack.EMPTY;
            }

            if (currentStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }



    public TemplateManagerTileEntity getTe() {
        return be;
    }

    public static class SlotTemplateManager extends Slot {
        private final String backgroundLoc;

        public SlotTemplateManager(Container container, int index, int xPosition, int yPosition, String backgroundLoc) {
            super(container, index, xPosition, yPosition);
            this.backgroundLoc = backgroundLoc;
        }

        // @Override
        // public Slot setBackground(ResourceLocation atlas, ResourceLocation sprite) {
        //    return super.setBackground(atlas, new ResourceLocation(Reference.MODID, this.backgroundLoc));
        // }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
