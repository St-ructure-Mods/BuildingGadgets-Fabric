package com.direwolf20.buildinggadgets.common.enchants;

import com.direwolf20.buildinggadgets.common.items.OurItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

public class ExchangeEnchantmentCategory extends EnchantmentCategoryMixin{

    @Override
    public boolean canEnchant(Item item) {
        return item == OurItems.EXCHANGING_GADGET_ITEM;
    }
}

@Mixin(EnchantmentCategory.class)
abstract class EnchantmentCategoryMixin {
    @Shadow
    public abstract boolean canEnchant(Item item);
}
