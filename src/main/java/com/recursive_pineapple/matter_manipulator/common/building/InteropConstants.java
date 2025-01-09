package com.recursive_pineapple.matter_manipulator.common.building;

import net.minecraft.block.Block;

import gregtech.api.GregTechAPI;

import com.recursive_pineapple.matter_manipulator.asm.Optional;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods.Names;

import tectech.thing.casing.TTCasingsContainer;

/**
 * Various constants or static methods used for interop.
 */
public enum InteropConstants {
    ;

    public static boolean shouldBeSkipped(Block block, int meta) {
        if (Mods.GregTech.isModLoaded()) { return shouldBeSkippedGT(block, meta); }

        return false;
    }

    @Optional(Names.GREG_TECH)
    private static boolean shouldBeSkippedGT(Block block, int meta) {
        if (block == GregTechAPI.sDroneRender) return true;
        if (block == GregTechAPI.sWormholeRender) return true;
        if (block == GregTechAPI.sBlackholeRender) return true;
        if (block == TTCasingsContainer.eyeOfHarmonyRenderBlock) return true;
        if (block == TTCasingsContainer.forgeOfGodsRenderBlock) return true;

        return false;
    }
}
