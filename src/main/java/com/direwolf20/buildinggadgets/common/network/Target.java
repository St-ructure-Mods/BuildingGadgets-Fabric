package com.direwolf20.buildinggadgets.common.network;

import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;

public record Target(PacketFlow flow, ServerPlayer player) {

    @Override
    public ServerPlayer player() {
        if (flow == PacketFlow.CLIENTBOUND) {
            return player;
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
