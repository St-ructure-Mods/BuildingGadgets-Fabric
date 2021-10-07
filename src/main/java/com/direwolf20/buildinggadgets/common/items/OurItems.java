package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public final class OurItems {

    private static Item itemRegister(String path, Item item) {
        return Registry.register(Registry.ITEM, BuildingGadgets.id(path), item);
    }

    // Gadgets
    public static final Item BUILDING_GADGET_ITEM = itemRegister("gadget_building", new GadgetBuilding());
    public static final Item EXCHANGING_GADGET_ITEM = itemRegister("gadget_exchanging", new GadgetExchanger());
    public static final Item COPY_PASTE_GADGET_ITEM = itemRegister("gadget_copy_paste", new GadgetCopyPaste());
    public static final Item DESTRUCTION_GADGET_ITEM = itemRegister("gadget_destruction", new GadgetDestruction());

    // Template
    public static final Item TEMPLATE_ITEM = itemRegister("template", new TemplateItem());

    // Item Blocks
    public static final Item TEMPLATE_MANGER_ITEM = itemRegister("template_manager", new BlockItem(OurBlocks.TEMPLATE_MANGER_BLOCK, OurItems.itemProperties()));


    public static Item.Properties itemProperties() {
        return new Item.Properties().tab(BuildingGadgets.creativeTab);
    }

    public static Item.Properties nonStackableItemProperties() {
        return itemProperties().stacksTo(1);
    }
}
