package com.direwolf20.buildinggadgets.common.network.fabricpacket;

import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;

public record Target(PacketFlow flow, ServerPlayer player) {

    public static Target toServer() {
        return null;
    }

    public static Target toClient(ServerPlayer player) {
        return null;
    }

    @Override
    public ServerPlayer player() {
        if (flow == PacketFlow.CLIENTBOUND)
            return player;
        else return null;
    }
}
