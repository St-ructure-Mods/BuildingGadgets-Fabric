package com.direwolf20.buildinggadgets.common.util.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public enum TooltipTranslation implements ITranslationProvider {
    GADGET_BLOCK("gadget.block", 1),
    GADGET_DESTROYSHOWOVERLAY("gadget.destroyshowoverlay", 1),
    GADGET_DESTROYWARNING("gadget.destroywarning", 0),
    GADGET_ENERGY("gadget.energy", 2),
    GADGET_MODE("gadget.mode", 1),
    GADGET_RANGE("gadget.range", 2),
    GADGET_FUZZY("gadget.fuzzy", 1),
    GADGET_RAYTRACE_FLUID("gadget.raytrace_fluid", 1),
    GADGET_BUILDING_PLACE_ATOP("gadget.building.place_atop", 1),
    GADGET_CONNECTED("gadget.connected", 1),
    GADGET_CONNECTED_AREA("gadget.connected_area", 0),
    GAGDGET_CONNECTED_SURFACE("gadget.connected_surface", 0),
    GADGET_MIRROR("gadget.mirror", 0),
    GADGET_ANCHOR("gadget.anchor", 0),
    GADGET_UNDO("gadget.undo", 0),
    GADGET_ROTATE("gadget.rotate", 0),
    GADGET_PALETTE_OVERFLOW("gadget.paletteOverflow", 0),
    DONOTUSE_TEXT("donotuse", 0),
    TEMPLATE_NAME("template.name", 1),
    TEMPLATE_AUTHOR("template.author", 1),
    CHARGER_ENERGY("charger.energy", 1),
    CHARGER_BURN("charger.burn_time", 1),
    CHARGER_EMPTY("charger.fuel_empty", 0);

    private static final String PREFIX = "tooltip.";
    private final String key;
    private final int argCount;

    TooltipTranslation(@NotNull String key, @Range(from = 0, to = Integer.MAX_VALUE) int argCount) {
        this.key = PREFIX + key;
        this.argCount = argCount;
    }

    @Override
    public boolean areValidArguments(Object... args) {
        return args.length == argCount;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}
