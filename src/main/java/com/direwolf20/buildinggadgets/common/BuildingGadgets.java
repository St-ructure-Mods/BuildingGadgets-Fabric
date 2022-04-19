package com.direwolf20.buildinggadgets.common;

import com.direwolf20.buildinggadgets.client.OurSounds;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.commands.ForceUnloadedCommand;
import com.direwolf20.buildinggadgets.common.commands.OverrideBuildSizeCommand;
import com.direwolf20.buildinggadgets.common.commands.OverrideCopySizeCommand;
import com.direwolf20.buildinggadgets.common.compat.FLANCompat;
import com.direwolf20.buildinggadgets.common.compat.FTBChunksCompat;
import com.direwolf20.buildinggadgets.common.compat.GOMLCompat;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.containers.OurContainers;
import com.direwolf20.buildinggadgets.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.enchants.GadgetSilkTouch;
import com.direwolf20.buildinggadgets.common.items.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.OurItems;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.tainted.registry.Registries;
import com.direwolf20.buildinggadgets.common.tileentities.OurTileEntities;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.reborn.energy.api.EnergyStorageUtil;
import team.reborn.energy.api.base.SimpleBatteryItem;

public final class BuildingGadgets implements ModInitializer {

    public static final Logger LOG = LogManager.getLogger();

    /**
     * Register our creative tab. Notice that we're also modifying the NBT data of the
     * building gadget to remove the damage / energy indicator from the creative
     * tabs icon.
     */
    public static final CreativeModeTab CREATIVE_TAB = FabricItemGroupBuilder.build(BuildingGadgets.id("tab"), () -> {
        ItemStack stack = new ItemStack(OurItems.BUILDING_GADGET_ITEM);
        SimpleBatteryItem.setStoredEnergyUnchecked(stack, getConfig().gadgets.gadgetBuilding.maxEnergy);
        return stack;
    });

    public static ResourceLocation id(String path) {
        return new ResourceLocation(Reference.MODID, path);
    }

    public static Config getConfig() {
        return AutoConfig.getConfigHolder(Config.class).getConfig();
    }

    @Override
    public void onInitialize() {
        AutoConfig.register(Config.class, GsonConfigSerializer::new);
        OurBlocks.registerBlocks();
        OurItems.registerItems();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> server.getCommands().getDispatcher()
                .register(Commands.literal(Reference.MODID)
                        .then(OverrideBuildSizeCommand.registerToggle())
                        .then(OverrideCopySizeCommand.registerToggle())
                        .then(ForceUnloadedCommand.registerToggle())
                        .then(OverrideBuildSizeCommand.registerList())
                        .then(OverrideCopySizeCommand.registerList())
                        .then(ForceUnloadedCommand.registerList())));

        Registries.registerTileDataSerializers();
        PacketHandler.registerMessages();
        OurSounds.initSounds();
        OurTileEntities.initBE();
        OurContainers.TEMPLATE_MANAGER_CONTAINER_TYPE = Registry.register(Registry.MENU, BuildingGadgets.id("template_manager_container"), new ExtendedScreenHandlerType<>(TemplateManagerContainer::new));

        Registry.register(Registry.ENCHANTMENT, id("silk_touch"), GadgetSilkTouch.GADGET_SILKTOUCH);

        GOMLCompat.MOD_LOADED = FabricLoader.getInstance().isModLoaded("goml");
        FLANCompat.MOD_LOADED = FabricLoader.getInstance().isModLoaded("flan");
        FTBChunksCompat.MOD_LOADED = FabricLoader.getInstance().isModLoaded("ftbchunks");
    }
}
