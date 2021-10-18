package com.direwolf20.buildinggadgets.common.compat;

import io.github.flemmli97.flan.api.ClaimHandler;
import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class FLANCompat {

    public static boolean isLoaded;

    public static boolean canUse(ServerLevel world, BlockPos pos, ServerPlayer player) {
        if(isLoaded) {
            IPermissionContainer forPermissionCheck = ClaimHandler.getPermissionStorage(world).getForPermissionCheck(pos);
            return (forPermissionCheck.canInteract(player, PermissionRegistry.PLACE, pos) && forPermissionCheck.canInteract(player, PermissionRegistry.BREAK, pos));
        }
        return true;
    }
}
