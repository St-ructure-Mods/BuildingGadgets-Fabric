package com.direwolf20.buildinggadgets.common.network.C2S;

import com.direwolf20.buildinggadgets.client.events.EventTooltip;
import com.direwolf20.buildinggadgets.client.renders.BaseRenderer;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryLinker;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.UniqueItem;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.mojang.datafixers.util.Either;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;

@EnvironmentInterface(value = EnvType.CLIENT, itf = ClientPlayNetworking.PlayChannelHandler.class)
public class PacketSetRemoteInventoryCache implements ServerPlayNetworking.PlayChannelHandler, ClientPlayNetworking.PlayChannelHandler {

    @Override
    @Environment(EnvType.CLIENT)
    public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        Data data = Data.read(buf);

        client.execute(() -> data.either.ifLeft(cache -> {
            if (data.isCopyPaste()) {
                EventTooltip.setCache(cache.cache());
            } else {
                BaseRenderer.setInventoryCache(cache.cache());
            }
        }));
    }

    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        Data data = Data.read(buf);

        server.execute(() -> data.either.ifRight(location -> {
            Set<UniqueItem> itemTypes = new HashSet<>();
            ImmutableMultiset.Builder<UniqueItem> builder = ImmutableMultiset.builder();
            InventoryLinker.getLinkedInventory(player.level, location.blockPos, location.level, null).ifPresent(inventory -> {
                for (int i = 0; i < inventory.getSlots(); i++) {
                    ItemStack stack = inventory.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        Item item = stack.getItem();
                        UniqueItem uniqueItem = new UniqueItem(item);
                        if (!itemTypes.contains(uniqueItem)) {
                            itemTypes.add(uniqueItem);
                            builder.addCopies(uniqueItem, InventoryHelper.countInContainer(inventory, item));
                        }
                    }
                }
            });

            send(new Data(data.isCopyPaste, Either.left(new Cache(builder.build()))), player);
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
                ImmutableMultiset.Builder<UniqueItem> builder = ImmutableMultiset.builder();

                for (int i = 0; i < len; i++) {
                    builder.addCopies(new UniqueItem(Item.byId(buf.readInt())), buf.readInt());
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

                for (Multiset.Entry<UniqueItem> entry : cache.cache().entrySet()) {
                    UniqueItem uniqueItem = entry.getElement();

                    buf.writeInt(Item.getId(uniqueItem.createStack().getItem()));
                    buf.writeInt(entry.getCount());
                }
                return null;
            }, location -> {
                buf.writeBoolean(false);
                return null;
            });
        }
    }

    private record Cache(Multiset<UniqueItem> cache) {
    }

    private record Location(ResourceKey<Level> level, BlockPos blockPos) {
    }
}
