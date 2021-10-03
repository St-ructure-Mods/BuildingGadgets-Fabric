package com.direwolf20.buildinggadgets.common;

import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.commands.ForceUnloadedCommand;
import com.direwolf20.buildinggadgets.common.commands.OverrideBuildSizeCommand;
import com.direwolf20.buildinggadgets.common.commands.OverrideCopySizeCommand;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.config.RecipeConstructionPaste.Serializer;
import com.direwolf20.buildinggadgets.common.events.BlockPlaceEvent;
import com.direwolf20.buildinggadgets.common.items.OurItems;
import com.direwolf20.buildinggadgets.common.tainted.registry.Registries;
import com.direwolf20.buildinggadgets.common.tainted.save.SaveManager;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BuildingGadgets implements ModInitializer {

    public static Logger LOG = LogManager.getLogger();

    /**
     * Register our creative tab. Notice that we're also modifying the NBT data of the
     * building gadget to remove the damage / energy indicator from the creative
     * tabs icon.
     */
    public static CreativeModeTab creativeTab = FabricItemGroupBuilder.build(BuildingGadgets.id("tab"), () -> {
        ItemStack stack = new ItemStack(OurItems.BUILDING_GADGET_ITEM);
        stack.getOrCreateTag().putByte(NBTKeys.CREATIVE_MARKER, (byte) 0);
        return stack;
    });

    public static Config config;

    public static ResourceLocation id(String path) {
        return new ResourceLocation(Reference.MODID, path);
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

    @Override
    public void onInitialize() {
        AutoConfig.register(Config.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(Config.class).getConfig();
        OurBlocks.registerBlocks();
        OurItems.registerItems();
        serverLoad();
        serverLoaded();
        serverStopped();

        Registries.registerTileDataSerializers();
        Registries.registerUniqueObjectSerializers();

        Registry.register(Registry.RECIPE_SERIALIZER, new ResourceLocation(Reference.MODID, "construction_paste"), Serializer.INSTANCE);

        BlockPlaceEvent.ON_PLACE.register((serverPlayer, level, itemStack, interactionHand, blockHitResult) -> {

        });
    }
}
