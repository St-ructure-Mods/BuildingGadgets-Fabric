package com.direwolf20.buildinggadgets.common.containers;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.world.inventory.MenuType;

public final class OurContainers {
    public static final MenuType<TemplateManagerContainer> TEMPLATE_MANAGER_CONTAINER_TYPE = ScreenHandlerRegistry.registerExtended(BuildingGadgets.id("template_manager_container"), TemplateManagerContainer::new);
}
