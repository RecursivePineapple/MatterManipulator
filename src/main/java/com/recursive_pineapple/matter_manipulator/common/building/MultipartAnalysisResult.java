package com.recursive_pineapple.matter_manipulator.common.building;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.recursive_pineapple.matter_manipulator.common.building.BlockAnalyzer.IBlockApplyContext;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.Transform;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;

import codechicken.lib.vec.BlockCoord;
import codechicken.microblock.Microblock;
import codechicken.multipart.TileMultipart;

public class MultipartAnalysisResult implements ITileAnalysisIntegration {

    public static MultipartAnalysisResult analyze(TileEntity tile) {
        if (!(tile instanceof TileMultipart multipart)) return null;

        var partList = multipart.jPartList();

        Microblock m = (Microblock) partList.get(0);

        return null;
    }

    @Override
    public boolean apply(IBlockApplyContext ctx) {
        World world = ctx.getWorld();
        int x = ctx.getX();
        int y = ctx.getY();
        int z = ctx.getZ();

        if (!MMUtils.FMP_BLOCK.matches(world.getBlock(x, y, z), 0)) return false;

        BlockCoord coord = new BlockCoord(x, y, z);

        TileMultipart tile = TileMultipart.getOrConvertTile(ctx.getWorld(), coord);

        var partList = tile.jPartList();

        return true;
    }

    @Override
    public boolean getRequiredItemsForExistingBlock(IBlockApplyContext context) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRequiredItemsForExistingBlock'");
    }

    @Override
    public boolean getRequiredItemsForNewBlock(IBlockApplyContext context) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRequiredItemsForNewBlock'");
    }

    @Override
    public void getItemTag(NBTTagCompound tag) {

    }

    @Override
    public void getItemDetails(List<String> details) {

    }

    @Override
    public void transform(Transform transform) {

    }

    @Override
    public MultipartAnalysisResult clone() {
        MultipartAnalysisResult dup = new MultipartAnalysisResult();

        return dup;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MultipartAnalysisResult;
    }
}
