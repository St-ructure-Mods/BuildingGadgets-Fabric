package com.direwolf20.buildinggadgets.common.network.bidirection;

import com.direwolf20.buildinggadgets.client.ClientProxy;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.Target;
import com.direwolf20.buildinggadgets.common.tainted.save.SaveManager;
import com.direwolf20.buildinggadgets.common.tainted.template.Template;
import com.direwolf20.buildinggadgets.common.tainted.template.TemplateIO;
import com.direwolf20.buildinggadgets.common.tainted.template.TemplateKey;
import com.direwolf20.buildinggadgets.common.util.exceptions.TemplateReadException;
import com.direwolf20.buildinggadgets.common.util.exceptions.TemplateWriteException;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

@EnvironmentInterface(value = EnvType.CLIENT, itf = ClientPlayNetworking.PlayChannelHandler.class)
public class SplitPacketUpdateTemplate implements ClientPlayNetworking.PlayChannelHandler, ServerPlayNetworking.PlayChannelHandler {

    public static void sendToTarget(Target target, UUID id, Template template) {
        if (target.flow() == PacketFlow.CLIENTBOUND) {
            sendToClient(id, template, target.player());
        } else {
            send(id, template);
        }
    }

    // S2C
    public static void sendToClient(UUID id, Template template, ServerPlayer player) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUUID(id);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            TemplateIO.writeTemplate(template, stream);
            buf.writeBytes(stream.toByteArray());
        } catch (TemplateWriteException e) {
            e.printStackTrace();
        }

        ServerPlayNetworking.send(player, PacketHandler.SplitPacketUpdateTemplate, buf);
    }

    // C2S
    public static void send(UUID id, Template template) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUUID(id);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            TemplateIO.writeTemplate(template, stream);
            buf.writeBytes(stream.toByteArray());
        } catch (TemplateWriteException e) {
            e.printStackTrace();
        }
        ClientPlayNetworking.send(PacketHandler.SplitPacketUpdateTemplate, buf);
    }

    public Template readTemplate(FriendlyByteBuf buf) throws TemplateReadException {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return TemplateIO.readTemplate(new ByteArrayInputStream(bytes), null);
    }

    // S2C
    @Environment(EnvType.CLIENT)
    @Override
    public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        UUID id = buf.readUUID();
        try {
            Template template = readTemplate(buf);
            client.execute(() -> ClientProxy.CACHE_TEMPLATE_PROVIDER.setTemplate(new TemplateKey(id), template));
        } catch (TemplateReadException e) {
            e.printStackTrace();
        }

    }

    // C2S
    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        UUID id = buf.readUUID();
        try {
            Template template = readTemplate(buf);
            server.execute(() -> SaveManager.INSTANCE.getTemplateProvider().setTemplate(new TemplateKey(id), template));
        } catch (TemplateReadException e) {
            e.printStackTrace();
        }

    }
}
