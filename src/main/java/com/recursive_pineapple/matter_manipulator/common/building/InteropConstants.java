package com.recursive_pineapple.matter_manipulator.common.building;

import net.minecraft.block.Block;

import net.minecraftforge.oredict.OreDictionary;

import gregtech.api.GregTechAPI;

import com.recursive_pineapple.matter_manipulator.asm.Optional;
import com.recursive_pineapple.matter_manipulator.common.utils.LazyBlock;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods.Names;

import bartworks.common.loaders.FluidLoader;
import goodgenerator.loader.Loaders;
import tectech.thing.casing.TTCasingsContainer;

/**
 * Various constants or static methods used for interop.
 */
public class InteropConstants {

    private InteropConstants() {}

    private static final LazyBlock BRIGHT_AIR = new LazyBlock(Mods.GalacticraftCore, "tile.brightAir", OreDictionary.WILDCARD_VALUE);

    public static boolean shouldBeSkipped(Block block, int meta) {
        if (Mods.GregTech.isModLoaded() && shouldBeSkippedGT(block)) return true;
        if (BRIGHT_AIR.matches(block, meta)) return true;

        return false;
    }

    @Optional(Names.GREG_TECH)
    private static boolean shouldBeSkippedGT(Block block) {
        if (block == GregTechAPI.sDroneRender) return true;
        if (block == GregTechAPI.sWormholeRender) return true;
        if (block == GregTechAPI.sBlackholeRender) return true;
        if (block == TTCasingsContainer.eyeOfHarmonyRenderBlock) return true;
        if (block == TTCasingsContainer.forgeOfGodsRenderBlock) return true;
        if (block == FluidLoader.bioFluidBlock) return true;
        if (block == Loaders.antimatterRenderBlock) return true;

        return false;
    }
}
