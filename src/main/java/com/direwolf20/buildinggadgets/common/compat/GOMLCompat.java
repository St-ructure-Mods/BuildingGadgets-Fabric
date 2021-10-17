package com.direwolf20.buildinggadgets.common.compat;

import com.jamieswhiteshirt.rtree3i.Entry;
import draylar.goml.api.Claim;
import draylar.goml.api.ClaimBox;
import draylar.goml.api.ClaimUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.stream.Collectors;

public class GOMLCompat {

    public static boolean isLoaded;

    public static boolean canUse(Level world, BlockPos pos, Player player) {
        if(isLoaded) {
           for(Entry<ClaimBox, Claim> entry : ClaimUtils.getClaimsAt(world, pos).collect(Collectors.toList())) {
                return ClaimUtils.playerHasPermission(entry, player);
           }
        }
        return true;
    }

}
