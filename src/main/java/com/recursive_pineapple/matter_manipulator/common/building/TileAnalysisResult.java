package com.recursive_pineapple.matter_manipulator.common.building;

import java.util.ArrayList;
import java.util.List;

import com.recursive_pineapple.matter_manipulator.common.building.BlockAnalyzer.IBlockApplyContext;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.Transform;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;

import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Stores all data needed to reconstruct a tile entity
 */
public class TileAnalysisResult {

    public ITileAnalysisIntegration gt, ae, arch;

    public InventoryAnalysis mInventory = null;

    public static final ForgeDirection[] ALL_DIRECTIONS = ForgeDirection.values();

    public TileAnalysisResult() {

    }

    public static TileAnalysisResult analyze(TileEntity te) {
        TileAnalysisResult result = new TileAnalysisResult();

        if (Mods.GregTech.isModLoaded()) {
            result.gt = GTAnalysisResult.analyze(te);
        }

        if (Mods.AppliedEnergistics2.isModLoaded()) {
            result.ae = AEAnalysisResult.analyze(te);
        }

        if (Mods.ArchitectureCraft.isModLoaded()) {
            result.arch = ArchitectureCraftAnalysisResult.analyze(te);
        }

        if (te instanceof IInventory inventory) {
            result.mInventory = InventoryAnalysis.fromInventory(inventory, false);
        }

        return result.doesAnything() ? result : null;
    }

    private static final TileAnalysisResult NO_OP = new TileAnalysisResult();

    public boolean doesAnything() {
        return !this.equals(NO_OP);
    }

    public boolean apply(IBlockApplyContext ctx) {
        TileEntity te = ctx.getTileEntity();

        for (var analysis : getIntegrations()) {
            if (!analysis.apply(ctx)) return false;
        }

        // update the inventory
        if (te instanceof IInventory inventory && mInventory != null) {
            if (!mInventory.apply(ctx, inventory, true, false)) {
                return false;
            }
        }

        return true;
    }

    private List<ITileAnalysisIntegration> getIntegrations() {
        List<ITileAnalysisIntegration> list = new ArrayList<>();

        if (gt != null) list.add(gt);
        if (ae != null) list.add(ae);
        if (arch != null) list.add(arch);

        return list;
    }

    /**
     * Get the required items for a block that exists in the world
     * 
     * @return True when this result can be applied to the tile, false otherwise
     */
    public boolean getRequiredItemsForExistingBlock(IBlockApplyContext context) {
        TileEntity te = context.getTileEntity();

        for (var analysis : getIntegrations()) {
            if (!analysis.getRequiredItemsForExistingBlock(context)) return false;
        }

        if (mInventory != null && te instanceof IInventory inventory) {
            mInventory.apply(context, inventory, true, true);
        }

        return true;
    }

    /**
     * Get the required items for a block that doesn't exist
     * 
     * @return True if this tile result is valid, false otherwise
     */
    public boolean getRequiredItemsForNewBlock(IBlockApplyContext context) {
        for (var analysis : getIntegrations()) {
            if (!analysis.getRequiredItemsForNewBlock(context)) return false;
        }

        if (mInventory != null) {
            for (IItemProvider item : mInventory.mItems) {
                if (item != null) {
                    item.getStack(context, true);
                }
            }
        }

        return true;
    }

    public NBTTagCompound getItemTag() {
        NBTTagCompound tag = new NBTTagCompound();
        
        for (var analysis : getIntegrations()) {
            analysis.getItemTag(tag);
        }

        return tag.hasNoTags() ? null : tag;
    }

    public String getItemDetails() {
        List<String> details = new ArrayList<>(0);

        for (var analysis : getIntegrations()) {
            analysis.getItemDetails(details);
        }

        return details.isEmpty() ? "" : String.format(" (%s)", String.join(", ", details));
    }

    public void transform(Transform transform) {
        for (var analysis : getIntegrations()) {
            analysis.transform(transform);
        }
    }

    public TileAnalysisResult clone() {
        TileAnalysisResult dup = new TileAnalysisResult();

        if (gt != null) dup.gt = gt.clone();
        if (ae != null) dup.ae = ae.clone();
        if (arch != null) dup.arch = arch.clone();
        if (mInventory != null) dup.mInventory = mInventory.clone();

        return dup;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((gt == null) ? 0 : gt.hashCode());
        result = prime * result + ((ae == null) ? 0 : ae.hashCode());
        result = prime * result + ((arch == null) ? 0 : arch.hashCode());
        result = prime * result + ((mInventory == null) ? 0 : mInventory.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TileAnalysisResult other = (TileAnalysisResult) obj;
        if (gt == null) {
            if (other.gt != null) return false;
        } else if (!gt.equals(other.gt)) return false;
        if (ae == null) {
            if (other.ae != null) return false;
        } else if (!ae.equals(other.ae)) return false;
        if (arch == null) {
            if (other.arch != null) return false;
        } else if (!arch.equals(other.arch)) return false;
        if (mInventory == null) {
            if (other.mInventory != null) return false;
        } else if (!mInventory.equals(other.mInventory)) return false;
        return true;
    }
}
