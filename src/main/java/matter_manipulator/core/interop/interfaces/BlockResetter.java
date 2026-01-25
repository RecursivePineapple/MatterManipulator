package matter_manipulator.core.interop.interfaces;

import java.util.List;

import net.minecraft.util.math.BlockPos;

import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.resources.ResourceStack;

/// Something that resets a block before the block is removed. Every resetter is called for each block, no filtering is
/// done.
public interface BlockResetter {

    List<ResourceStack> resetBlock(ManipulatorContext context, BlockPos pos);
}
