package com.direwolf20.buildinggadgets.common.network.bidirection;

import com.direwolf20.buildinggadgets.client.ClientProxy;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.Target;
import com.direwolf20.buildinggadgets.common.tainted.save.SaveManager;
import com.direwolf20.buildinggadgets.common.tainted.template.TemplateKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.UUID;

@EnvironmentInterface(value = EnvType.CLIENT, itf = ClientPlayNetworking.PlayChannelHandler.class)
public class PacketRequestTemplate implements ClientPlayNetworking.PlayChannelHandler, ServerPlayNetworking.PlayChannelHandler {

    public static void sendToTarget(Target target, UUID id) {
        if (target.flow() == PacketFlow.CLIENTBOUND) {
            sendToClient(target.player(), id);
        } else {
            send(id);
        }
    }

    public static void sendToClient(ServerPlayer player, UUID id) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUUID(id);
        ServerPlayNetworking.send(player, PacketHandler.PacketRequestTemplate, buf);
    }

    public static void send(UUID id) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUUID(id);
        ClientPlayNetworking.send(PacketHandler.PacketRequestTemplate, buf);
    }

    // S2C
    @Override
    @Environment(EnvType.CLIENT)
    public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        UUID id = buf.readUUID();

        client.execute(() -> ClientProxy.CACHE_TEMPLATE_PROVIDER.requestRemoteUpdate(new TemplateKey(id)));
    }

    // C2S
    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        UUID id = buf.readUUID();

        server.execute(() -> SaveManager.INSTANCE.getTemplateProvider().requestRemoteUpdate(new TemplateKey(id)));
    }
}
