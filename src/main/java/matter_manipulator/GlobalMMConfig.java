package matter_manipulator;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import matter_manipulator.common.utils.Mods.Names;

public class GlobalMMConfig {

    @Config(modid = Names.MATTER_MANIPULATOR, category = "interaction")
    public static class InteractionConfig {

        @Config.Comment("Clear the paste region when the copy or cut regions are marked")
        @Config.DefaultBoolean(true)
        @Config.Name("Auto Clear Paste")
        public static boolean pasteAutoClear;

        @Config.Comment("Clear the transform and the stacking amount when the coordinates are cleared")
        @Config.DefaultBoolean(true)
        @Config.Name("Clear Transform")
        public static boolean resetTransform;
    }

    @Config(modid = Names.MATTER_MANIPULATOR, category = "rendering")
    public static class RenderingConfig {

        @Config.Comment("Controls how many blocks are shown in the preview. Client only.")
        @Config.DefaultInt(1_000_000)
        @Config.Name("Max Hints Shown")
        public static int maxHints;

        @Config.Comment("Controls the duration of the build status warning/error hints (seconds). Client only. Set to 0 to never clear hints.")
        @Config.DefaultInt(60)
        @Config.Name("Build Status Timeout")
        public static int statusExpiration;

        @Config.Comment("When true, hints will always be drawn on top of the terrain. Client only.")
        @Config.DefaultBoolean(true)
        @Config.Name("Draw Hints On Top")
        public static boolean hintsOnTop;
    }

    @Config(modid = Names.MATTER_MANIPULATOR, category = "debug")
    public static class DebugConfig {

        @Config.DefaultBoolean(false)
        @Config.Name("Enable Debug Logging")
        public static boolean debug;
    }

    @Config(modid = Names.MATTER_MANIPULATOR, category = "building")
    public static class BuildingConfig {

        @Config.Comment("Empty ME Output Hatches/Busses when they're removed. Server only.")
        @Config.DefaultBoolean(true)
        @Config.Name("Empty ME Outputs")
        public static boolean meEmptying;

        @Config.Comment("High values may cause world desync and lag. Server only. Requires restart.")
        @Config.DefaultInt(256)
        @Config.RangeInt(min = 1)
        @Config.Name("MK3 Block Place Speed")
        @Config.RequiresMcRestart
        public static int mk3BlocksPerPlace;
    }

    public static boolean DEVENV = false;

    public static void init() {
        try {
            ConfigurationManager.registerConfig(InteractionConfig.class);
            ConfigurationManager.registerConfig(RenderingConfig.class);
            ConfigurationManager.registerConfig(DebugConfig.class);
            ConfigurationManager.registerConfig(BuildingConfig.class);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }

        try {
            Class.forName("net.minecraft.server.MinecraftServer");
            DEVENV = true;
        } catch (ClassNotFoundException e) {
            // ignored
        }
    }
}
