package com.direwolf20.buildinggadgets.common.component;

import com.direwolf20.buildinggadgets.client.BuildingGadgetsClient;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.OurItems;
import com.direwolf20.buildinggadgets.common.tainted.save.SaveTemplateProvider;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;
import dev.onyxstudios.cca.api.v3.level.LevelComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.level.LevelComponentInitializer;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import net.minecraft.server.level.ServerLevel;

public class BGComponent implements ItemComponentInitializer, WorldComponentInitializer, LevelComponentInitializer {

    public static final ComponentKey<ITemplateProvider> TEMPLATE_PROVIDER_COMPONENT = ComponentRegistryV3.INSTANCE.getOrCreate(BuildingGadgets.id("template_provider"), ITemplateProvider.class);
    public static final ComponentKey<ITemplateKey> TEMPLATE_KEY_COMPONENT = ComponentRegistryV3.INSTANCE.getOrCreate(BuildingGadgets.id("template_key"), ITemplateKey.class);
    public static final ComponentKey<UndoService> UNDO_COMPONENT = ComponentRegistryV3.INSTANCE.getOrCreate(BuildingGadgets.id("undo"), UndoService.class);

    @Override
    public void registerItemComponentFactories(ItemComponentFactoryRegistry registry) {
        registry.register(OurItems.TEMPLATE_ITEM, TEMPLATE_KEY_COMPONENT, ItemTemplateKey::new);
        registry.register(OurItems.COPY_PASTE_GADGET_ITEM, TEMPLATE_KEY_COMPONENT, ItemTemplateKey::new);
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(TEMPLATE_PROVIDER_COMPONENT, world -> {
            if (world instanceof ServerLevel) {
                return new SaveTemplateProvider();
            } else {
                return BuildingGadgetsClient.CACHE_TEMPLATE_PROVIDER;
            }
        });
    }

    @Override
    public void registerLevelComponentFactories(LevelComponentFactoryRegistry registry) {
        registry.register(UNDO_COMPONENT, levelData -> new UndoService());
    }
}
