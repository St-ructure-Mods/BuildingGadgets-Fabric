package com.direwolf20.buildinggadgets.common.compat;

import io.github.flemmli97.flan.api.ClaimHandler;
import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class FLANCompat {

    public static final boolean MOD_LOADED = FabricLoader.getInstance().isModLoaded("flan");

    public static boolean canUse(ServerPlayer player, BlockPos pos) {
        if (MOD_LOADED) {
            IPermissionContainer forPermissionCheck = ClaimHandler.getPermissionStorage(player.getLevel()).getForPermissionCheck(pos);
            return (forPermissionCheck.canInteract(player, PermissionRegistry.PLACE, pos) && forPermissionCheck.canInteract(player, PermissionRegistry.BREAK, pos));
        }

        return true;
    }
}
