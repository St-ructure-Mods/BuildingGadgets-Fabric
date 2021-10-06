package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public final class OurItems {

    public static void registerItems() {
        itemRegister("gadget_building", BUILDING_GADGET_ITEM);
        itemRegister("gadget_exchanging", EXCHANGING_GADGET_ITEM);
        itemRegister("gadget_copy_paste", COPY_PASTE_GADGET_ITEM);
        itemRegister("gadget_destruction", DESTRUCTION_GADGET_ITEM);

        itemRegister("template", TEMPLATE_ITEM);

        itemRegister("template_manager", TEMPLATE_MANGER_ITEM);

    }

    private static void itemRegister(String path, Item item) {
        Registry.register(Registry.ITEM, BuildingGadgets.id(path), item);
    }
    // Gadgets
    public static final Item BUILDING_GADGET_ITEM = new GadgetBuilding();
    public static final Item EXCHANGING_GADGET_ITEM = new GadgetExchanger();
    public static final Item COPY_PASTE_GADGET_ITEM = new GadgetCopyPaste();
    public static final Item DESTRUCTION_GADGET_ITEM = new GadgetDestruction();

    // Template
    public static final Item TEMPLATE_ITEM = new TemplateItem();

    // Item Blocks
    public static final Item TEMPLATE_MANGER_ITEM = new BlockItem(OurBlocks.TEMPLATE_MANGER_BLOCK, OurItems.itemProperties());

    public static Item.Properties itemProperties() {
        return new Item.Properties().tab(BuildingGadgets.creativeTab);
    }

    public static Item.Properties nonStackableItemProperties() {
        return itemProperties().stacksTo(1);
    }
}
