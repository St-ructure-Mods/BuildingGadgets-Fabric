package com.direwolf20.buildinggadgets.common;

import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.commands.ForceUnloadedCommand;
import com.direwolf20.buildinggadgets.common.commands.OverrideBuildSizeCommand;
import com.direwolf20.buildinggadgets.common.commands.OverrideCopySizeCommand;
import com.direwolf20.buildinggadgets.common.compat.FLANCompat;
import com.direwolf20.buildinggadgets.common.compat.GOMLCompat;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.OurItems;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.tainted.registry.Registries;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BuildingGadgets implements ModInitializer {

    public static final Logger LOG = LogManager.getLogger();

    /**
     * Register our creative tab. Notice that we're also modifying the NBT data of the
     * building gadget to remove the damage / energy indicator from the creative
     * tabs icon.
     */
    public static final CreativeModeTab CREATIVE_TAB = FabricItemGroupBuilder.build(BuildingGadgets.id("tab"), () -> new ItemStack(OurItems.BUILDING_GADGET_ITEM));

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

        GOMLCompat.isLoaded = FabricLoader.getInstance().isModLoaded("goml");
        FLANCompat.isLoaded = FabricLoader.getInstance().isModLoaded("flan");
    }
}
