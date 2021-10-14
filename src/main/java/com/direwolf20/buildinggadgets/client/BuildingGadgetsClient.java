package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.client.cache.CacheTemplateProvider;
import com.direwolf20.buildinggadgets.client.events.EventKeyInput;
import com.direwolf20.buildinggadgets.client.events.EventRenderWorldLast;
import com.direwolf20.buildinggadgets.client.renderer.EffectBlockTER;
import com.direwolf20.buildinggadgets.client.renders.BGRenderers;
import com.direwolf20.buildinggadgets.client.renders.CopyPasteRender;
import com.direwolf20.buildinggadgets.client.screen.TemplateManagerGUI;
import com.direwolf20.buildinggadgets.common.containers.OurContainers;
import com.direwolf20.buildinggadgets.common.items.OurItems;
import com.direwolf20.buildinggadgets.common.network.ClientPacketHandler;
import com.direwolf20.buildinggadgets.common.tileentities.OurTileEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.impl.client.rendering.WorldRenderContextImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

public class BuildingGadgetsClient implements ClientModInitializer {

    public static final CacheTemplateProvider CACHE_TEMPLATE_PROVIDER = new CacheTemplateProvider();

    @Override
    public void onInitializeClient() {
        KeyBindings.initialize();
        WorldRenderEvents.AFTER_SETUP.register(EventRenderWorldLast::renderAfterSetup);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(EventRenderWorldLast::renderWorldLastEvent);
        ScreenRegistry.register(OurContainers.TEMPLATE_MANAGER_CONTAINER_TYPE, TemplateManagerGUI::new);
        ClientTickEvents.END_CLIENT_TICK.register(EventKeyInput::handleEventInput);
        BlockEntityRendererRegistry.register(OurTileEntities.EFFECT_BLOCK_TILE_ENTITY, EffectBlockTER::new);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> CACHE_TEMPLATE_PROVIDER.clear());
        CACHE_TEMPLATE_PROVIDER.registerUpdateListener(BGRenderers.COPY_PASTE);
        ClientPacketHandler.registerMessages();

        ItemTooltipCallback.EVENT.register(EventUtil::printUUID);
    }

    public static void playSound(SoundEvent sound, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch));
    }
}
