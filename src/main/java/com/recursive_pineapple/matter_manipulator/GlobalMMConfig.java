package com.recursive_pineapple.matter_manipulator;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods.Names;

public class GlobalMMConfig {

    @Config(modid = Names.MATTER_MANIPULATOR, category = "Interaction")
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

    @Config(modid = Names.MATTER_MANIPULATOR, category = "Rendering")
    public static class RenderingConfig {

        @Config.Comment("Controls how many blocks are shown in the preview (client only)")
        @Config.DefaultInt(5_000)
        @Config.Name("Max Hints Shown")
        public static int maxHints;
    }

    public static boolean DEVENV = false, D1 = false;

    public static void init() {
        try {
            ConfigurationManager.registerConfig(InteractionConfig.class);
            ConfigurationManager.registerConfig(RenderingConfig.class);
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
