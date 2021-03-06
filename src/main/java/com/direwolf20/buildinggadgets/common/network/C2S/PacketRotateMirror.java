package com.direwolf20.buildinggadgets.common.network.C2S;

import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PacketRotateMirror implements ServerPlayNetworking.PlayChannelHandler {

    public enum Operation {
        ROTATE, MIRROR
    }

    public static void send(@Nullable Operation operation) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        boolean hasOperation = operation != null;
        buf.writeBoolean(hasOperation);
        if (hasOperation) {
            buf.writeEnum(operation);
        }
        ClientPlayNetworking.send(PacketHandler.PacketRotateMirror, buf);
    }

    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        boolean hasOperation = buf.readBoolean();
        Operation operation = hasOperation ? buf.readEnum(Operation.class) : (player.isShiftKeyDown() ? Operation.MIRROR : Operation.ROTATE);

        server.execute(() -> {
            ItemStack stack = AbstractGadget.getGadget(player);

            if (stack.getItem() instanceof AbstractGadget item) {
                if (operation == Operation.MIRROR) {
                    item.onMirror(stack, player);
                } else {
                    item.onRotate(stack, player);
                }
            }
        });
    }
}
