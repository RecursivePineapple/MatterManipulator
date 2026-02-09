package matter_manipulator.core.interop;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.resources.ResourceStack;

public interface BlockAdapter {

    boolean canAdapt(IBlockState state);
    boolean canAdapt(ResourceStack resource);

    ResourceStack getResourceForm(IBlockState state);
    IBlockState getBlockForm(ResourceStack resource);

    default ApplyResult place(World world, BlockPos pos, ResourceStack resource) {
        IBlockState toPlace = getBlockForm(resource);

        if (toPlace == world.getBlockState(pos)) return ApplyResult.DidNothing;

        world.setBlockState(pos, toPlace);

        return ApplyResult.DidSomething;
    }
}
