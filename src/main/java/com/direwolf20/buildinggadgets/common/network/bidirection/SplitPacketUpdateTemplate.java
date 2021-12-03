package com.direwolf20.buildinggadgets.common.network.bidirection;

import com.direwolf20.buildinggadgets.client.BuildingGadgetsClient;
import com.direwolf20.buildinggadgets.common.component.BGComponent;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.Target;
import com.direwolf20.buildinggadgets.common.tainted.template.Template;
import com.direwolf20.buildinggadgets.common.tainted.template.TemplateIO;
import com.direwolf20.buildinggadgets.common.tainted.template.TemplateKey;
import com.direwolf20.buildinggadgets.common.util.exceptions.TemplateReadException;
import com.direwolf20.buildinggadgets.common.util.exceptions.TemplateWriteException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class SplitPacketUpdateTemplate {

    public static void sendToTarget(Target target, UUID id, Template template) {
        if (target.flow() == PacketFlow.CLIENTBOUND) {
            sendToClient(id, template, target.player());
        } else {
            Client.send(id, template);
        }
    }

    public static void sendToClient(UUID id, Template template, ServerPlayer player) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        write(buf, id, template);
        ServerPlayNetworking.send(player, PacketHandler.SplitPacketUpdateTemplate, buf);
    }

    private static Template readTemplate(FriendlyByteBuf buf) throws TemplateReadException {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return TemplateIO.readTemplate(new ByteArrayInputStream(bytes), null);
    }

    private static void write(FriendlyByteBuf buf, UUID id, Template template) {
        buf.writeUUID(id);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            TemplateIO.writeTemplate(template, stream);
            buf.writeBytes(stream.toByteArray());
        } catch (TemplateWriteException e) {
            e.printStackTrace();
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Client implements ClientPlayNetworking.PlayChannelHandler {

        public static void send(UUID id, Template template) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            write(buf, id, template);
            ClientPlayNetworking.send(PacketHandler.SplitPacketUpdateTemplate, buf);
        }

        @Override
        public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
            UUID id = buf.readUUID();

            try {
                Template template = readTemplate(buf);
                client.execute(() -> BuildingGadgetsClient.CACHE_TEMPLATE_PROVIDER.setTemplate(new TemplateKey(id), template));
            } catch (TemplateReadException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Server implements ServerPlayNetworking.PlayChannelHandler {

        @Override
        public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
            UUID id = buf.readUUID();

            try {
                Template template = readTemplate(buf);
                server.execute(() -> BGComponent.TEMPLATE_PROVIDER_COMPONENT.maybeGet(player.level).ifPresent(provider -> {
                    provider.setTemplate(new TemplateKey(id), template);
                }));
            } catch (TemplateReadException e) {
                e.printStackTrace();
            }
        }
    }
}
