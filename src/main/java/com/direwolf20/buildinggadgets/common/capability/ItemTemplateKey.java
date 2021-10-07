package com.direwolf20.buildinggadgets.common.capability;

import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;
import java.util.function.Supplier;

public final class ItemTemplateKey extends ItemComponent implements ITemplateKey {
    private final ItemStack stack;

    public ItemTemplateKey(ItemStack stack) {
        super(stack);
        this.stack = stack;
    }

    @Override
    public UUID getTemplateId(Supplier<UUID> freeIdAllocator) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (!nbt.hasUUID(NBTKeys.TEMPLATE_KEY_ID)) {
            UUID newID = freeIdAllocator.get();
            nbt.putUUID(NBTKeys.TEMPLATE_KEY_ID, newID);
            return newID;
        }
        return nbt.getUUID(NBTKeys.TEMPLATE_KEY_ID);
    }
}
