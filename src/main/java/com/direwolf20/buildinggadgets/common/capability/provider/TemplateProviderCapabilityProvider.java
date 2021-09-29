package com.direwolf20.buildinggadgets.common.capability.provider;

import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TemplateProviderCapabilityProvider implements ICapabilityProvider {
    private final LazyOptional<ITemplateProvider> opt;

    public TemplateProviderCapabilityProvider(ITemplateProvider provider) {
        this.opt = LazyOptional.of(() -> provider);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY)
            return opt.cast();
        return LazyOptional.empty();
    }
}
