package com.recursive_pineapple.matter_manipulator;

import com.recursive_pineapple.matter_manipulator.common.entities.EntityItemLarge;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator;

import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        EntityItemLarge.registerClient();
        ItemMatterManipulator.initKeybindings();
    }
}
