package com.recursive_pineapple.matter_manipulator.common.building;

import java.util.List;

import com.recursive_pineapple.matter_manipulator.common.building.BlockAnalyzer.IBlockAnalysisContext;
import com.recursive_pineapple.matter_manipulator.common.building.BlockAnalyzer.IBlockApplyContext;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.Transform;

import gcewing.architecture.ArchitectureCraft;
import gcewing.architecture.common.shape.Shape;
import gcewing.architecture.common.tile.TileShape;
import gcewing.architecture.compat.BlockCompatUtils;
import gcewing.architecture.compat.IBlockState;
import gcewing.architecture.compat.MetaBlockState;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class ArchitectureCraftAnalysisResult implements ITileAnalysisIntegration {

    public int shape;
    public byte side, turn;
    public PortableItemStack material, cladding;

    public static ArchitectureCraftAnalysisResult analyze(IBlockAnalysisContext context, TileEntity te) {
        if (!(te instanceof TileShape tileShape)) return null;

        ArchitectureCraftAnalysisResult result = new ArchitectureCraftAnalysisResult();

        result.shape = tileShape.shape.id;
        result.side = tileShape.side;
        result.turn = tileShape.turn;

        result.material = new PortableItemStack(new ItemStack(tileShape.baseBlockState.getBlock(), 0, BlockCompatUtils.getMetaFromBlockState(tileShape.baseBlockState)));

        if (tileShape.secondaryBlockState != null) {
            result.cladding = new PortableItemStack(new ItemStack(tileShape.secondaryBlockState.getBlock(), 0, BlockCompatUtils.getMetaFromBlockState(tileShape.secondaryBlockState)));
        }

        return result;
    }

    @Override
    public boolean apply(IBlockApplyContext ctx) {
        TileEntity te = ctx.getTileEntity();

        if (te instanceof TileShape tileShape) {
            tileShape.setSide(side);
            tileShape.setTurn(turn);

            if (tileShape.secondaryBlockState != null) {
                removeCladding(ctx, tileShape, false);
            }

            if (cladding != null) {
                if (!addCladding(ctx, tileShape, false)) {
                    return false;
                }
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
            if (!addCladding(context, tileShape, true)) {
                return false;
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
}
