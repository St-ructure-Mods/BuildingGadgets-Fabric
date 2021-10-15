package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public final class OurItems {

    // Gadgets
    public static final Item BUILDING_GADGET_ITEM = new GadgetBuilding(itemProperties().stacksTo(1));
    public static final Item EXCHANGING_GADGET_ITEM = new GadgetExchanger(itemProperties().stacksTo(1));
    public static final Item COPY_PASTE_GADGET_ITEM = new GadgetCopyPaste(itemProperties().stacksTo(1));
    public static final Item DESTRUCTION_GADGET_ITEM = new GadgetDestruction(itemProperties().stacksTo(1));

    // Template
    public static final Item TEMPLATE_ITEM = new TemplateItem(itemProperties().stacksTo(1));

    // Item Blocks
    public static final Item TEMPLATE_MANGER_ITEM = new BlockItem(OurBlocks.TEMPLATE_MANGER_BLOCK, OurItems.itemProperties());

    private static Item.Properties itemProperties() {
        return new Item.Properties().tab(BuildingGadgets.CREATIVE_TAB);
    }

    public static void registerItems() {
        Registry.register(Registry.ITEM, BuildingGadgets.id("gadget_building"), BUILDING_GADGET_ITEM);
        Registry.register(Registry.ITEM, BuildingGadgets.id("gadget_exchanging"), EXCHANGING_GADGET_ITEM);
        // Registry.register(Registry.ITEM, BuildingGadgets.id("gadget_copy_paste"), COPY_PASTE_GADGET_ITEM);
        Registry.register(Registry.ITEM, BuildingGadgets.id("gadget_destruction"), DESTRUCTION_GADGET_ITEM);
        // Registry.register(Registry.ITEM, BuildingGadgets.id("template"), TEMPLATE_ITEM);
        // Registry.register(Registry.ITEM, BuildingGadgets.id("template_manager"), TEMPLATE_MANGER_ITEM);
    }
}
