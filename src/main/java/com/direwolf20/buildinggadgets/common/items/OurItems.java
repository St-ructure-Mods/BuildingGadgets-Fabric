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

        itemRegister("construction_paste_container_t1", PASTE_CONTAINER_T1_ITEM);
        itemRegister("construction_paste_container_t2", PASTE_CONTAINER_T2_ITEM);
        itemRegister("construction_paste_container_t3", PASTE_CONTAINER_T3_ITEM);
        itemRegister("construction_paste_container_creative", PASTE_CONTAINER_CREATIVE_ITEM);

        itemRegister("construction_paste", CONSTRUCTION_PASTE_ITEM);
        itemRegister("construction_chunk_dense", CONSTRUCTION_PASTE_DENSE_ITEM);

        itemRegister("template", TEMPLATE_ITEM);


        itemRegister("construction_block", CONSTRUCTION_ITEM);
        itemRegister("construction_block_dense", CONSTRUCTION_DENSE_ITEM);
        itemRegister("construction_block_powder", CONSTRUCTION_POWDER_ITEM);
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

    // Construction Paste Containers
    public static final Item PASTE_CONTAINER_T1_ITEM = new ConstructionPasteContainer(false, BuildingGadgets.config.PASTE_CONTAINERS.capacityT1);
    public static final Item PASTE_CONTAINER_T2_ITEM = new ConstructionPasteContainer(false, BuildingGadgets.config.PASTE_CONTAINERS.capacityT2);
    public static final Item PASTE_CONTAINER_T3_ITEM = new ConstructionPasteContainer(false, BuildingGadgets.config.PASTE_CONTAINERS.capacityT3);
    public static final Item PASTE_CONTAINER_CREATIVE_ITEM = new ConstructionPasteContainer(true);

    // Construction Paste
    public static final Item CONSTRUCTION_PASTE_ITEM = new ConstructionPaste();
    public static final Item CONSTRUCTION_PASTE_DENSE_ITEM = new Item(itemProperties());

    // Template
    public static final Item TEMPLATE_ITEM = new TemplateItem();

    // Item Blocks
    public static final Item CONSTRUCTION_ITEM = new BlockItem(OurBlocks.CONSTRUCTION_BLOCK, OurItems.itemProperties());
    public static final Item CONSTRUCTION_DENSE_ITEM = new BlockItem(OurBlocks.CONSTRUCTION_DENSE_BLOCK, OurItems.itemProperties());
    public static final Item CONSTRUCTION_POWDER_ITEM = new BlockItem(OurBlocks.CONSTRUCTION_POWDER_BLOCK, OurItems.itemProperties());
    public static final Item TEMPLATE_MANGER_ITEM = new BlockItem(OurBlocks.TEMPLATE_MANGER_BLOCK, OurItems.itemProperties());

    public static Item.Properties itemProperties() {
        return new Item.Properties().tab(BuildingGadgets.creativeTab);
    }

    public static Item.Properties nonStackableItemProperties() {
        return itemProperties().stacksTo(1);
    }
}
