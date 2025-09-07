package com.recursive_pineapple.matter_manipulator.common.building;

import net.minecraft.block.Block;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.material.MaterialTransparent;
import net.minecraft.init.Blocks;

import net.minecraftforge.oredict.OreDictionary;

import gregtech.api.GregTechAPI;

import com.gtnewhorizon.gtnhlib.util.data.LazyBlock;
import com.recursive_pineapple.matter_manipulator.asm.Optional;
import com.recursive_pineapple.matter_manipulator.common.utils.LazyBlockSpec;
import com.recursive_pineapple.matter_manipulator.common.utils.MMValues;
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

    public static final LazyBlock BRIGHT_AIR = new LazyBlock(Mods.GalacticraftCore, "tile.brightAir", OreDictionary.WILDCARD_VALUE);
    public static final LazyBlock ARCANE_LAMP_LIGHT = new LazyBlock(Mods.Thaumcraft, "blockAiry", 2);
    public static final LazyBlock WIRELESS_CONNECTOR = new LazyBlock(Mods.AE2Stuff, "Wireless");
    public static final LazyBlockSpec AE_BLOCK_CABLE = LazyBlockSpec.ofBlock(Mods.AppliedEnergistics2, "tile.BlockCableBus", 0);
    public static final LazyBlock FMP_BLOCK = new LazyBlock(Mods.ForgeMultipart, "block");
    public static final LazyBlockSpec ENDER_STORAGE = LazyBlockSpec.ofBlock(Mods.EnderStorage, "enderChest", MMValues.W);

    public static boolean isAir(Block block, int meta) {
        if (block.getMaterial() instanceof MaterialTransparent) return true;

        return false;
    }

    public static boolean skipWhenCopying(Block block, int meta) {
        if (block.getMaterial() instanceof MaterialLiquid) return true;

        if (Mods.GregTech.isModLoaded() && isGTRenderer(block)) return true;
        if (FMP_BLOCK.matches(block, meta)) return true;
        if (BRIGHT_AIR.matches(block, meta)) return true;
        if (ARCANE_LAMP_LIGHT.matches(block, meta)) return true;

        return false;
    }

    public static boolean shouldDropItem(Block block, int meta) {
        if (Mods.GregTech.isModLoaded() && isGTRenderer(block)) return false;

        // Don't check for MaterialTransparent because it could include things like nitor
        if (BRIGHT_AIR.matches(block, meta)) return false;
        if (ARCANE_LAMP_LIGHT.matches(block, meta)) return false;

        return true;
    }

    @Optional(Names.GREG_TECH_NH)
    private static boolean isGTRenderer(Block block) {
        if (block == GregTechAPI.sDroneRender) return true;
        if (block == GregTechAPI.sWormholeRender) return true;
        if (block == GregTechAPI.sBlackholeRender) return true;
        if (block == TTCasingsContainer.eyeOfHarmonyRenderBlock) return true;
        if (block == TTCasingsContainer.forgeOfGodsRenderBlock) return true;
        if (block == FluidLoader.bioFluidBlock) return true;
        if (block == Loaders.antimatterRenderBlock) return true;

        return false;
    }

    public static boolean isFree(Block block, int metadata) {
        if (block == Blocks.air) return true;

        if (FMP_BLOCK.matches(block, metadata)) return true;
        if (AE_BLOCK_CABLE.matches(block, metadata)) return true;

        return false;
    }
}
