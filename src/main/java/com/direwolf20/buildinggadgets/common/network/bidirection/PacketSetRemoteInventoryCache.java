package com.direwolf20.buildinggadgets.common.network.bidirection;

import com.direwolf20.buildinggadgets.client.renders.BaseRenderer;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryLinker;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.mojang.datafixers.util.Either;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;

public class PacketSetRemoteInventoryCache {

    public static class Client implements ClientPlayNetworking.PlayChannelHandler {

        @Override
        @Environment(EnvType.CLIENT)
        public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
            Data data = Data.read(buf);

            client.execute(() -> data.either.ifLeft(cache -> {
                if (!data.isCopyPaste()) {
                    BaseRenderer.setInventoryCache(cache.cache());
                }
            }));
        }
    }

    public static class Server implements ServerPlayNetworking.PlayChannelHandler {

        @Override
        public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
            Data data = Data.read(buf);

            server.execute(() -> data.either.ifRight(hand -> {
                Multiset<ItemVariant> items = HashMultiset.create();

                InventoryLinker.getLinkedInventory(player.level, player.getItemInHand(hand)).ifPresent(inventory -> {
                    try (Transaction transaction = Transaction.openOuter()) {
                        for (StorageView<ItemVariant> view : inventory.iterable(transaction)) {
                            if (!view.isResourceBlank()) {
                                ItemVariant resource = view.getResource();
                                items.add(resource, (int) view.getAmount());
                            }
                        }
                    }
                });

                send(new Data(data.isCopyPaste, Either.left(new Cache(ImmutableMultiset.copyOf(items)))), player);
            }));
        }
    }

    private static void send(Data data, ServerPlayer player) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        data.write(buf);
        ServerPlayNetworking.send(player, PacketHandler.PacketSetRemoteInventoryCache, buf);
    }

    @Environment(EnvType.CLIENT)
    public static void send(boolean isCopyPaste, InteractionHand hand) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        new Data(isCopyPaste, Either.right(hand)).write(buf);
        ClientPlayNetworking.send(PacketHandler.PacketSetRemoteInventoryCache, buf);
    }

    private record Data(boolean isCopyPaste, Either<Cache, InteractionHand> either) {
        private static Data read(FriendlyByteBuf buf) {
            boolean isCopyPaste = buf.readBoolean();

            if (buf.readBoolean()) {
                int len = buf.readInt();
                ImmutableMultiset.Builder<ItemVariant> builder = ImmutableMultiset.builder();

                for (int i = 0; i < len; i++) {
                    builder.addCopies(ItemVariant.fromPacket(buf), buf.readInt());
                }

                return new Data(isCopyPaste, Either.left(new Cache(builder.build())));
            } else {
                InteractionHand hand = buf.readEnum(InteractionHand.class);
                return new Data(isCopyPaste, Either.right(hand));
            }
        }

        private void write(FriendlyByteBuf buf) {
            buf.writeBoolean(isCopyPaste);

            either.mapBoth(cache -> {
                buf.writeBoolean(true);
                buf.writeInt(cache.cache().entrySet().size());

                for (Multiset.Entry<ItemVariant> entry : cache.cache().entrySet()) {
                    ItemVariant uniqueItem = entry.getElement();

                    uniqueItem.toPacket(buf);
                    buf.writeInt(entry.getCount());
                }

                return null;
            }, hand -> {
                buf.writeBoolean(false);
                buf.writeEnum(hand);
                return null;
            });
        }
    }

    private record Cache(Multiset<ItemVariant> cache) {
    }
}
