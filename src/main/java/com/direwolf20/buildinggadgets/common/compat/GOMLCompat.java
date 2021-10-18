package com.direwolf20.buildinggadgets.common.compat;

import draylar.goml.api.ClaimUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class GOMLCompat {

    public static final boolean MOD_LOADED = FabricLoader.getInstance().isModLoaded("goml");

    public static boolean canUse(ServerPlayer player, BlockPos pos) {
        if (MOD_LOADED) {
            return ClaimUtils.getClaimsAt(player.level, pos).anyMatch(entry -> ClaimUtils.playerHasPermission(entry, player));
        }

        return true;
    }
}
