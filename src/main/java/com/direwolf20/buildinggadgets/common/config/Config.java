package com.direwolf20.buildinggadgets.common.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@me.shedaniel.autoconfig.annotation.Config(name = "building_gadgets")
public class Config implements ConfigData {

    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("general")
    public CategoryGeneral general = new CategoryGeneral();

    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("gadgets")
    public CategoryGadgets gadgets = new CategoryGadgets();

    public static class CategoryGeneral {
        @Comment("How far away players can build")
        @ConfigEntry.BoundedDiscrete(min = 1, max = 48)
        public double rayTraceRange = 32D;

        @Comment("Defined whether or not a player can use Absolute Coords mode in the Copy Paste Gadget")
        public boolean allowAbsoluteCoords = true;

        @Comment("(Client option) Determines if the Copy/Paste GUI's coordinate mode starts in 'Absolute' mode by default., Set to true for Absolute, set to False for Relative.")
        public boolean absoluteCoordDefault = false;

        @Comment("Whether the Building / CopyPaste Gadget can overwrite blocks like water, lava, grass, etc (like a player can).\n" +
                 "False will only allow it to overwrite air blocks.")
        public boolean allowOverwriteBlocks = true;
    }

    public static class CategoryGadgets {

        @Comment("The max range of the Gadgets")
        @ConfigEntry.BoundedDiscrete(min = 1, max = 32)
        public int maxRange = 15;

        @Comment("""
                Maximum amount of Blocks to be placed in one Tick.
                Notice that an EffectBlock takes 20 ticks to place, therefore a Server has to handle 20-times this value effect-block Tile's at once.  +
                Reduce this if  you notice lag-spikes from Players placing Templates.
                Of course decreasing this value will result in more time required to place large TemplateItem's.""")
        @ConfigEntry.BoundedDiscrete(min = 1, max = 4096)
        public int placeSteps = 1024;

        @ConfigEntry.Gui.CollapsibleObject
        @Comment("Energy Cost & Durability of the Building Gadget")
        public CategoryGadgetBuilding gadgetBuilding = new CategoryGadgetBuilding();

        @ConfigEntry.Gui.CollapsibleObject
        @Comment("Energy Cost & Durability of the Exchanging Gadget")
        public CategoryGadgetExchanger gadgetExchanger = new CategoryGadgetExchanger();

        @ConfigEntry.Gui.CollapsibleObject
        @Comment("Energy Cost, Durability & Maximum Energy of the Destruction Gadget")
        public CategoryGadgetDestruction gadgetDestruction = new CategoryGadgetDestruction();

        @Comment("Energy Cost & Durability of the Copy-Paste Gadget")
        @ConfigEntry.Gui.CollapsibleObject
        public CategoryGadgetCopyPaste gadgetCopyPaste = new CategoryGadgetCopyPaste();

        @Comment("Gadget Max Undo size (Note, the exchanger does not support undo)")
        @ConfigEntry.BoundedDiscrete(min = 0, max = 128)
        public int undoSize = 128;

        @Comment("Gadget undo list expiry time (in milliseconds)")
        @ConfigEntry.BoundedDiscrete(min = 0, max = 3600000) // 1 hour
        public int undoExpiry = 300000; // 5 minutes

        public static class CategoryGadgetBuilding {

            @Comment("The max energy of the Gadget, set to 0 to disable energy usage")
            @ConfigEntry.BoundedDiscrete(min = 0, max = 4000000)
            public long maxEnergy = 500000;

            @Comment("The Gadget's Energy cost per Operation")
            @ConfigEntry.BoundedDiscrete(min = 0, max = 4000000)
            public long energyCost = 50;
        }

        public static class CategoryGadgetExchanger {

            @Comment("The max energy of the Gadget, set to 0 to disable energy usage")
            @ConfigEntry.BoundedDiscrete(min = 0, max = 4000000)
            public long maxEnergy = 500000;

            @Comment("The Gadget's Energy cost per Operation")
            @ConfigEntry.BoundedDiscrete(min = 0, max = 4000000)
            public long energyCost = 50;
        }

        public static class CategoryGadgetDestruction {

            @Comment("The max energy of the Gadget, set to 0 to disable energy usage")
            @ConfigEntry.BoundedDiscrete(min = 0, max = 4000000)
            public long maxEnergy = 1000000;

            @Comment("The Gadget's Energy cost per Operation")
            @ConfigEntry.BoundedDiscrete(min = 0, max = 4000000)
            public long energyCost = 200;

            @Comment("The maximum dimensions, the Destruction Gadget can destroy.")
            @ConfigEntry.BoundedDiscrete(min = 0, max = 32)
            public int destroySize = 16;

            @Comment("The cost in energy/durability will increase by this amount when not in fuzzy mode")
            @ConfigEntry.BoundedDiscrete(min = 0, max = 4000000)
            public long nonFuzzyMultiplier = 2;

            @Comment("If enabled, the Destruction Gadget can be taken out of fuzzy mode, allowing only instances of the block "
                     + "clicked to be removed (at a higher cost)")
            public boolean nonFuzzyEnabled = false;
        }

        public static class CategoryGadgetCopyPaste {

            @Comment("The max energy of the Gadget, set to 0 to disable energy usage")
            @ConfigEntry.BoundedDiscrete(min = 0, max = 4000000)
            public long maxEnergy = 500000;

            @Comment("The Gadget's Energy cost per Operation")
            @ConfigEntry.BoundedDiscrete(min = 0, max = 4000000)
            public long energyCost = 50;

            @Comment("Maximum amount of Blocks to be copied in one Tick.\n" +
                     "Lower values may improve Server-Performance when copying large Templates")
            @ConfigEntry.BoundedDiscrete(min = 1, max = 65536)
            public int copySteps = 32768;

            @Comment("Maximum dimensions (x, y and z) that can be copied by a Template without requiring special permission.\n" +
                     "Permission can be granted using the '/buildinggadgets OverrideCopySize [<Player>]' command.")
            @ConfigEntry.BoundedDiscrete(min = -1, max = 2048)
            public int maxCopySize = 256;

            @Comment("Maximum dimensions (x, y and z) that can be build by a Template without requiring special permission.\n" +
                     "Permission can be granted using the '/buildinggadgets OverrideBuildSize [<Player>]' command.")
            @ConfigEntry.BoundedDiscrete(min = -1, max = 256)
            public int maxBuildSize = 256;
        }
    }
}
