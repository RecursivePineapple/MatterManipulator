package com.recursive_pineapple.matter_manipulator.common.items;

import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator.ManipulatorTier;

public class MMItemList {
    
    private MMItemList() {}

    public static MetaItem META_ITEM;
    public static ItemMatterManipulator MK0, MK1, MK2, MK3;

    public static void registerItems() {
        META_ITEM = new MetaItem("metaitem");

        MK0 = new ItemMatterManipulator(ManipulatorTier.Tier0);
        MK1 = new ItemMatterManipulator(ManipulatorTier.Tier1);
        MK2 = new ItemMatterManipulator(ManipulatorTier.Tier2);
        MK3 = new ItemMatterManipulator(ManipulatorTier.Tier3);
    }
}
