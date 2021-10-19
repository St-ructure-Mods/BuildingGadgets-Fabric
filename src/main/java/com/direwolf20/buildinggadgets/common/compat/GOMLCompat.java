package com.direwolf20.buildinggadgets.common.compat;

import com.jamieswhiteshirt.rtree3i.Entry;
import draylar.goml.api.Claim;
import draylar.goml.api.ClaimBox;
import draylar.goml.api.ClaimUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.stream.Collectors;

public class GOMLCompat {

    public static boolean MOD_LOADED;

    public static boolean canUse(ServerPlayer player, BlockPos pos) {
        if (MOD_LOADED) {
            for(Entry<ClaimBox, Claim> entry : ClaimUtils.getClaimsAt(player.level, pos).collect(Collectors.toList())) {
                return ClaimUtils.playerHasPermission(entry, player);
            }
        }

        return true;
    }
}
