package com.direwolf20.buildinggadgets.common.tainted.template;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * A very simple {@link ITemplateKey} which allows to query an {@link ITemplateProvider} for a specific Template, without
 * having the CapabilityProvider at hand. (For example useful for packets)
 */
public final class TemplateKey implements ITemplateKey {
    @Nullable
    private UUID id;

    public TemplateKey(@Nullable UUID id) {
        this.id = id;
    }

    @Override
    public UUID getOrComputeId(Supplier<UUID> freeIdAllocator) {
        if (id == null) {
            setId(freeIdAllocator.get());
        }

        return id;
    }

    @Nullable
    public UUID getId() {
        return id;
    }

    public void setId(@Nullable UUID id) {
        this.id = id;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        id = tag.getUUID("id");
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putUUID("id", id);
    }
}
