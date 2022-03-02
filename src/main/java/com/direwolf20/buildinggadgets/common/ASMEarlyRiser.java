package com.direwolf20.buildinggadgets.common;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class ASMEarlyRiser implements Runnable{
    @Override
    public void run() {
        MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
        String enchantmentTarget = resolver.mapClassName("intermediary", "net.minecraft.class_1886");

        ClassTinkerers.enumBuilder(enchantmentTarget).addEnumSubclass("EXCHANGE", "com.direwolf20.buildinggadgets.common.enchants.ExchangeEnchantmentCategory").build();
    }
}
