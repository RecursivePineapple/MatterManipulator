package matter_manipulator.core.interop;

import net.minecraft.block.state.IBlockState;

import org.apache.commons.lang3.mutable.MutableObject;

import matter_manipulator.core.block_spec.ApplyResult;

/// A block state transformer is an object that can modify in-world blocks depending on the existing [IBlockState] and
/// the target [IBlockState].
public interface BlockStateTransformer {

    /// Modifies `existing` to match `target` as best as possible. This should not spawn in items or change block
    /// materials, it should only update things like `facing` properties.
    ApplyResult transform(MutableObject<IBlockState> state, IBlockState target);

}
