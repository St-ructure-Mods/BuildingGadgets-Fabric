package com.direwolf20.buildinggadgets.common.enchants;

import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class GadgetSilkTouch extends Enchantment {

    public static Enchantment GADGET_SILKTOUCH = new GadgetSilkTouch(Rarity.VERY_RARE, EquipmentSlot.MAINHAND);

    protected GadgetSilkTouch(Rarity rarity, EquipmentSlot... equipmentSlots) {
        super(rarity, ClassTinkerers.getEnum(EnchantmentCategory.class, "EXCHANGE"), equipmentSlots);
    }

    @Override
    public int getMinCost(int level) {
        return 15;
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
