package com.direwolf20.buildinggadgets.common.network.fabricpacket.C2S;

import com.direwolf20.buildinggadgets.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.network.fabricpacket.PacketHandler;
import com.direwolf20.buildinggadgets.common.tainted.building.Region;
import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
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

import java.util.Optional;

public class PacketCopyCoords implements ServerPlayNetworking.PlayChannelHandler{

    public static void send(BlockPos start, BlockPos end) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(start);
        buf.writeBlockPos(end);
        ClientPlayNetworking.send(PacketHandler.PacketCopyCoords, buf);
    }

    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        server.execute(() -> {
            ItemStack heldItem = GadgetCopyPaste.getGadget(player);
            if (heldItem.isEmpty()) return;

            BlockPos startPos = buf.readBlockPos();
            BlockPos endPos = buf.readBlockPos();
            if (startPos.equals(BlockPos.ZERO) && endPos.equals(BlockPos.ZERO)) {
                GadgetCopyPaste.setSelectedRegion(heldItem, null);
                player.displayClientMessage(MessageTranslation.AREA_RESET.componentTranslation().setStyle(Styles.AQUA), true);
            } else {
                GadgetCopyPaste.setSelectedRegion(heldItem, new Region(startPos, endPos));
            }

            Optional<Region> regionOpt = GadgetCopyPaste.getSelectedRegion(heldItem);
            if (regionOpt.isEmpty()) //notify of single copy
                player.displayClientMessage(MessageTranslation.FIRST_COPY.componentTranslation().setStyle(Styles.DK_GREEN), true);
            regionOpt.ifPresent(region -> ((GadgetCopyPaste) heldItem.getItem()).tryCopy(heldItem, player.level, player, region));
        });
    }
}
