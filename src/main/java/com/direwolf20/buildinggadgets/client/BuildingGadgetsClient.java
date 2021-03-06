package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.client.cache.CacheTemplateProvider;
import com.direwolf20.buildinggadgets.client.events.EventKeyInput;
import com.direwolf20.buildinggadgets.client.events.EventRenderWorldLast;
import com.direwolf20.buildinggadgets.client.renderer.EffectBlockTER;
import com.direwolf20.buildinggadgets.client.renders.BGRenderers;
import com.direwolf20.buildinggadgets.client.screen.TemplateManagerGUI;
import com.direwolf20.buildinggadgets.client.screen.tooltip.TemplateData;
import com.direwolf20.buildinggadgets.common.containers.OurContainers;
import com.direwolf20.buildinggadgets.common.network.ClientPacketHandler;
import com.direwolf20.buildinggadgets.common.tileentities.OurTileEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

public class BuildingGadgetsClient implements ClientModInitializer {

    public static final CacheTemplateProvider CACHE_TEMPLATE_PROVIDER = new CacheTemplateProvider();

    @Override
    public void onInitializeClient() {
        KeyBindings.initialize();
        WorldRenderEvents.AFTER_SETUP.register(EventRenderWorldLast::renderAfterSetup);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(EventRenderWorldLast::renderWorldLastEvent);
        MenuScreens.register(OurContainers.TEMPLATE_MANAGER_CONTAINER_TYPE, TemplateManagerGUI::new);
        ClientTickEvents.END_CLIENT_TICK.register(EventKeyInput::handleEventInput);
        BlockEntityRendererRegistry.register(OurTileEntities.EFFECT_BLOCK_TILE_ENTITY, EffectBlockTER::new);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> CACHE_TEMPLATE_PROVIDER.clear());
        CACHE_TEMPLATE_PROVIDER.registerUpdateListener(BGRenderers.COPY_PASTE);
        ClientPacketHandler.registerMessages();

        TooltipComponentCallback.EVENT.register(data -> {
            if(data instanceof TemplateData) {
                return ((TemplateData) data).clientTooltip();
            }
            return null;
        });
    }

    public static void playSound(SoundEvent sound, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch));
    }
}
