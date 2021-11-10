package com.direwolf20.buildinggadgets.client.screen.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class TemplateData implements TooltipComponent {

    public ItemStack stack;

    public TemplateData(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public TemplateTooltip clientTooltip() {
        return new TemplateTooltip(this);
    }
}
