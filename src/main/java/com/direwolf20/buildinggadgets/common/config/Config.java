package com.direwolf20.buildinggadgets.common.config;

import dev.architectury.utils.value.IntValue;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

public class Config implements ConfigData{

    @ConfigEntry.Category("General")
    @Comment("General mod settings")
    public static final CategoryGeneral GENERAL = new CategoryGeneral();
    @ConfigEntry.Category("Gadgets")
    @Comment("Configure the Gadgets")
    public static final CategoryGadgets GADGETS = new CategoryGadgets();

    public static final CategoryPasteContainers PASTE_CONTAINERS = new CategoryPasteContainers();

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
        public final CategoryGadgetCopyPaste GADGET_COPY_PASTE = new CategoryGadgetCopyPaste();;

        public static class GadgetConfig {

            @Comment("The max energy of the Gadget, set to 0 to disable energy usage")
            @ConfigEntry.BoundedDiscrete(min = 0, max = Integer.MAX_VALUE)
            public final IntValue maxEnergy;
            //Maximum Energy

            @Comment("The Gadget's Energy cost per Operation")
            @ConfigEntry.BoundedDiscrete(min = 0, max = Integer.MAX_VALUE)
            public final IntValue energyCost;
            //Energy Cost

            @Comment("The Gadget's Max Undo size (Note, the exchanger does not support undo)")
            @ConfigEntry.BoundedDiscrete(min = 0, max = 128)
            public final IntValue undoSize;
            //Max Undo History Size

            public GadgetConfig(String name, int maxEnergy, int energyCost, int getMaxUndo) {
                this.maxEnergy = new IntValue() {
                    @Override
                    public void accept(int value) {}

                    @Override
                    public int getAsInt() {
                        return maxEnergy;
                    }
                };
                this.energyCost = new IntValue() {
                    @Override
                    public void accept(int value) {}

                    @Override
                    public int getAsInt() {
                        return energyCost;
                    }
                };
                this.undoSize = new IntValue() {
                    @Override
                    public void accept(int value) {}

                    @Override
                    public int getAsInt() {
                        return getMaxUndo;
                    }
                };
            }
        }

        public static final class CategoryGadgetDestruction extends GadgetConfig {
            public final IntValue destroySize;
            public final DoubleValue nonFuzzyMultiplier;
            public final BooleanValue nonFuzzyEnabled;

            private CategoryGadgetDestruction() {
                super("Destruction Gadget", 1000000, 200, 1);

                SERVER_BUILDER
                        .comment("Energy Cost, Durability & Maximum Energy of the Destruction Gadget")
                        .push("Destruction Gadget");


                destroySize = SERVER_BUILDER
                        .comment("The maximum dimensions, the Destruction Gadget can destroy.")
                        .defineInRange("Destroy Dimensions", 16, 0, 32);

                nonFuzzyMultiplier = SERVER_BUILDER
                        .comment("The cost in energy/durability will increase by this amount when not in fuzzy mode")
                        .defineInRange("Non-Fuzzy Mode Multiplier", 2, 0, Double.MAX_VALUE);

                nonFuzzyEnabled = SERVER_BUILDER
                        .comment("If enabled, the Destruction Gadget can be taken out of fuzzy mode, allowing only instances of the block "
                                + "clicked to be removed (at a higher cost)")
                        .define("Non-Fuzzy Mode Enabled", false);

                SERVER_BUILDER.pop();

            }
        }

        public static final class CategoryGadgetCopyPaste extends GadgetConfig {
            public final int copySteps;
            public final int maxCopySize;
            public final int maxBuildSize;

            private CategoryGadgetCopyPaste() {
                super("Copy-Paste Gadget", 500000, 50, 1);

                SERVER_BUILDER
                        .comment("Energy Cost & Durability of the Copy-Paste Gadget")
                        .push("Copy-Paste Gadget");

                //use the old cap as the per tick border... This implies that 32*32*32 areas are the max size for a one tick copy by default
                copySteps = SERVER_BUILDER
                        .comment("Maximum amount of Blocks to be copied in one Tick. ",
                                "Lower values may improve Server-Performance when copying large Templates")
                        .defineInRange("Max Copy/Tick", 32768, 1, Integer.MAX_VALUE);

                maxCopySize = SERVER_BUILDER
                        .comment("Maximum dimensions (x, y and z) that can be copied by a Template without requiring special permission.",
                                "Permission can be granted using the '/buildinggadgets OverrideCopySize [<Player>]' command.")
                        .defineInRange("Max Copy Dimensions", 256, - 1, Integer.MAX_VALUE);

                maxBuildSize = SERVER_BUILDER
                        .comment("Maximum dimensions (x, y and z) that can be build by a Template without requiring special permission.",
                                "Permission can be granted using the '/buildinggadgets OverrideBuildSize [<Player>]' command.")
                        .defineInRange("Max Build Dimensions", 256, - 1, Integer.MAX_VALUE);

                SERVER_BUILDER.pop();
            }
        }
    }

    public static final class CategoryPasteContainers {

        public final int capacityT1, capacityT2, capacityT3;

        private CategoryPasteContainers() {
            SERVER_BUILDER
                    .comment("Configure the Paste Containers")
                    .push("Paste Containers");

            capacityT1 = getMaxCapacity(1);
            capacityT2 = getMaxCapacity(2);
            capacityT3 = getMaxCapacity(3);
        }

        private static int getMaxCapacity(int tier) {
            return SERVER_BUILDER
                    .comment(String.format("The maximum capacity of a tier %s (iron) Construction Paste Container", tier))
                    .defineInRange(String.format("T%s Container Capacity", tier), (int) (512 * Math.pow(4, tier - 1)), 1, Integer.MAX_VALUE);
        }
    }
}
