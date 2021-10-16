package com.direwolf20.buildinggadgets.common.network.C2S;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.component.BGComponent;
import com.direwolf20.buildinggadgets.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.OurItems;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.Target;
import com.direwolf20.buildinggadgets.common.tileentities.TemplateManagerTileEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

public class PacketTemplateManagerTemplateCreated implements ServerPlayNetworking.PlayChannelHandler {

    public static void send(UUID id, BlockPos pos) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUUID(id);
        buf.writeBlockPos(pos);
        ClientPlayNetworking.send(PacketHandler.PacketTemplateManagerTemplateCreated, buf);
    }

    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        UUID uuid = buf.readUUID();
        BlockPos pos = buf.readBlockPos();

        server.execute(() -> {
            Level level = player.level;
            if (level.hasChunkAt(pos)) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof TemplateManagerTileEntity manager) {
                    ItemStack stack = new ItemStack(OurItems.TEMPLATE_ITEM);
                    BGComponent.TEMPLATE_KEY_COMPONENT.maybeGet(stack).ifPresent(key -> {
                        UUID id = key.getOrComputeId(() -> uuid);

                        if (!id.equals(uuid)) {
                            BuildingGadgets.LOG.error("Failed to apply Template id on server!");
                        } else {
                            if(!(manager.getItem(1).getItem() instanceof GadgetCopyPaste)) {
                                manager.setItem(1, stack);
                            }
                            BGComponent.TEMPLATE_PROVIDER_COMPONENT.maybeGet(level).ifPresent(provider -> provider.requestUpdate(key, new Target(PacketFlow.CLIENTBOUND, player)));
                        }
                    });
                }
            }
        });
    }
}
