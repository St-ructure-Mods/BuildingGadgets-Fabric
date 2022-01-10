package com.direwolf20.buildinggadgets.common.compat;

import dev.ftb.mods.ftbchunks.data.ClaimedChunk;
import dev.ftb.mods.ftbchunks.data.FTBChunksAPI;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class FTBChunksCompat {

    public static boolean MOD_LOADED;

    public static boolean canUse(ServerPlayer player, BlockPos pos) {
        if(MOD_LOADED) {
            if (FTBChunksAPI.isManagerLoaded()) {
                ClaimedChunk chunk = FTBChunksAPI.getManager().getChunk(new ChunkDimPos(player.level, pos));
                if(chunk != null) return chunk.getTeamData().isTeamMember(player.getUUID());
            }
        }
        return true;
    }
}
