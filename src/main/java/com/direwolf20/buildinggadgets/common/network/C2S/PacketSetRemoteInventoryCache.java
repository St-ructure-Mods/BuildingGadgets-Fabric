package com.direwolf20.buildinggadgets.common.network.C2S;

import com.direwolf20.buildinggadgets.client.EventUtil;
import com.direwolf20.buildinggadgets.client.renders.BaseRenderer;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryLinker;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

@EnvironmentInterface(value = EnvType.CLIENT, itf = ClientPlayNetworking.PlayChannelHandler.class)
public class PacketSetRemoteInventoryCache implements ServerPlayNetworking.PlayChannelHandler, ClientPlayNetworking.PlayChannelHandler {

    @Override
    @Environment(EnvType.CLIENT)
    public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        Data data = Data.read(buf);

        client.execute(() -> data.either.ifLeft(cache -> {
            if (data.isCopyPaste()) {
                EventUtil.setCache(cache.cache());
            } else {
                BaseRenderer.setInventoryCache(cache.cache());
            }
        }));
    }

    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        Data data = Data.read(buf);

        server.execute(() -> data.either.ifRight(location -> {
            Multiset<ItemVariant> items = HashMultiset.create();

            InventoryLinker.getLinkedInventory(player.level, location.blockPos, location.level, null).ifPresent(inventory -> {
                try (Transaction transaction = Transaction.openOuter()) {
                    Object2IntMap<Item> counts = new Object2IntOpenHashMap<>();

                    for (StorageView<ItemVariant> view : inventory.iterable(transaction)) {
                        if (!view.isResourceBlank()) {
                            Item item = view.getResource().getItem();
                            counts.put(item, counts.getInt(item) + 1);
                        }
                    }

                    for (StorageView<ItemVariant> view : inventory.iterable(transaction)) {
                        if (!view.isResourceBlank()) {
                            Item item = view.getResource().getItem();
                            ItemVariant ItemVariant = new ItemVariant(item);

                            if (!items.contains(ItemVariant)) {
                                items.add(ItemVariant, counts.getInt(item));
                            }
                        }
                    }
                }
            });

            send(new Data(data.isCopyPaste, Either.left(new Cache(ImmutableMultiset.copyOf(items)))), player);
        }));
    }

    private static void send(Data data, ServerPlayer player) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        data.write(buf);
        ServerPlayNetworking.send(player, PacketHandler.PacketSetRemoteInventoryCache, buf);
    }

    @Environment(EnvType.CLIENT)
    public static void send(boolean isCopyPaste, ResourceKey<Level> level, BlockPos blockPos) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(isCopyPaste);
        buf.writeBoolean(false);
        buf.writeResourceLocation(level.location());
        buf.writeBlockPos(blockPos);
    }

    private record Data(boolean isCopyPaste, Either<Cache, Location> either) {
        private static Data read(FriendlyByteBuf buf) {
            boolean isCopyPaste = buf.readBoolean();

            if (buf.readBoolean()) {
                int len = buf.readInt();
                ImmutableMultiset.Builder<ItemVariant> builder = ImmutableMultiset.builder();

                for (int i = 0; i < len; i++) {
                    builder.addCopies(new ItemVariant(Item.byId(buf.readInt())), buf.readInt());
                }

                return new Data(isCopyPaste, Either.left(new Cache(builder.build())));
            } else {
                return new Data(isCopyPaste, Either.right(new Location(ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation()), buf.readBlockPos())));
            }
        }

        private void write(FriendlyByteBuf buf) {
            buf.writeBoolean(isCopyPaste);

            either.mapBoth(cache -> {
                buf.writeBoolean(true);
                buf.writeInt(cache.cache().size());

                for (Multiset.Entry<ItemVariant> entry : cache.cache().entrySet()) {
                    ItemVariant ItemVariant = entry.getElement();

                    buf.writeInt(Item.getId(ItemVariant.createStack().getItem()));
                    buf.writeInt(entry.getCount());
                }
                return null;
            }, location -> {
                buf.writeBoolean(false);
                return null;
            });
        }
    }

    private record Cache(Multiset<ItemVariant> cache) {
    }

    private record Location(ResourceKey<Level> level, BlockPos blockPos) {
    }
}
