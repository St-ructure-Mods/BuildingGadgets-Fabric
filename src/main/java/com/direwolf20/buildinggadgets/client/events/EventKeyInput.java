package com.direwolf20.buildinggadgets.client.events;

import com.direwolf20.buildinggadgets.client.KeyBindings;
import com.direwolf20.buildinggadgets.client.screen.GuiMod;
import com.direwolf20.buildinggadgets.client.screen.ModeRadialMenu;
import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.network.C2S.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class EventKeyInput {

    public static void handleEventInput(Minecraft mc) {
        if (mc.player == null) {
            return;
        }

        if (KeyBindings.materialList.consumeClick()) {
            GuiMod.MATERIAL_LIST.openScreen(mc.player);
            return;
        }

        ItemStack tool = AbstractGadget.getGadget(mc.player);

        if (tool.isEmpty()) {
            return;
        }

        if (!(mc.screen instanceof ModeRadialMenu) && KeyBindings.menuSettings.consumeClick()) {
            mc.setScreen(new ModeRadialMenu(tool));
        } else if (KeyBindings.range.consumeClick()) {
            PacketChangeRange.send();
        } else if (KeyBindings.rotateMirror.consumeClick()) {
            PacketRotateMirror.send(null);
        } else if (KeyBindings.undo.consumeClick()) {
            PacketUndo.send();
        } else if (KeyBindings.anchor.consumeClick()) {
            PacketAnchor.send();
        } else if (KeyBindings.fuzzy.consumeClick()) {
            PacketToggleFuzzy.send();
        } else if (KeyBindings.connectedArea.consumeClick()) {
            PacketToggleConnectedArea.send();
        }
    }
}
