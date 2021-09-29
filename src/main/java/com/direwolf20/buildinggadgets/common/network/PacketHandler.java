package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.network.packets.*;
import com.direwolf20.buildinggadgets.common.network.split.PacketSplitManager;
import com.direwolf20.buildinggadgets.common.network.split.SplitPacket;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.networking.client.ClientPlayNetworkAddon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = Integer.toString(4);
    private static short index = 0;
    private static final PacketSplitManager SPLIT_MANAGER = new PacketSplitManager();

    public static final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Reference.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static PacketSplitManager getSplitManager() {
        return SPLIT_MANAGER;
    }

    public static void register() {

        // Both Sides
        registerMessage(SplitPacket.class, SPLIT_MANAGER::encode, SPLIT_MANAGER::decode, SPLIT_MANAGER::handle);
        getSplitManager().registerSplitPacket(SplitPacketUpdateTemplate.class, SplitPacketUpdateTemplate::encode, SplitPacketUpdateTemplate::new, SplitPacketUpdateTemplate::handle);

        registerMessage(PacketSetRemoteInventoryCache.class, PacketSetRemoteInventoryCache::encode, PacketSetRemoteInventoryCache::decode, PacketSetRemoteInventoryCache.Handler::handle);
        registerMessage(PacketRequestTemplate.class, PacketRequestTemplate::encode, PacketRequestTemplate::new, PacketRequestTemplate::handle);

        //Client side
        registerMessage(PacketTemplateManagerTemplateCreated.class, PacketTemplateManagerTemplateCreated::encode, PacketTemplateManagerTemplateCreated::new, PacketTemplateManagerTemplateCreated::handle);
    }

    public static void sendTo(Object msg, ServerPlayer player) {
        HANDLER.sendTo(msg, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(Object msg) {
        HANDLER.sendToServer(msg);
    }

    private static <MSG> void registerMessage(Class<MSG> messageType, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
        HANDLER.registerMessage(index, messageType, encoder, decoder, messageConsumer);
        index++;
        if (index > 0xFF)
            throw new RuntimeException("Too many messages!");
    }
}
