package matter_manipulator.core.interop.interfaces;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import matter_manipulator.core.building.CopyableProperty;
import matter_manipulator.core.resources.ResourceStack;

public interface BlockAdapter {

    boolean canAdapt(IBlockState state);
    boolean canAdapt(ResourceStack resource);

    ResourceStack getResourceForm(IBlockState state);
    IBlockState getBlockForm(ResourceStack resource);

    default void place(World world, BlockPos pos, IBlockState state) {
        world.setBlockState(pos, CopyableProperty.sanitizeBlockState(state));
    }
}
