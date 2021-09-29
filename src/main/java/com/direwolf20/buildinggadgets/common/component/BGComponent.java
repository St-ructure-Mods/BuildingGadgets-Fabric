package com.direwolf20.buildinggadgets.common.component;

import com.direwolf20.buildinggadgets.client.cache.CacheTemplateProvider;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.capability.ItemTemplateKey;
import com.direwolf20.buildinggadgets.common.items.OurItems;
import com.direwolf20.buildinggadgets.common.tainted.save.SaveTemplateProvider;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;

public class BGComponent implements ItemComponentInitializer, WorldComponentInitializer {
    public static final ComponentKey<ITemplateProvider> TEMPLATE_PROVIDER_COMPONENT = ComponentRegistryV3.INSTANCE.getOrCreate(BuildingGadgets.id("template_provider"), ITemplateProvider.class);
    public static final ComponentKey<ITemplateKey> TEMPLATE_KEY_COMPONENT = ComponentRegistryV3.INSTANCE.getOrCreate(BuildingGadgets.id("template_key"), ITemplateKey.class);

    @Override
    public void registerItemComponentFactories(ItemComponentFactoryRegistry registry) {
        registry.register(OurItems.TEMPLATE_ITEM, TEMPLATE_KEY_COMPONENT, ItemTemplateKey::new);
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(TEMPLATE_PROVIDER_COMPONENT, CacheTemplateProvider::new);
        registry.register(TEMPLATE_PROVIDER_COMPONENT, SaveTemplateProvider::new);
    }
}
