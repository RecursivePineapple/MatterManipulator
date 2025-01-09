package com.recursive_pineapple.matter_manipulator.common.building;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

import com.recursive_pineapple.matter_manipulator.common.building.BlockAnalyzer.IBlockApplyContext;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.Transform;

public interface ITileAnalysisIntegration {

    public boolean apply(IBlockApplyContext ctx);

    public boolean getRequiredItemsForExistingBlock(IBlockApplyContext context);

    public boolean getRequiredItemsForNewBlock(IBlockApplyContext context);

    public void getItemTag(NBTTagCompound tag);

    public void getItemDetails(List<String> details);

    public void transform(Transform transform);

    public ITileAnalysisIntegration clone();
}
