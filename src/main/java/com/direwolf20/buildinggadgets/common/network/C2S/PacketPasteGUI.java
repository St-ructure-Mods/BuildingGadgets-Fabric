package com.direwolf20.buildinggadgets.common.network.C2S;

import com.direwolf20.buildinggadgets.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

public class PacketPasteGUI implements ServerPlayNetworking.PlayChannelHandler {

    public static void send(int x, int y, int z) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ClientPlayNetworking.send(PacketHandler.PacketPasteGUI, buf);
    }

    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();

        server.execute(() -> {
            ItemStack heldItem = GadgetCopyPaste.getGadget(player);
            if (!heldItem.isEmpty()) GadgetCopyPaste.setRelativeVector(heldItem, new BlockPos(x, y, z));
        });
    }
}
