package matter_manipulator.common.building;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

import matter_manipulator.common.building.BlockAnalyzer.IBlockApplyContext;
import matter_manipulator.common.items.manipulator.Transform;

public interface ITileAnalysisIntegration {

    boolean apply(IBlockApplyContext ctx);

    boolean getRequiredItemsForExistingBlock(IBlockApplyContext context);

    boolean getRequiredItemsForNewBlock(IBlockApplyContext context);

    void getItemTag(NBTTagCompound tag);

    void getItemDetails(List<String> details);

    void transform(Transform transform);

    ITileAnalysisIntegration clone();

    void migrate();
}
