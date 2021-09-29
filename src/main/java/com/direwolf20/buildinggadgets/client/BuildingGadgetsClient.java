package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.client.events.EventRenderWorldLast;
import com.direwolf20.buildinggadgets.client.screen.TemplateManagerGUI;
import com.direwolf20.buildinggadgets.common.containers.OurContainers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

public class BuildingGadgetsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        WorldRenderEvents.LAST.register(EventRenderWorldLast::renderWorldLastEvent);
        ScreenRegistry.register(OurContainers.TEMPLATE_MANAGER_CONTAINER_TYPE, TemplateManagerGUI::new);
    }
}
