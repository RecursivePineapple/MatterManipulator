package com.recursive_pineapple.matter_manipulator.common.items;

import gregtech.api.enums.MetaTileEntityIDs;

import com.recursive_pineapple.matter_manipulator.asm.Optional;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator.ManipulatorTier;
import com.recursive_pineapple.matter_manipulator.common.uplink.MTEMMUplink;
import com.recursive_pineapple.matter_manipulator.common.uplink.MTEMMUplinkMEHatch;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods.Names;

public class MMItems {

    private MMItems() {}

    public static MetaItem META_ITEM;

    public static void registerItems() {
        MMItemList.MK0.set(new ItemMatterManipulator(ManipulatorTier.Tier0));
        MMItemList.MK1.set(new ItemMatterManipulator(ManipulatorTier.Tier1));
        MMItemList.MK2.set(new ItemMatterManipulator(ManipulatorTier.Tier2));
        MMItemList.MK3.set(new ItemMatterManipulator(ManipulatorTier.Tier3));

        META_ITEM = new MetaItem("metaitem");
    }

    @Optional({
        Names.GREG_TECH_NH, Names.APPLIED_ENERGISTICS2
    })
    public static void registerMultis() {
        MMItemList.UplinkController.set(
            new MTEMMUplink(
                MetaTileEntityIDs.MATTER_MANIPULATOR_UPLINK.ID,
                "multimachine.mmuplink",
                "Matter Manipulator Quantum Uplink"
            ).getStackForm(1)
        );
        MMItemList.UplinkHatch.set(
            new MTEMMUplinkMEHatch(
                MetaTileEntityIDs.HATCH_MATTER_MANIPULATOR_UPLINK_ME.ID,
                "hatch.mmuplink.me",
                "Quantum Uplink ME Connector Hatch"
            ).getStackForm(1)
        );
    }
}
