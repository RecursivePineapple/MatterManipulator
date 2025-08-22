package com.recursive_pineapple.matter_manipulator;

import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

import com.recursive_pineapple.matter_manipulator.common.compat.BlockPropertyRegistry;
import com.recursive_pineapple.matter_manipulator.common.entities.EntityItemLarge;
import com.recursive_pineapple.matter_manipulator.common.items.MMItems;
import com.recursive_pineapple.matter_manipulator.common.items.RecipeInstallUpgrade;
import com.recursive_pineapple.matter_manipulator.common.networking.Messages;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;
import com.recursive_pineapple.matter_manipulator.server.BlockStateCommand;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        MMMod.LOG.info("Loading Matter Manipulator version " + Tags.VERSION);

        GlobalMMConfig.init();
        MMItems.registerItems();
        Messages.init();
    }

    public void init(FMLInitializationEvent event) {
        EntityItemLarge.registerCommon();
        if (Mods.AppliedEnergistics2.isModLoaded() && Mods.GregTechNH.isModLoaded()) {
            MMItems.registerMultis();
        }

        RecipeInstallUpgrade.register();
    }

    public void postInit(FMLPostInitializationEvent event) {
        BlockPropertyRegistry.init();
    }

    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new BlockStateCommand());
    }

    public EntityPlayer getThePlayer() {
        return null;
    }
}
