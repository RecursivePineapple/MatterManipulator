package com.recursive_pineapple.matter_manipulator;

import com.recursive_pineapple.matter_manipulator.common.entities.EntityItemLarge;
import com.recursive_pineapple.matter_manipulator.common.items.MMItems;
import com.recursive_pineapple.matter_manipulator.common.networking.Messages;
import com.recursive_pineapple.matter_manipulator.common.recipes.ManipulatorRecipes;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import gregtech.api.GregTechAPI;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        MMMod.LOG.info("Loading Matter Manipulator version " + Tags.VERSION);

        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());

        MMItems.registerItems();
        Messages.init();

        if (Mods.GregTech.isModLoaded()) {
            GregTechAPI.sThirdPartyMultiRegistration.add(MMItems::registerMultis);
        }
    }

    public void init(FMLInitializationEvent event) {
        EntityItemLarge.registerCommon();
    }

    public void postInit(FMLPostInitializationEvent event) {
        ManipulatorRecipes.addRecipes();
    }

    public void serverStarting(FMLServerStartingEvent event) {}
}
