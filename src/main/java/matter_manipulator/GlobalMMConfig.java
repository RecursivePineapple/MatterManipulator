package matter_manipulator;

import net.minecraftforge.common.config.Config;

public class GlobalMMConfig {

    @Config(modid = Tags.MODID, category = "interaction")
    public static class InteractionConfig {

        @Config.Comment("Clear the paste region when the copy or cut regions are marked")
        @Config.Name("Auto Clear Paste")
        public static boolean pasteAutoClear = true;

        @Config.Comment("Clear the transform and the stacking amount when the coordinates are cleared")
        @Config.Name("Clear Transform")
        public static boolean resetTransform = true;
    }

    @Config(modid = Tags.MODID, category = "rendering")
    public static class RenderingConfig {

        @Config.Comment("Controls how many blocks are shown in the preview. Client only.")
        @Config.Name("Max Hints Shown")
        public static int maxHints = 1_000_000;

        @Config.Comment("Controls the duration of the build status warning/error hints (seconds). Client only. Set to 0 to never clear hints.")
        @Config.Name("Build Status Timeout")
        public static int statusExpiration = 60;
    }

    @Config(modid = Tags.MODID, category = "debug")
    public static class DebugConfig {

        @Config.Comment("The maximum number of nanoseconds that the cooperative scheduler will run for each tick.")
        @Config.Name("Scheduler Max Duration (ns)")
        public static int schedulerDuration = 10_000_000; // 10 ms

        @Config.Comment("The maximum number of tasks that the scheduler will try to run per tick (not a hard limit).")
        @Config.Name("Scheduler Target Task Count")
        public static int maxTaskCount = 5;

        @Config.Comment("0 = No Profiling. 1 = Print the time taken by the scheduler. 2 = Print the time taken by each task.")
        @Config.RangeInt(min = 0, max = 2)
        @Config.Name("Scheduler Profiling")
        public static int schedulerProfileLevel = 0;
    }

    @Config(modid = Tags.MODID, category = "building")
    public static class BuildingConfig {

        @Config.Comment("High values may cause world desync, lag, and general server instability. Server only. Requires restart.")
        @Config.RangeInt(min = 1)
        @Config.Name("MK3 Block Place Speed")
        @Config.RequiresMcRestart
        public static int mk3BlocksPerPlace = 256;
    }

    public static boolean DEVENV = false;

    public static void init() {
        try {
            Class.forName("net.minecraft.server.MinecraftServer");
            DEVENV = true;
        } catch (ClassNotFoundException e) {
            // ignored
        }
    }
}
