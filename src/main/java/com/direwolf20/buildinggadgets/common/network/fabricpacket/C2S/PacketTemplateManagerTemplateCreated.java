package com.direwolf20.buildinggadgets.common.network.fabricpacket.C2S;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.TemplateManager;
import com.direwolf20.buildinggadgets.common.component.BGComponent;
import com.direwolf20.buildinggadgets.common.items.OurItems;
import com.direwolf20.buildinggadgets.common.network.fabricpacket.PacketHandler;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider;
import com.direwolf20.buildinggadgets.common.tileentities.TemplateManagerTileEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

public class PacketTemplateManagerTemplateCreated implements ServerPlayNetworking.PlayChannelHandler{

    UUID uuid;

    public UUID getUUID() {
        return uuid;
    }

    public static void send(UUID id, BlockPos pos) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUUID(id);
        buf.writeBlockPos(pos);
        ClientPlayNetworking.send(PacketHandler.PacketTemplateManagerTemplateCreated, buf);
    }

    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        server.execute(() -> {
            Level level = player.level;
            uuid = buf.readUUID();
            BlockPos pos = buf.readBlockPos();
            if(level.hasChunkAt(pos)) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if(blockEntity instanceof TemplateManagerTileEntity) {
                    ItemStack stack = new ItemStack(OurItems.TEMPLATE_ITEM);
                    ITemplateKey key = BGComponent.TEMPLATE_KEY_COMPONENT.get(stack);
                    UUID id = key.getTemplateId(this::getUUID);
                    if(!id.equals(getUUID())) {
                        BuildingGadgets.LOG.error("Failed to apply Template id on server!");
                    }
                    else {
                        ((TemplateManagerTileEntity) blockEntity).setItem(1, stack);
                        ITemplateProvider provider = BGComponent.TEMPLATE_PROVIDER_COMPONENT.getNullable(level);
                        provider.requestUpdate(key, player);
                    }
                }
            }
        });
    }
}
