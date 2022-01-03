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
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class SplitPacketUpdateTemplate {

    public static final int PAYLOAD_LIMIT = Short.MAX_VALUE;

    public static void sendToTarget(Target target, UUID id, Template template) {
        if (target.flow() == PacketFlow.CLIENTBOUND) {
            Server.send(id, template, target.player());
        } else {
            Client.send(id, template);
        }
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

        private FriendlyByteBuf accumulator;

        public static void send(UUID id, Template template) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            write(buf, id, template);

            while (buf.isReadable(PAYLOAD_LIMIT)) {
                ClientPlayNetworking.send(PacketHandler.SplitPacketUpdateTemplate, PacketByteBufs.readBytes(buf, PAYLOAD_LIMIT));
            }

            if (buf.isReadable()) {
                ClientPlayNetworking.send(PacketHandler.SplitPacketUpdateTemplate, buf);
            }

            // todo: sentinel value when length == 0, probably should use another marker packet instead but cope
            ClientPlayNetworking.send(PacketHandler.SplitPacketUpdateTemplate, PacketByteBufs.empty());
        }

        @Override
        public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
            if(accumulator == null) {
                accumulator = PacketByteBufs.create();
            }

            if (buf.isReadable()) {
                accumulator.writeBytes(buf);
                return;
            }

            UUID id = accumulator.readUUID();

            try {
                Template template = readTemplate(accumulator);
                client.execute(() -> BuildingGadgetsClient.CACHE_TEMPLATE_PROVIDER.setTemplate(new TemplateKey(id), template));
            } catch (TemplateReadException e) {
                e.printStackTrace();
            }

            accumulator.release();
            accumulator = null;
        }
    }

    public static class Server implements ServerPlayNetworking.PlayChannelHandler {

        private final Map<ServerPlayer, FriendlyByteBuf> buffers = new WeakHashMap<>();

        public static void send(UUID id, Template template, ServerPlayer player) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            write(buf, id, template);

            while (buf.isReadable(PAYLOAD_LIMIT)) {
                ServerPlayNetworking.send(player, PacketHandler.SplitPacketUpdateTemplate, PacketByteBufs.readBytes(buf, PAYLOAD_LIMIT));
            }

            if (buf.isReadable()) {
                ServerPlayNetworking.send(player, PacketHandler.SplitPacketUpdateTemplate, buf);
            }

            // todo: sentinel value when length == 0, probably should use another marker packet instead but cope
            ServerPlayNetworking.send(player, PacketHandler.SplitPacketUpdateTemplate, PacketByteBufs.empty());
        }

        @Override
        public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
            FriendlyByteBuf accumulator = buffers.computeIfAbsent(player, $ -> PacketByteBufs.create());

            if (buf.isReadable()) {
                accumulator.writeBytes(buf);
                return;
            }

            UUID id = accumulator.readUUID();

            try {
                Template template = readTemplate(accumulator);

                server.execute(() -> BGComponent.TEMPLATE_PROVIDER_COMPONENT.maybeGet(player.level).ifPresent(provider -> {
                    provider.setTemplate(new TemplateKey(id), template);
                }));
            } catch (TemplateReadException e) {
                e.printStackTrace();
            }

            buffers.remove(player).release();
        }
    }
}
