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

        MMItemList.Hologram.set(META_ITEM, IDMetaItem.Hologram.ID);
        MMItemList.PowerCore0.set(META_ITEM, IDMetaItem.PowerCore0.ID);
        MMItemList.ComputerCore0.set(META_ITEM, IDMetaItem.ComputerCore0.ID);
        MMItemList.TeleporterCore0.set(META_ITEM, IDMetaItem.TeleporterCore0.ID);
        MMItemList.Frame0.set(META_ITEM, IDMetaItem.Frame0.ID);
        MMItemList.Lens0.set(META_ITEM, IDMetaItem.Lens0.ID);
        MMItemList.PowerCore1.set(META_ITEM, IDMetaItem.PowerCore1.ID);
        MMItemList.ComputerCore1.set(META_ITEM, IDMetaItem.ComputerCore1.ID);
        MMItemList.TeleporterCore1.set(META_ITEM, IDMetaItem.TeleporterCore1.ID);
        MMItemList.Frame1.set(META_ITEM, IDMetaItem.Frame1.ID);
        MMItemList.Lens1.set(META_ITEM, IDMetaItem.Lens1.ID);
        MMItemList.PowerCore2.set(META_ITEM, IDMetaItem.PowerCore2.ID);
        MMItemList.ComputerCore2.set(META_ITEM, IDMetaItem.ComputerCore2.ID);
        MMItemList.TeleporterCore2.set(META_ITEM, IDMetaItem.TeleporterCore2.ID);
        MMItemList.Frame2.set(META_ITEM, IDMetaItem.Frame2.ID);
        MMItemList.Lens2.set(META_ITEM, IDMetaItem.Lens2.ID);
        MMItemList.PowerCore3.set(META_ITEM, IDMetaItem.PowerCore3.ID);
        MMItemList.ComputerCore3.set(META_ITEM, IDMetaItem.ComputerCore3.ID);
        MMItemList.TeleporterCore3.set(META_ITEM, IDMetaItem.TeleporterCore3.ID);
        MMItemList.Frame3.set(META_ITEM, IDMetaItem.Frame3.ID);
        MMItemList.Lens3.set(META_ITEM, IDMetaItem.Lens3.ID);
        MMItemList.AEDownlink.set(META_ITEM, IDMetaItem.AEDownlink.ID);
        MMItemList.QuantumDownlink.set(META_ITEM, IDMetaItem.QuantumDownlink.ID);
    }

    @Optional({
        Names.GREG_TECH, Names.APPLIED_ENERGISTICS2
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
