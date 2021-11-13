package com.direwolf20.buildinggadgets.common.network.S2C;

import com.direwolf20.buildinggadgets.common.network.C2S.PacketBindTool;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class LookupResult{

    public static void sendToClient(ServerPlayer player, boolean result) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(result);
        ServerPlayNetworking.send(player, PacketHandler.PacketLookupResult, buf);
    }


    public static class Client implements ClientPlayNetworking.PlayChannelHandler{

        @Override
        public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
            boolean result = buf.readBoolean();
            client.execute(() -> {
                if(client.player.isShiftKeyDown() && Screen.hasControlDown() && result)
                    PacketBindTool.send();
            });
        }
    }
}

