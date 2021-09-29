package com.direwolf20.buildinggadgets.common.network.fabricpacket.C2S;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.GadgetExchanger;
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

public class PacketToggleFuzzy implements ServerPlayNetworking.PlayChannelHandler{

    public static void send() {
        ClientPlayNetworking.send(PacketHandler.PacketToggleFuzzy, PacketByteBufs.empty());
    }

    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        server.execute(() -> {
            ItemStack stack = AbstractGadget.getGadget(player);
            if(stack.getItem() instanceof GadgetExchanger || stack.getItem() instanceof GadgetBuilding || (stack.getItem() instanceof GadgetDestruction && BuildingGadgets.config.GADGETS.GADGET_DESTRUCTION.nonFuzzyEnabled)) {
                AbstractGadget.toggleFuzzy(player, stack);
            }
        });
    }
}
