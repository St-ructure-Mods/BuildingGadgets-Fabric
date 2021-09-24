package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.client.screen.TemplateManagerGUI;
import com.direwolf20.buildinggadgets.common.containers.OurContainers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

public class BuildingGadgetsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(OurContainers.TEMPLATE_MANAGER_CONTAINER_TYPE, TemplateManagerGUI::new);
    }
}
