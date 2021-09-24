package com.direwolf20.buildinggadgets.common.component;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider;
import dev.onyxstudios.cca.api.v3.block.BlockComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.block.BlockComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;

public class BGComponent implements ItemComponentInitializer, BlockComponentInitializer, WorldComponentInitializer {
    public static final ComponentKey<ITemplateProvider> TEMPLATE_PROVIDER_COMPONENT = ComponentRegistryV3.INSTANCE.getOrCreate(BuildingGadgets.id("template_provider"), ITemplateProvider.class);
    public static final ComponentKey<ITemplateKey> TEMPLATE_KEY_COMPONENT = ComponentRegistryV3.INSTANCE.getOrCreate(BuildingGadgets.id("template_key"), ITemplateKey.class);

    @Override
    public void registerItemComponentFactories(ItemComponentFactoryRegistry registry) {

    }

    @Override
    public void registerBlockComponentFactories(BlockComponentFactoryRegistry registry) {

    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {

    }
}
