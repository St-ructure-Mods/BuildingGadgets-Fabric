package com.direwolf20.buildinggadgets.common.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

public class Config implements ConfigData {

    @ConfigEntry.Category("General")
    @Comment("General mod settings")
    public final CategoryGeneral GENERAL = new CategoryGeneral();
    @ConfigEntry.Category("Gadgets")
    @Comment("Configure the Gadgets")
    public final CategoryGadgets GADGETS = new CategoryGadgets();

    public static final class CategoryGeneral {
        @Comment("Defines how far away you can build")
        @ConfigEntry.BoundedDiscrete(min = 1, max = 48)
        public final double rayTraceRange = 32D;
        //Translate option as MaxBuildDistance

        @Comment("Defined whether or not a player can use Absolute Coords mode in the Copy Paste Gadget")
        public final boolean allowAbsoluteCoords = true;
        //Translate option as Allow Absolute Coords
        /* Client Only!*/
        @Comment("Determines if the Copy/Paste GUI's coordinate mode starts in 'Absolute' mode by default.\", \"Set to true for Absolute, set to False for Relative.")
        public final boolean absoluteCoordDefault = false;
        //Translate option as Default to absolute Coord-Mode

        @Comment("Whether the Building / CopyPaste Gadget can overwrite blocks like water, lava, grass, etc (like a player can).\",\n" +
                "                            \"False will only allow it to overwrite air blocks.")
        public final boolean allowOverwriteBlocks = true;
        //Translate option as Allow non-Air-Block-Overwrite
    }

    public static final class CategoryGadgets {

        @Comment("The max range of the Gadgets")
        @ConfigEntry.BoundedDiscrete(min = 1, max = 32)
        public final int maxRange = 15;

        @Comment("Maximum amount of Blocks to be placed in one Tick.\",\n" +
                "                            \"Notice that an EffectBlock takes 20 ticks to place, therefore a Server has to handle 20-times this value effect-block Tile's at once. \" +\n" +
                "                            \"Reduce this if  you notice lag-spikes from Players placing Templates.\",\n" +
                "                            \"Of course decreasing this value will result in more time required to place large TemplateItem's.")

        @ConfigEntry.BoundedDiscrete(min = 1, max = Integer.MAX_VALUE)
        public final int placeSteps = 1024;

        @ConfigEntry.Gui.CollapsibleObject
        @Comment("Energy Cost & Durability of the Building Gadget")
        public final GadgetConfig GADGET_BUILDING = new GadgetConfig("Building Gadget", 500000, 50, 10);

        @ConfigEntry.Gui.CollapsibleObject
        @Comment("Energy Cost & Durability of the Exchanging Gadget")
        public final GadgetConfig GADGET_EXCHANGER = new GadgetConfig("Exchanging Gadget", 500000, 100, 10);

        @ConfigEntry.Gui.CollapsibleObject
        @Comment("Energy Cost, Durability & Maximum Energy of the Destruction Gadget")
        public final CategoryGadgetDestruction GADGET_DESTRUCTION = new CategoryGadgetDestruction();

        @Comment("Energy Cost & Durability of the Copy-Paste Gadget")
        @ConfigEntry.Gui.CollapsibleObject
        public final CategoryGadgetCopyPaste GADGET_COPY_PASTE = new CategoryGadgetCopyPaste();

        public static class GadgetConfig {

            @Comment("The max energy of the Gadget, set to 0 to disable energy usage")
            @ConfigEntry.BoundedDiscrete(min = 0, max = Integer.MAX_VALUE)
            public final long maxEnergy;
            //Maximum Energy

            @Comment("The Gadget's Energy cost per Operation")
            @ConfigEntry.BoundedDiscrete(min = 0, max = Integer.MAX_VALUE)
            public final long energyCost;
            //Energy Cost

            @Comment("The Gadget's Max Undo size (Note, the exchanger does not support undo)")
            @ConfigEntry.BoundedDiscrete(min = 0, max = 128)
            public final long undoSize;
            //Max Undo History Size

            public GadgetConfig(String name, int maxEnergy, int energyCost, int getMaxUndo) {
                this.maxEnergy = maxEnergy;
                this.energyCost = energyCost;
                this.undoSize = getMaxUndo;
            }
        }

        public static final class CategoryGadgetDestruction extends GadgetConfig {

            @Comment("The maximum dimensions, the Destruction Gadget can destroy.")
            @ConfigEntry.BoundedDiscrete(min = 0, max = 32)
            public final int destroySize = 16;
            //Destroy Dimensions

            @Comment("The cost in energy/durability will increase by this amount when not in fuzzy mode")
            @ConfigEntry.BoundedDiscrete(min = 0, max = Long.MAX_VALUE)
            public final long nonFuzzyMultiplier = 2;
            //Non-Fuzzy Mode Multiplier

            @Comment("If enabled, the Destruction Gadget can be taken out of fuzzy mode, allowing only instances of the block "
                    + "clicked to be removed (at a higher cost)")
            public final boolean nonFuzzyEnabled = false;
            //Non-Fuzzy Mode Enabled

            private CategoryGadgetDestruction() {
                super("Destruction Gadget", 1000000, 200, 1);
            }
        }

        public static final class CategoryGadgetCopyPaste extends GadgetConfig {

            @Comment("Maximum amount of Blocks to be copied in one Tick. \",\n" +
                    "                                \"Lower values may improve Server-Performance when copying large Templates")
            @ConfigEntry.BoundedDiscrete(min = 1, max = Integer.MAX_VALUE)
            public final int copySteps = 32768;
            //Max Copy/Tick

            @Comment("Maximum dimensions (x, y and z) that can be copied by a Template without requiring special permission.\",\n" +
                    "                                \"Permission can be granted using the '/buildinggadgets OverrideCopySize [<Player>]' command.")
            @ConfigEntry.BoundedDiscrete(min = -1, max = Integer.MAX_VALUE)
            public final int maxCopySize = 256;
            //Max Copy Dimensions

            @Comment("Maximum dimensions (x, y and z) that can be build by a Template without requiring special permission.\",\n" +
                    "                                \"Permission can be granted using the '/buildinggadgets OverrideBuildSize [<Player>]' command.")
            @ConfigEntry.BoundedDiscrete(min = -1, max = Integer.MAX_VALUE)
            public final int maxBuildSize = 256;
            //Max Build Dimensions

            private CategoryGadgetCopyPaste() {
                super("Copy-Paste Gadget", 500000, 50, 1);
            }
        }
    }

}
