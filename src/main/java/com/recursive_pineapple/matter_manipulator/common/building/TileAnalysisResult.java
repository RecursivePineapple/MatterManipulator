package com.recursive_pineapple.matter_manipulator.common.building;

import static com.recursive_pineapple.matter_manipulator.common.utils.MMUtils.nullIfUnknown;

import java.util.ArrayList;
import java.util.List;

import com.recursive_pineapple.matter_manipulator.common.building.AnalysisHacks.RotationHacks;
import com.recursive_pineapple.matter_manipulator.common.building.BlockAnalyzer.IBlockAnalysisContext;
import com.recursive_pineapple.matter_manipulator.common.building.BlockAnalyzer.IBlockApplyContext;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.Transform;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;

import ic2.api.tile.IWrenchable;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Stores all data needed to reconstruct a tile entity
 */
public class TileAnalysisResult {

    // hopefully these field are self explanitory

    public ITileAnalysisIntegration gt, ae;

    public InventoryAnalysis mInventory = null;
    public ForgeDirection mDirection = null;

    public static final ForgeDirection[] ALL_DIRECTIONS = ForgeDirection.values();

    public TileAnalysisResult() {

    }

    public TileAnalysisResult(IBlockAnalysisContext context, TileEntity te) {

        if (Mods.GregTech.isModLoaded()) {
            gt = GTAnalysisResult.analyze(context, te);
        }

        if (Mods.AppliedEnergistics2.isModLoaded()) {
            ae = AEAnalysisResult.analyze(context, te);
        }

        // check its inventory
        if (te instanceof IInventory inventory) {
            mInventory = InventoryAnalysis.fromInventory(inventory, false);
        }

        if (te instanceof IWrenchable wrenchable) {
            mDirection = MMUtils.getIndexSafe(ForgeDirection.VALID_DIRECTIONS, wrenchable.getFacing());
        } else {
            mDirection = nullIfUnknown(RotationHacks.getRotation(te));
        }
    }

    private static final TileAnalysisResult NO_OP = new TileAnalysisResult();

    public boolean doesAnything() {
        return !this.equals(NO_OP);
    }

    public boolean apply(IBlockApplyContext ctx) {
        TileEntity te = ctx.getTileEntity();

        if (gt != null) {
            if (!gt.apply(ctx)) return false;
        }

        if (ae != null) {
            if (!ae.apply(ctx)) return false;
        }

        // update the inventory
        if (te instanceof IInventory inventory && mInventory != null) {
            if (!mInventory.apply(ctx, inventory, true, false)) {
                return false;
            }
        }

        if (mDirection != null) {
            if (te instanceof IWrenchable wrenchable) {
                wrenchable.setFacing((short) mDirection.ordinal());
            } else {
                RotationHacks.setRotation(te, mDirection);
            }
        }

        return true;
    }

    /**
     * Get the required items for a block that exists in the world
     * 
     * @return True when this result can be applied to the tile, false otherwise
     */
    public boolean getRequiredItemsForExistingBlock(IBlockApplyContext context) {
        TileEntity te = context.getTileEntity();

        if (gt != null) {
            if (!gt.getRequiredItemsForExistingBlock(context)) return false;
        }

        if (ae != null) {
            if (!ae.getRequiredItemsForExistingBlock(context)) return false;
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
        if (gt != null) {
            if (!gt.getRequiredItemsForNewBlock(context)) return false;
        }

        if (ae != null) {
            if (!ae.getRequiredItemsForNewBlock(context)) return false;
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
        
        if (gt != null) gt.getItemTag(tag);
        if (ae != null) ae.getItemTag(tag);

        return tag.hasNoTags() ? null : tag;
    }

    public String getItemDetails() {
        List<String> details = new ArrayList<>(0);

        if (gt != null) gt.getItemDetails(details);
        if (ae != null) ae.getItemDetails(details);

        return details.isEmpty() ? "" : String.format(" (%s)", String.join(", ", details));
    }

    public void transform(Transform transform) {
        if (gt != null) gt.transform(transform);
        if (ae != null) ae.transform(transform);

        mDirection = transform.apply(mDirection);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((gt == null) ? 0 : gt.hashCode());
        result = prime * result + ((ae == null) ? 0 : ae.hashCode());
        result = prime * result + ((mInventory == null) ? 0 : mInventory.hashCode());
        result = prime * result + ((mDirection == null) ? 0 : mDirection.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TileAnalysisResult other = (TileAnalysisResult) obj;
        if (gt == null) {
            if (other.gt != null)
                return false;
        } else if (!gt.equals(other.gt))
            return false;
        if (ae == null) {
            if (other.ae != null)
                return false;
        } else if (!ae.equals(other.ae))
            return false;
        if (mInventory == null) {
            if (other.mInventory != null)
                return false;
        } else if (!mInventory.equals(other.mInventory))
            return false;
        if (mDirection != other.mDirection)
            return false;
        return true;
    }

    
}
