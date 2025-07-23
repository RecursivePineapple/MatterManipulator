package com.recursive_pineapple.matter_manipulator.common.building;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import com.google.gson.annotations.SerializedName;
import com.recursive_pineapple.matter_manipulator.common.building.BlockAnalyzer.IBlockApplyContext;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.Transform;

import gcewing.architecture.ArchitectureCraft;
import gcewing.architecture.common.shape.Shape;
import gcewing.architecture.common.tile.TileShape;
import gcewing.architecture.compat.BlockCompatUtils;
import gcewing.architecture.compat.IBlockState;
import gcewing.architecture.compat.MetaBlockState;

public class ArchitectureCraftAnalysisResult implements ITileAnalysisIntegration {

    @SerializedName("s")
    public int shape;
    @SerializedName("m")
    public PortableItemStack material;
    @SerializedName("c")
    public PortableItemStack cladding;

    public static ArchitectureCraftAnalysisResult analyze(TileEntity te) {
        if (!(te instanceof TileShape tileShape)) return null;

        ArchitectureCraftAnalysisResult result = new ArchitectureCraftAnalysisResult();

        result.shape = tileShape.shape.id;

        result.material = new PortableItemStack(
            new ItemStack(tileShape.baseBlockState.getBlock(), 0, BlockCompatUtils.getMetaFromBlockState(tileShape.baseBlockState))
        );

        if (tileShape.secondaryBlockState != null) {
            result.cladding = new PortableItemStack(
                new ItemStack(tileShape.secondaryBlockState.getBlock(), 0, BlockCompatUtils.getMetaFromBlockState(tileShape.secondaryBlockState))
            );
        }

        return result;
    }

    @Override
    public boolean apply(IBlockApplyContext ctx) {
        TileEntity te = ctx.getTileEntity();

        if (te instanceof TileShape tileShape) {
            if (tileShape.secondaryBlockState != null) {
                removeCladding(ctx, tileShape, false);
            }

            if (cladding != null) {
                if (!addCladding(ctx, tileShape, false)) { return false; }
            }

            tileShape.markChanged();

            return true;
        }

        return false;
    }

    @Override
    public boolean getRequiredItemsForExistingBlock(IBlockApplyContext context) {
        TileEntity te = context.getTileEntity();

        if (te instanceof TileShape tileShape) {
            removeCladding(context, tileShape, true);
            if (cladding != null) {
                if (!addCladding(context, tileShape, true)) return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean getRequiredItemsForNewBlock(IBlockApplyContext context) {
        if (cladding != null) {
            addCladding(context, null, true);
        }

        return true;
    }

    private void removeCladding(IBlockApplyContext context, TileShape tileShape, boolean simulate) {
        IBlockState existingCladding = tileShape.secondaryBlockState;

        if (existingCladding == null) return;

        Block block = existingCladding.getBlock();
        int meta = BlockCompatUtils.getMetaFromBlockState(existingCladding);

        context.givePlayerItems(ArchitectureCraft.content.itemCladding.newStack(block, meta, 1));

        if (!simulate) {
            tileShape.secondaryBlockState = null;
            tileShape.markChanged();
        }
    }

    private boolean addCladding(IBlockApplyContext context, TileShape tileShape, boolean simulate) {
        Block block = cladding.getBlock();

        if (block == null) return false;

        ItemStack claddingStack = ArchitectureCraft.content.itemCladding.newStack(block, cladding.getMeta(), 1);

        if (!context.tryConsumeItems(claddingStack)) {
            if (!simulate) {
                context.warn("Could not find cladding: " + claddingStack.getDisplayName());
            }
            return false;
        }

        if (!simulate) {
            tileShape.secondaryBlockState = new MetaBlockState(block, cladding.getMeta());
            tileShape.markChanged();
        }

        return true;
    }

    @Override
    public void getItemTag(NBTTagCompound tag) {
        tag.setString("BaseName", material.item.toString());
        tag.setInteger("BaseData", material.getMeta());
        tag.setInteger("Shape", shape);
    }

    @Override
    public void getItemDetails(List<String> details) {
        Shape shape = Shape.forId(this.shape);

        if (shape != null) details.add(shape.title);

        ItemStack stack = material == null ? null : material.toStack();

        if (stack != null) details.add(stack.getDisplayName());
    }

    @Override
    public void transform(Transform transform) {

    }

    @Override
    public void migrate() {

    }

    @Override
    public ArchitectureCraftAnalysisResult clone() {
        ArchitectureCraftAnalysisResult dup = new ArchitectureCraftAnalysisResult();

        dup.shape = shape;
        dup.material = material == null ? null : material.clone();
        dup.cladding = cladding == null ? null : cladding.clone();

        return dup;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + shape;
        result = prime * result + ((material == null) ? 0 : material.hashCode());
        result = prime * result + ((cladding == null) ? 0 : cladding.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ArchitectureCraftAnalysisResult other = (ArchitectureCraftAnalysisResult) obj;
        if (shape != other.shape) return false;
        if (material == null) {
            if (other.material != null) return false;
        } else if (!material.equals(other.material)) return false;
        if (cladding == null) {
            if (other.cladding != null) return false;
        } else if (!cladding.equals(other.cladding)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "ArchitectureCraftAnalysisResult [shape=" + shape
            + ", material="
            + material
            + ", cladding="
            + cladding
            + "]";
    }
}
