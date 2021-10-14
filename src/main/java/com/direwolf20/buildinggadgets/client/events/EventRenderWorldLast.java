package com.direwolf20.buildinggadgets.client.events;

import com.direwolf20.buildinggadgets.client.renders.BGRenderers;
import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class EventRenderWorldLast {

    public static void renderAfterSetup(WorldRenderContext evt) {
        Player player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        ItemStack heldItem = AbstractGadget.getGadget(player);

        if (heldItem.isEmpty()) {
            return;
        }

        BGRenderers.find(heldItem.getItem()).renderAfterSetup(evt, player, heldItem);
    }

    public static void renderWorldLastEvent(WorldRenderContext evt) {
        Player player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        ItemStack heldItem = AbstractGadget.getGadget(player);

        if (heldItem.isEmpty()) {
            return;
        }

        BGRenderers.find(heldItem.getItem()).render(evt, player, heldItem);
    }
}
