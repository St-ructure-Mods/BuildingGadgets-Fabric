package com.direwolf20.buildinggadgets.common.util.ref;

import net.minecraft.resources.ResourceLocation;

public final class Reference {

    public static final String MODID = "buildinggadgets";

    public static final class ItemReference {
        public static final ResourceLocation TAG_TEMPLATE_CONVERTIBLE = new ResourceLocation(MODID, "template_convertible");
    }

    public static final class TileDataSerializerReference {
        public static final ResourceLocation REGISTRY_ID_TILE_DATA_SERIALIZER = new ResourceLocation(MODID, "tile_data/serializer");
        public static final ResourceLocation DUMMY_SERIALIZER_RL = new ResourceLocation(MODID, "dummy_serializer");
        public static final ResourceLocation NBT_TILE_ENTITY_DATA_SERIALIZER_RL = new ResourceLocation(MODID, "nbt_tile_data_serializer");
    }

    public static final class TagReference {
        public static final ResourceLocation BLACKLIST_COPY_PASTE = new ResourceLocation(MODID, "blacklist/copy_paste");
        public static final ResourceLocation BLACKLIST_BUILDING = new ResourceLocation(MODID, "blacklist/building");
        public static final ResourceLocation BLACKLIST_EXCHANGING = new ResourceLocation(MODID, "blacklist/exchanging");
        public static final ResourceLocation BLACKLIST_DESTRUCTION = new ResourceLocation(MODID, "blacklist/destruction");
        public static final ResourceLocation WHITELIST_COPY_PASTE = new ResourceLocation(MODID, "whitelist/copy_paste");
        public static final ResourceLocation WHITELIST_BUILDING = new ResourceLocation(MODID, "whitelist/building");
        public static final ResourceLocation WHITELIST_EXCHANGING = new ResourceLocation(MODID, "whitelist/exchanging");
        public static final ResourceLocation WHITELIST_DESTRUCTION = new ResourceLocation(MODID, "whitelist/destruction");
    }
}
