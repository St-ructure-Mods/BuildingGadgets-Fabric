package com.direwolf20.buildinggadgets.client.events;

import com.direwolf20.buildinggadgets.client.KeyBindings;
import com.direwolf20.buildinggadgets.client.screen.GuiMod;
import com.direwolf20.buildinggadgets.client.screen.ModeRadialMenu;
import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.network.fabricpacket.C2S.*;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = Reference.MODID, value = Dist.CLIENT)
public class EventKeyInput {

    @SubscribeEvent
    public static void handleEventInput(ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || event.phase == Phase.START)
            return;

        if (KeyBindings.materialList.consumeClick()) {
            GuiMod.MATERIAL_LIST.openScreen(mc.player);
            return;
        }

        ItemStack tool = AbstractGadget.getGadget(mc.player);
        if (tool.isEmpty())
            return;

        KeyMapping mode = KeyBindings.menuSettings;
        if (!(mc.screen instanceof ModeRadialMenu) && mode.consumeClick() && ((mode.getKeyModifier() == KeyModifier.NONE
                && KeyModifier.getActiveModifier() == KeyModifier.NONE) || mode.getKeyModifier() != KeyModifier.NONE)) {
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
