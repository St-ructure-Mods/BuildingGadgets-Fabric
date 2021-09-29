package com.direwolf20.buildinggadgets.common.capability;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PasteContainerCapabilityProvider implements ICapabilityProvider {
    private final LazyOptional<PasteContainerItemHandler> itemHandler;

    public PasteContainerCapabilityProvider(ItemStack container) {
        this.itemHandler = LazyOptional.of(() -> new PasteContainerItemHandler(container));
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (CapabilityItemHandler.ITEM_HANDLER_CAPABILITY == cap)
            return itemHandler.cast();
        return LazyOptional.empty();
    }
}
