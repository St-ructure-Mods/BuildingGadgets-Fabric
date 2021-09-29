package com.direwolf20.buildinggadgets.client.events;

import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class EventRenderWorldLast {

    public static void renderWorldLastEvent(WorldRenderContext evt) {
        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;

        ItemStack heldItem = AbstractGadget.getGadget(player);
        if (heldItem.isEmpty())
            return;

        ((AbstractGadget) heldItem.getItem()).getRender().render(evt, player, heldItem);
    }
}
