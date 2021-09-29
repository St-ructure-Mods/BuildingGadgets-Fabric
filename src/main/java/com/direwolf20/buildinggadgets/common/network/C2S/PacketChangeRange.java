package com.direwolf20.buildinggadgets.common.network.C2S;

import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

public class PacketChangeRange implements ServerPlayNetworking.PlayChannelHandler {

    public static void send() {
        send(-1);
    }

    public static void send(int range) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(range);
        ClientPlayNetworking.send(PacketHandler.PacketChangeRange, buf);
    }

    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        int range = buf.readInt();

        server.execute(() -> {
            ItemStack stack = AbstractGadget.getGadget(player);

            if (range >= 0) {
                GadgetUtils.setToolRange(stack, range);
            } else if (stack.getItem() instanceof GadgetBuilding) {
                GadgetBuilding.rangeChange(player, stack);
            } else if (stack.getItem() instanceof GadgetExchanger) {
                GadgetExchanger.rangeChange(player, stack);
            } else if (stack.getItem() instanceof GadgetDestruction) {
                GadgetDestruction.switchOverlay(player, stack);
            }
        });
    }
}
