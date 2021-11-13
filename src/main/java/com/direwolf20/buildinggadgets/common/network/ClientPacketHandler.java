package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.network.S2C.LookupResult;
import com.direwolf20.buildinggadgets.common.network.bidirection.PacketSetRemoteInventoryCache;
import com.direwolf20.buildinggadgets.common.network.bidirection.PacketRequestTemplate;
import com.direwolf20.buildinggadgets.common.network.bidirection.SplitPacketUpdateTemplate;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientPacketHandler {

    public static void registerMessages() {
        ClientPlayNetworking.registerGlobalReceiver(PacketHandler.PacketRequestTemplate, new PacketRequestTemplate.Client());
        ClientPlayNetworking.registerGlobalReceiver(PacketHandler.SplitPacketUpdateTemplate, new SplitPacketUpdateTemplate.Client());
        ClientPlayNetworking.registerGlobalReceiver(PacketHandler.PacketSetRemoteInventoryCache, new PacketSetRemoteInventoryCache.Client());
        ClientPlayNetworking.registerGlobalReceiver(PacketHandler.PacketLookupResult, new LookupResult.Client());
    }
}
