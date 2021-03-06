package com.direwolf20.buildinggadgets.common.network.C2S;

import com.direwolf20.buildinggadgets.common.items.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

public class PacketDestructionGUI implements ServerPlayNetworking.PlayChannelHandler {

    public static void send(int left, int right, int up, int down, int depth) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(left);
        buf.writeInt(right);
        buf.writeInt(up);
        buf.writeInt(down);
        buf.writeInt(depth);
        ClientPlayNetworking.send(PacketHandler.PacketDestructionGUI, buf);
    }

    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        int left = buf.readInt();
        int right = buf.readInt();
        int up = buf.readInt();
        int down = buf.readInt();
        int depth = buf.readInt();

        server.execute(() -> {
            ItemStack heldItem = GadgetDestruction.getGadget(player);
            if (!heldItem.isEmpty()) {
                GadgetDestruction.setToolValue(heldItem, left, NBTKeys.GADGET_VALUE_LEFT);
                GadgetDestruction.setToolValue(heldItem, right, NBTKeys.GADGET_VALUE_RIGHT);
                GadgetDestruction.setToolValue(heldItem, up, NBTKeys.GADGET_VALUE_UP);
                GadgetDestruction.setToolValue(heldItem, down, NBTKeys.GADGET_VALUE_DOWN);
                GadgetDestruction.setToolValue(heldItem, depth, NBTKeys.GADGET_VALUE_DEPTH);
            }
        });
    }
}
