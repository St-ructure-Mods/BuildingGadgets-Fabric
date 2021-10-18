package com.direwolf20.buildinggadgets.common.component;

import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;
import java.util.function.Supplier;

public final class ItemTemplateKey extends ItemComponent implements ITemplateKey {

    public ItemTemplateKey(ItemStack stack) {
        super(stack);
    }

    @Override
    public UUID getOrComputeId(Supplier<UUID> freeIdAllocator) {
        UUID id = getUuid(NBTKeys.TEMPLATE_KEY_ID);

        if (id == null) {
            id = freeIdAllocator.get();
            putUuid(NBTKeys.TEMPLATE_KEY_ID, id);
            BGComponent.TEMPLATE_KEY_COMPONENT.sync(stack);
        }

        return id;
    }
}
