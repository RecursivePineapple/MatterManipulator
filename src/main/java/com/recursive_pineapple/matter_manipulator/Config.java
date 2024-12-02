package com.recursive_pineapple.matter_manipulator;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static boolean DEVENV = false, D1 = false;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        if (configuration.hasChanged()) {
            configuration.save();
        }

        try {
            Class.forName("net.minecraft.server.MinecraftServer");
            DEVENV = true;
        } catch (ClassNotFoundException e) {
            // ignored
        }
    }
}
