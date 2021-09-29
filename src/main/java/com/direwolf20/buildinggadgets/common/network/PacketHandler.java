package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.network.C2S.*;
import com.direwolf20.buildinggadgets.common.network.bidirection.PacketRequestTemplate;
import com.direwolf20.buildinggadgets.common.network.bidirection.SplitPacketUpdateTemplate;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class PacketHandler {

    public static final ResourceLocation PacketAnchor = BuildingGadgets.id("packet_anchor");
    public static final ResourceLocation PacketBindTool = BuildingGadgets.id("packet_bind_tool");
    public static final ResourceLocation PacketToggleFuzzy = BuildingGadgets.id("packet_toggle_fuzzy");
    public static final ResourceLocation PacketToggleFluidOnly = BuildingGadgets.id("packet_toggle_fluid_only");
    public static final ResourceLocation PacketToggleConnectedArea = BuildingGadgets.id("packet_toggle_connected_area");
    public static final ResourceLocation PacketToggleRayTraceFluid = BuildingGadgets.id("packet_toggle_ray_trace_fluid");
    public static final ResourceLocation PacketToggleBlockPlacement = BuildingGadgets.id("packet_toggle_block_placement");
    public static final ResourceLocation PacketChangeRange = BuildingGadgets.id("packet_change_range");
    public static final ResourceLocation PacketRotateMirror = BuildingGadgets.id("packet_rotate_mirror");
    public static final ResourceLocation PacketCopyCoords = BuildingGadgets.id("packet_copy_coords");
    public static final ResourceLocation PacketDestructionGUI = BuildingGadgets.id("packet_destruction_gui");
    public static final ResourceLocation PacketPasteGUI = BuildingGadgets.id("packet_paste_gui");
    public static final ResourceLocation PacketToggleMode = BuildingGadgets.id("packet_toggle_mode");
    public static final ResourceLocation PacketUndo = BuildingGadgets.id("packet_undo");

    public static final ResourceLocation PacketTemplateManagerTemplateCreated = BuildingGadgets.id("packet_template_manager_template_created");
    public static final ResourceLocation SplitPacketUpdateTemplate = BuildingGadgets.id("split_packet_update_template");
    public static final ResourceLocation PacketSetRemoteInventoryCache = BuildingGadgets.id("packet_set_remote_inventory_cache");

    public static final ResourceLocation PacketRequestTemplate = BuildingGadgets.id("packet_request_template");

    public static void registerMessages() {
        ServerPlayNetworking.registerGlobalReceiver(PacketAnchor, new PacketAnchor());
        ServerPlayNetworking.registerGlobalReceiver(PacketBindTool, new PacketBindTool());
        ServerPlayNetworking.registerGlobalReceiver(PacketToggleFuzzy, new PacketToggleFuzzy());
        ServerPlayNetworking.registerGlobalReceiver(PacketToggleFluidOnly, new PacketToggleFluidOnly());
        ServerPlayNetworking.registerGlobalReceiver(PacketToggleConnectedArea, new PacketToggleConnectedArea());
        ServerPlayNetworking.registerGlobalReceiver(PacketToggleRayTraceFluid, new PacketToggleRayTraceFluid());
        ServerPlayNetworking.registerGlobalReceiver(PacketToggleBlockPlacement, new PacketToggleBlockPlacement());
        ServerPlayNetworking.registerGlobalReceiver(PacketChangeRange, new PacketChangeRange());
        ServerPlayNetworking.registerGlobalReceiver(PacketRotateMirror, new PacketRotateMirror());
        ServerPlayNetworking.registerGlobalReceiver(PacketCopyCoords, new PacketCopyCoords());
        ServerPlayNetworking.registerGlobalReceiver(PacketDestructionGUI, new PacketDestructionGUI());
        ServerPlayNetworking.registerGlobalReceiver(PacketPasteGUI, new PacketPasteGUI());
        ServerPlayNetworking.registerGlobalReceiver(PacketToggleMode, new PacketToggleMode());
        ServerPlayNetworking.registerGlobalReceiver(PacketUndo, new PacketUndo());

        ServerPlayNetworking.registerGlobalReceiver(PacketTemplateManagerTemplateCreated, new PacketTemplateManagerTemplateCreated());
        ServerPlayNetworking.registerGlobalReceiver(SplitPacketUpdateTemplate, new SplitPacketUpdateTemplate());
        ServerPlayNetworking.registerGlobalReceiver(PacketSetRemoteInventoryCache, new PacketSetRemoteInventoryCache());

        ServerPlayNetworking.registerGlobalReceiver(PacketRequestTemplate, new PacketRequestTemplate());

    }

}
