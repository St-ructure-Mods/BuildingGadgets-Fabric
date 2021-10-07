package com.direwolf20.buildinggadgets.client.renders;

import com.direwolf20.buildinggadgets.common.items.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.GadgetExchanger;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public class BGRenderers {

    public static final BuildRender BUILD = new BuildRender(false);
    public static final BuildRender EXCHANGE = new BuildRender(true);
    public static final CopyPasteRender COPY_PASTE = new CopyPasteRender();
    public static final DestructionRender DESTRUCTION = new DestructionRender();

    @Nullable
    public static BaseRenderer find(Item item) {
        if (item instanceof GadgetBuilding) {
            return BUILD;
        } else if (item instanceof GadgetExchanger) {
            return EXCHANGE;
        } else if (item instanceof GadgetCopyPaste) {
            return COPY_PASTE;
        } else if (item instanceof GadgetDestruction) {
            return DESTRUCTION;
        } else {
            return null;
        }
    }
}
