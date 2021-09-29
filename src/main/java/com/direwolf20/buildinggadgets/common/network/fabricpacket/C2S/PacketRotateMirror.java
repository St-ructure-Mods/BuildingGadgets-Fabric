package com.direwolf20.buildinggadgets.common.network.fabricpacket.C2S;

import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.network.fabricpacket.PacketHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

public class PacketRotateMirror implements ServerPlayNetworking.PlayChannelHandler {

    public enum Operation {
        ROTATE, MIRROR
    }

    public static void send(Operation operation) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        boolean hasOperation = operation != null;
        buf.writeBoolean(hasOperation);
        if(hasOperation) {
            buf.writeInt(operation.ordinal());
        }
        ClientPlayNetworking.send(PacketHandler.PacketRotateMirror, buf);
    }

    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        server.execute(() -> {
            boolean hasOperation = buf.readBoolean();
            Operation operation = null;
            ItemStack stack = AbstractGadget.getGadget(player);
            operation = hasOperation ? Operation.values()[buf.readInt()] : (player.isShiftKeyDown() ? Operation.MIRROR : Operation.ROTATE);

            if (operation == Operation.MIRROR) {
                ((AbstractGadget) stack.getItem()).onMirror(stack, player);
            } else {
                ((AbstractGadget) stack.getItem()).onRotate(stack, player);
            }
        });
    }
}
