package com.direwolf20.buildinggadgets.common;

import com.direwolf20.buildinggadgets.client.ClientProxy;
import com.direwolf20.buildinggadgets.client.renderer.EffectBlockTER;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.commands.ForceUnloadedCommand;
import com.direwolf20.buildinggadgets.common.commands.OverrideBuildSizeCommand;
import com.direwolf20.buildinggadgets.common.commands.OverrideCopySizeCommand;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.config.RecipeConstructionPaste.Serializer;
import com.direwolf20.buildinggadgets.common.items.OurItems;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.tainted.registry.Registries;
import com.direwolf20.buildinggadgets.common.tainted.save.SaveManager;
import com.direwolf20.buildinggadgets.common.tileentities.OurTileEntities;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BuildingGadgets implements ModInitializer {

    public static Logger LOG = LogManager.getLogger();

    /**
     * Register our creative tab. Notice that we're also modifying the NBT data of the
     * building gadget to remove the damage / energy indicator from the creative
     * tabs icon.
     */

    public static Config config;

    public static CreativeModeTab creativeTab = FabricItemGroupBuilder.build(BuildingGadgets.id("tab"), () -> {
        ItemStack stack = new ItemStack(OurItems.BUILDING_GADGET_ITEM);
        stack.getOrCreateTag().putByte(NBTKeys.CREATIVE_MARKER, (byte) 0);
        return stack;
    });

    public static ResourceLocation id(String path) {
        return new ResourceLocation(Reference.MODID, path);
    }

    private static BuildingGadgets theMod = null;

    public static BuildingGadgets getInstance() {
        assert theMod != null;
        return theMod;
    }

    public BuildingGadgets() {


        eventBus.addListener(this::registerRegistries);
        eventBus.addListener(this::setup);
        eventBus.addListener(this::clientSetup);
        eventBus.addListener(this::loadComplete);
        eventBus.addListener(this::handleIMC);

        eventBus.addGenericListener(RecipeSerializer.class, this::onRecipeRegister);
        eventBus.addListener(this::onEnqueueIMC);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
//        ClientRegistry.bindTileEntityRenderer(OurTileEntities.EFFECT_BLOCK_TILE_ENTITY.get(), EffectBlockTER::new);
        BlockEntityRenderers.register(OurTileEntities.EFFECT_BLOCK_TILE_ENTITY, EffectBlockTER::new);
        ClientProxy.clientSetup(FMLJavaModLoadingContext.get().getModEventBus());
//        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> GuiMod::openScreen);
    }

    private void setup(final FMLCommonSetupEvent event) {
        theMod = (BuildingGadgets) ModLoadingContext.get().getActiveContainer().getMod();

        CapabilityTemplate.register();
    }

    private void registerRegistries(RegistryEvent.NewRegistry event) {
        Registries.onCreateRegistries();
    }

    private void loadComplete(FMLLoadCompleteEvent event) {
        Registries.createOrderedRegistries();
    }

    private void handleIMC(InterModProcessEvent event) {
        event.getIMCStream().forEach(this::handleIMCMessage);
    }

    private void handleIMCMessage(InterModComms.IMCMessage message) {
        if (Registries.handleIMC(message))
            LOG.trace("Successfully handled IMC-Message using Method {} from Mod {}.", message.getMethod(), message.getSenderModId());
        else
            LOG.warn("Failed to handle IMC-Message using Method {} from Mod {}!", message.getMethod(), message.getSenderModId());
    }

    private void serverLoad() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            server.getCommands().getDispatcher().register(
                    Commands.literal(Reference.MODID)
                            .then(OverrideBuildSizeCommand.registerToggle())
                            .then(OverrideCopySizeCommand.registerToggle())
                            .then(ForceUnloadedCommand.registerToggle())
                            .then(OverrideBuildSizeCommand.registerList())
                            .then(OverrideCopySizeCommand.registerList())
                            .then(ForceUnloadedCommand.registerList()));
        });
    }

    private void serverLoaded() {
        SaveManager.INSTANCE.onServerStarted();
    }

    private void serverStopped() {
        SaveManager.INSTANCE.onServerStopped();
    }

    private void onEnqueueIMC(InterModEnqueueEvent event) {
        InventoryHelper.registerHandleProviders();
    }

    @Override
    public void onInitialize() {
        AutoConfig.register(Config.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(Config.class).getConfig();
        OurBlocks.registerBlocks();
        OurItems.registerItems();
        serverLoad();
        serverLoaded();
        serverStopped();

        Registry.register(Registry.RECIPE_SERIALIZER, new ResourceLocation(Reference.MODID, "construction_paste"), Serializer.INSTANCE);
    }
}
