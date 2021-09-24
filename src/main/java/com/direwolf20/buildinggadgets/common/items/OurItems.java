package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.config.Config;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public final class OurItems {

    public static void registerItems() {
        Registry.register(Registry.ITEM, BuildingGadgets.id("gadget_building"), BUILDING_GADGET_ITEM);
        Registry.register(Registry.ITEM, BuildingGadgets.id("gadget_exchanging"), EXCHANGING_GADGET_ITEM);
        Registry.register(Registry.ITEM, BuildingGadgets.id("gadget_copy_paste"), COPY_PASTE_GADGET_ITEM);
        Registry.register(Registry.ITEM, BuildingGadgets.id("gadget_destruction"), DESTRUCTION_GADGET_ITEM);
    }
    // Gadgets
    public static final Item BUILDING_GADGET_ITEM = new GadgetBuilding();
    public static final Item EXCHANGING_GADGET_ITEM = new GadgetExchanger();
    public static final Item COPY_PASTE_GADGET_ITEM = new GadgetCopyPaste();
    public static final Item DESTRUCTION_GADGET_ITEM = new GadgetDestruction();

    // Construction Paste Containers
    public static final Item PASTE_CONTAINER_T1_ITEM
            = ITEMS.register("construction_paste_container_t1", () -> new ConstructionPasteContainer(false, Config.PASTE_CONTAINERS.capacityT1::get));
    public static final Item PASTE_CONTAINER_T2_ITEM
            = ITEMS.register("construction_paste_container_t2", () -> new ConstructionPasteContainer(false, Config.PASTE_CONTAINERS.capacityT2::get));
    public static final Item PASTE_CONTAINER_T3_ITEM
            = ITEMS.register("construction_paste_container_t3", () -> new ConstructionPasteContainer(false, Config.PASTE_CONTAINERS.capacityT3::get));
    public static final Item PASTE_CONTAINER_CREATIVE_ITEM
            = ITEMS.register("construction_paste_container_creative", () -> new ConstructionPasteContainer(true));

    // Construction Paste
    public static final Item CONSTRUCTION_PASTE_ITEM = ITEMS.register("construction_paste", ConstructionPaste::new);
    public static final Item CONSTRUCTION_PASTE_DENSE_ITEM = ITEMS.register("construction_chunk_dense", () -> new Item(itemProperties()));

    // Template
    public static final Item TEMPLATE_ITEM = ITEMS.register("template", TemplateItem::new);

    // Item Blocks
    public static final Item CONSTRUCTION_ITEM
            = ITEMS.register("construction_block", () -> new BlockItem(OurBlocks.CONSTRUCTION_BLOCK, OurItems.itemProperties()));
    public static final Item CONSTRUCTION_DENSE_ITEM
            = ITEMS.register("construction_block_dense", () -> new BlockItem(OurBlocks.CONSTRUCTION_DENSE_BLOCK, OurItems.itemProperties()));
    public static final Item CONSTRUCTION_POWDER_ITEM
            = ITEMS.register("construction_block_powder", () -> new BlockItem(OurBlocks.CONSTRUCTION_POWDER_BLOCK, OurItems.itemProperties()));
    public static final Item TEMPLATE_MANGER_ITEM
            = ITEMS.register("template_manager", () -> new BlockItem(OurBlocks.TEMPLATE_MANGER_BLOCK, OurItems.itemProperties()));

    public static Item.Properties itemProperties() {
        return new Item.Properties().tab(BuildingGadgets.creativeTab);
    }

    public static Item.Properties nonStackableItemProperties() {
        return itemProperties().stacksTo(1);
    }
}
